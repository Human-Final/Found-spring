package com.human.found.domain.admin.service;

import static com.human.found.global.common.validation.UserValidationRules.*; 

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.human.found.domain.admin.dto.UserBulkInfoDTO;
import com.human.found.domain.admin.dto.UserSearchConditionDTO;
import com.human.found.domain.admin.mapper.UserManageMapper;
import com.human.found.domain.user.mapper.UserMapper;
import com.human.found.domain.user.vo.UserVO;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserManageServiceImpl implements UserManageService{
    
    private final UserManageMapper userManageMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;


    // 유저 전체 조회 -----------
    @Override
    public List<UserVO> totalUserList(){
        return userManageMapper.totalUserList();
    }


    // 유저 정보 변경/등록 한번에 처리 ------------
    @Transactional
    @Override
    public int updateUserBulk(
            UserBulkInfoDTO userInfo,             
            boolean isAdmin,
            boolean isManager){


        if (!isAdmin && !isManager) {
            throw new IllegalArgumentException("회원 관리 권한이 없습니다.");
        }

        if (userInfo == null) {
            throw new IllegalArgumentException("수정할 회원 정보가 없습니다.");
        }

        int totalCount = 0;

        List<UserVO> changedUsers =
                userInfo.getChangedUsers() == null ? List.of() : userInfo.getChangedUsers();

        List<UserVO> newUsers =
                userInfo.getNewUsers() == null ? List.of() : userInfo.getNewUsers();

        // 실제 DB 수정 전에 변경 후 최고관리자 수를 예상해서 검증
        vaildateOneAdmin(changedUsers, newUsers);                
        
        if (!changedUsers.isEmpty()) {
            totalCount += updateUserChangedBulk(changedUsers, isAdmin, isManager);
        }

        if (!newUsers.isEmpty()) {
            totalCount += insertNewUsersBulk(newUsers, isAdmin, isManager);
        }

        return totalCount;
    }


    // 유저 정보 변경 --------------
    private int updateUserChangedBulk(
            List<UserVO> changedUsers,
            boolean isAdmin,
            boolean isManager) {

        int updatedCount = 0;
        
        
        for (UserVO changedUser : changedUsers) {

            if (changedUser == null) {
                continue;
            }

            String userId = changedUser.getId();

            if (userId == null || userId.trim().isEmpty()) {
                throw new IllegalArgumentException("회원 ID가 없습니다.");
            }

            userId = userId.trim();

            UserVO targetUser = userManageMapper.findByIdIncludeDeleted(userId);

            if (targetUser == null) {
                throw new IllegalArgumentException("존재하지 않는 회원입니다: " + userId);
            }

            // ADMIN은 전부 가능. MANAGER는 USER만 수정 가능
            if(!isAdmin){
                if (!isManager) {
                    throw new IllegalArgumentException("회원 정보 변경 권한이 없습니다.");
                }
            
                // 매니저는 유저만 변경 가능
                if(!"USER".equals(targetUser.getRole())){
                    throw new IllegalArgumentException("매니저는 일반 회원의 상태만 변경할 수 있습니다.");
                }
            }

        String name = changedUser.getName();
        String email = changedUser.getEmail();
        String tel = changedUser.getTel();
        String status = changedUser.getStatus();
        Integer isDeleted = changedUser.getIsDeleted();
        String role = changedUser.getRole();

        String trimmedName = name == null ? "" : name.trim();
        String trimmedEmail = email == null ? "" : email.trim();
        String trimmedTel = tel == null ? "" : tel.trim();

        boolean profileChanged =
                !trimmedName.equals(targetUser.getName())
                || !trimmedEmail.equals(targetUser.getEmail())
                || !trimmedTel.equals(targetUser.getTel());

        validateStatus(status);
        validateIsDeleted(isDeleted);
        
        // ADMIN일 때만 role 검증
        if (isAdmin) {
            validateRole(role);
        }

        // 이메일 중복 체크
        if (profileChanged && !trimmedEmail.equals(targetUser.getEmail())) {
            int duplicateEmailCount = userMapper.countByEmail(trimmedEmail);

            if (duplicateEmailCount > 0) {
                throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + trimmedEmail);
            }
        }

        updatedCount += userManageMapper.updateUserById(
                userId,
                trimmedName,
                trimmedEmail,
                trimmedTel,
                status,
                isDeleted,
                role,
                isAdmin
            );
        }
        
        return updatedCount;
    }


    // 신규 회원 추가 -----------------------
    private int insertNewUsersBulk(
            List<UserVO> newUsers,
            boolean isAdmin,
            boolean isManager) {

        if (!isAdmin && !isManager) {
            throw new IllegalArgumentException("회원 추가 권한이 없습니다.");
        }

        int insertedCount = 0;

        // 이번 요청 안에서 아이디 중복 방지
        Set<String> requestUserIds = new HashSet<>();
        
        for (UserVO newUser : newUsers) {
            String userId = newUser.getId();
            String name = newUser.getName();
            String email = newUser.getEmail();
            String tel = newUser.getTel();
            String role = newUser.getRole();
            String status = newUser.getStatus();

            validateNewUser(userId, name, email, tel, role, status, isAdmin, isManager);

            // 같은 요청 안에서 중복된 아이디 검사
            if (!requestUserIds.add(userId.trim())) {
                throw new IllegalArgumentException("추가하려는 회원 목록에 중복된 아이디가 있습니다: " + userId);
            }
            
            // DB에 존재하는 아이디와 중복 검사
            UserVO existingUser = userManageMapper.findByIdIncludeDeleted(userId);

            if (existingUser != null) {
                throw new IllegalArgumentException("이미 사용 중인 아이디입니다: " + userId);
            }

            // 이메일 중복 검사
            int duplicateEmailCount = userMapper.countByEmail(email.trim());

            if (duplicateEmailCount > 0) {
                throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + email);
            }

            // 임시 비밀번호 = 전화번호 뒤 4자리
            String tempPassword = tel.trim().substring(tel.trim().length() - 4);
            String encodedPassword = passwordEncoder.encode(tempPassword);

            newUser.setId(userId.trim());
            newUser.setPw(encodedPassword);
            newUser.setName(name.trim());
            newUser.setEmail(email.trim());
            newUser.setTel(tel.trim());
            newUser.setStatus(status);
            newUser.setRole(role);
            newUser.setIsDeleted(0);

            userManageMapper.insertUserByAdmin(newUser);
            insertedCount++;
        }

        return insertedCount;
    }


    // **검증-------------------------------------------
    // 상태 검증 ----------
    private void validateStatus(String status) {
        if (!List.of("active", "dormant", "blocked").contains(status)) {
            throw new IllegalArgumentException("허용되지 않은 회원 상태입니다: " + status);
        }
    }

    // 탈퇴 여부 검증 -------------
    private void validateIsDeleted(Integer isDeleted) {
        if (isDeleted == null || (isDeleted != 0 && isDeleted != 1)) {
            throw new IllegalArgumentException("삭제 여부 값이 올바르지 않습니다.");
        }
    }

    // 권한 검증 -------------
    private void validateRole(String role) {
        if (!List.of("USER", "MANAGER", "ADMIN").contains(role)) {
            throw new IllegalArgumentException("허용되지 않은 권한입니다: " + role);
        }
    }

    // 이름 / 이메일 / 전화번호 검증
    // private void validateProfile(String name, String email, String tel) {
    //     if (name == null || name.trim().isEmpty()) {
    //         throw new IllegalArgumentException("이름은 필수입니다.");
    //     }

    //      String trimmedName = name.trim();

    //     // 한글 완성형(가-힣), 영문 대소문자만 허용
    //     // ㅁㄴㅇㄹ 같은 한글 자음/모음 단독 입력은 막힘
    //     if (!trimmedName.matches("^[가-힣a-zA-Z]{2,20}$")) {
    //         throw new IllegalArgumentException("이름은 한글 또는 영문 2~20자로 입력해야 합니다.");
    //     }

    //     if (email == null || email.trim().isEmpty()) {
    //         throw new IllegalArgumentException("이메일은 필수입니다.");
    //     }

    //     String trimmedEmail = email.trim();

    //     // 영문, 숫자, 일부 특수문자만 허용
    //     // 한글 포함 불가
    //     // 반드시 .com으로 끝나야 함
    //     if (!trimmedEmail.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.com$")) {
    //         throw new IllegalArgumentException("이메일은 영문/숫자 형식이어야 하며 .com으로 끝나야 합니다.");
    //     }

    //     if (tel == null || tel.trim().isEmpty()) {
    //         throw new IllegalArgumentException("전화번호는 필수입니다.");
    //     }

    //     String trimmedTel = tel.trim();

    //     // 숫자만 11자리
    //     if (!trimmedTel.matches("^010[0-9]{8}$")) {
    //         throw new IllegalArgumentException("전화번호는 숫자 11자리로 입력해야 합니다.");
    //     }

    // }

    // 신규회원 검증
    private void validateNewUser(
            String userId,
            String name,
            String email,
            String tel,
            String role,
            String status,
            boolean isAdmin,
            boolean isManager) {

        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("아이디는 필수입니다.");
        }

        String trimmedUserId = userId.trim();

        if (!trimmedUserId.matches(USER_ID_REGEX)) {
            throw new IllegalArgumentException(USER_ID_MESSAGE);
        }

        // validateProfile(name, email, tel);
        validateRole(role);
        validateStatus(status);

        if (!isAdmin && isManager && !"USER".equals(role)) {
            throw new IllegalArgumentException("매니저는 일반 회원만 추가할 수 있습니다.");
        }
    }

    @Override
    public int countUsers(UserSearchConditionDTO conditionDTO) {
        return userManageMapper.countUsers(conditionDTO);
    }


    @Override
    public List<UserVO> searchUsers(UserSearchConditionDTO conditionDTO) {
       return userManageMapper.searchUsers(conditionDTO);
    }


    @Override
    public void userInfoDownload(
            UserSearchConditionDTO conditionDTO, 
            HttpServletResponse response
        ) throws IOException {
        
        List<UserVO> userList = userManageMapper.userInfoDownload(conditionDTO);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("회원목록");

        String[] headers = {
            "아이디", "이름", "이메일", "전화번호", "가입일", "권한", "상태", "탈퇴일"
        };

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(0);

        for(int i = 0; i < headers.length; i++){
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIndex = 1;

        for(UserVO user : userList){
            Row row = sheet.createRow(rowIndex++);

            row.createCell(0).setCellValue(nullToBlank(user.getId()));
            row.createCell(1).setCellValue(nullToBlank(user.getName()));
            row.createCell(2).setCellValue(nullToBlank(user.getEmail()));
            row.createCell(3).setCellValue(nullToBlank(user.getTel()));
            row.createCell(4).setCellValue(
                user.getSignUp() != null ? user.getSignUp().toString() : "");
            row.createCell(5).setCellValue(getRoleLabel(user.getRole()));
            row.createCell(6).setCellValue(getStatusLabel(user));
            row.createCell(7).setCellValue(
                user.getDeletedAt() != null ? user.getDeletedAt().toString() : "");
        }  

        for(int i = 0; i < headers.length; i++){
            sheet.autoSizeColumn(i);
        }

        String fileName = URLEncoder.encode("회원목록.xlsx", StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);

            workbook.write(response.getOutputStream());
            workbook.close();

    }

    private String nullToBlank(String value){
        return value == null? "" : value;
    }

    private String getRoleLabel(String role){
        if("USER".equals(role)){
            return "일반 회원";
        }
        
        if("MANAGER".equals(role)){
            return "관리자";
        }

        if("ADMIN".equals(role)){
            return "최고관리자";
        }

        return role == null ? "" : role;
    }
     
    private String getStatusLabel(UserVO userVO){
        if(userVO.getIsDeleted() == 1){
            return "탈퇴";
        }

        String status = userVO.getStatus();
        if("active".equals(status)){
            return "활성";
        }

        if("dormant".equals(status)){
            return "휴면";
        }

        if("blocked".equals(status)){
            return "정지";
        }

        return status == null ? "" : status;
    }

    // 최고관리자 1명 보장 로직
    private void vaildateOneAdmin(
            List<UserVO> changedUsers,
            List<UserVO> newUsers){

        int expectedAdminCount  = userManageMapper.countAdmin();

        for (UserVO changedUser : changedUsers) {
            if (changedUser == null || changedUser.getId() == null) {
                continue;
            }

            UserVO currentUser = userManageMapper.findByIdIncludeDeleted(changedUser.getId());

            if (currentUser == null) {
                continue;
            }

            boolean wasAdmin = isAvailableAdmin(
                    currentUser.getRole(),
                    currentUser.getStatus(),
                    currentUser.getIsDeleted()
            );

            String nextRole =
                changedUser.getRole() != null ? changedUser.getRole() : currentUser.getRole();

            String nextStatus =
                    changedUser.getStatus() != null 
                        ? changedUser.getStatus() 
                        : currentUser.getStatus();

            int nextIsDeleted = changedUser.getIsDeleted();

            boolean willBeAdmin =  
                isAvailableAdmin(nextRole, nextStatus, nextIsDeleted);

             if (wasAdmin && !willBeAdmin) {
                expectedAdminCount--;
            }

            if (!wasAdmin && willBeAdmin) {
                expectedAdminCount++;
            }
        }

        for (UserVO newUser : newUsers) {
            if (newUser == null) {
                continue;
            }

            int isDeleted =  0;

            if (isAvailableAdmin(newUser.getRole(), newUser.getStatus(), isDeleted)) {
                expectedAdminCount++;
            }
        }

        if(expectedAdminCount  < 1){
            throw new IllegalArgumentException("최고관리자는 최소 1명 이상 유지되어야 합니다.");
        }
    }

    // 활성 상태인 최고 관리자 유무 검증
    private boolean isAvailableAdmin(String role, String status, int isDeleted) {
        return "ADMIN".equals(role)
                && isDeleted == 0
                && "active".equals(status);
    }


}
