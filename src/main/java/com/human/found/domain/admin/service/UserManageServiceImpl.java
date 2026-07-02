package com.human.found.domain.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.human.found.domain.admin.dto.UserSearchConditionDTO;
import com.human.found.domain.admin.mapper.UserManageMapper;
import com.human.found.domain.user.mapper.UserMapper;
import com.human.found.domain.user.vo.UserVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserManageServiceImpl implements UserManageService{
    
    private final UserManageMapper userManageMapper;
    private final UserMapper userMapper;


    // 유저 전체 조회 -----------
    @Override
    public List<UserVO> totalUserList(){
        return userManageMapper.totalUserList();
    }


    // 유저 상태 변경 ------------
    @Transactional
    @Override
    public int updateUserBulk(
            List<String> statusUserIds,
            List<String> statuses, 
            List<Integer> isDeletedList,

            List<String> roleUserIds,
            List<String> roles,
            
            List<String> profileUserIds,
            List<String> names,
            List<String> emails,
            List<String> tels,
            
            boolean isAdmin,
            boolean isManager){


        int updatedCount = 0;

       // 상태 변경이 있는 경우에만 실행
        if (statusUserIds != null && !statusUserIds.isEmpty()) {
            updatedCount += updateUserStatusBulk(
                statusUserIds,
                statuses,
                isDeletedList,
                isAdmin,
                isManager
            );
        }

        // 권한 변경이 있는 경우만 실행
        if (roleUserIds != null && !roleUserIds.isEmpty()) {
            updatedCount += updateUserRoleBulk(
                roleUserIds,
                roles,
                isAdmin
            );
        }

        // 이름 / 이메일 / 전화번호 변경이 있는 경우만 실행
        if (profileUserIds != null && !profileUserIds.isEmpty()) {
            updatedCount += updateUserProfileBulk(
                profileUserIds,
                names,
                emails,
                tels,
                isAdmin,
                isManager
            );
        }

        return updatedCount;
    }


    // 유저 상태 변경 --------------
    private int updateUserStatusBulk(List<String> statusUserIds,
            List<String> statuses,
            List<Integer> isDeletedList,
            boolean isAdmin,
            boolean isManager) {

        if (statuses == null || isDeletedList == null) {
            throw new IllegalArgumentException("변경할 상태 정보가 없습니다.");
        }

        if(statusUserIds.size() != statuses.size()
                || statusUserIds.size() != isDeletedList.size()) {
            throw new IllegalArgumentException("회원 ID, 상태, 삭제 여부 개수가 일치하지 않습니다.");
        }

        int updatedCount = 0;

        for(int i = 0; i < statusUserIds.size(); i++){
            String userId = statusUserIds.get(i);
            String status = statuses.get(i);
            Integer isDeleted = isDeletedList.get(i);

            validateStatus(status);
            validateIsDeleted(isDeleted);

            UserVO targetUser =  userManageMapper.findByIdIncludeDeleted(userId);

            if(targetUser == null){
                throw new IllegalArgumentException("존재하지 않는 회원입니다: " + userId);
            }

            // ADMIN은 전부 가능
            if(!isAdmin){
                // MANAGER도 아니면 불가
                if (!isManager) {
                    throw new IllegalArgumentException("상태 변경 권한이 없습니다.");
                }
            

                // 매니저는 유저만 변경 가능
                if(!"USER".equals(targetUser.getRole())){
                    throw new IllegalArgumentException("매니저는 일반 회원의 상태만 변경할 수 있습니다.");
                }
            }

            updatedCount += userManageMapper.updateUserStatusById(
                    userId,
                    status, 
                    isDeleted
            );
        }
        
        return updatedCount;
    }


    // 유저 권한 변경 -------------------------
    private int updateUserRoleBulk(List<String> roleUserIds,
                                   List<String> roles,
                                   boolean isAdmin) {

        if (!isAdmin) {
            throw new IllegalArgumentException("권한 변경은 최고관리자만 가능합니다.");
        }

        if (roles == null) {
            throw new IllegalArgumentException("변경할 권한 정보가 없습니다.");
        }

        if (roleUserIds.size() != roles.size()) {
            throw new IllegalArgumentException("회원 ID와 권한 개수가 일치하지 않습니다.");
        }

        int updatedCount = 0;

        for (int i = 0; i < roleUserIds.size(); i++) {
            String userId = roleUserIds.get(i);
            String role = roles.get(i);
            
            validateRole(role);

            UserVO targetUser =  userManageMapper.findByIdIncludeDeleted(userId);

            if (targetUser == null) {
                throw new IllegalArgumentException("존재하지 않는 회원입니다: " + userId);
            }

            updatedCount += userManageMapper.updateUserRoleById(userId, role);
        }

        return updatedCount;
    }


    // 유저 정보 변경 -----------------------
    private int updateUserProfileBulk(
            List<String> profileUserIds,
            List<String> names,
            List<String> emails,
            List<String> tels,
            boolean isAdmin,
            boolean isManager) {
    
        if (!isAdmin && !isManager) {
            throw new IllegalArgumentException("회원 정보 변경 권한이 없습니다.");
        }

        if (names == null || emails == null || tels == null) {
            throw new IllegalArgumentException("변경할 회원 정보가 없습니다.");
        }

        if (profileUserIds.size() != names.size()
                || profileUserIds.size() != emails.size()
                || profileUserIds.size() != tels.size()) {
            throw new IllegalArgumentException("회원 ID, 이름, 이메일, 전화번호 개수가 일치하지 않습니다.");
        }

        int updatedCount = 0;

        for (int i = 0; i < profileUserIds.size(); i++) {
            String userId = profileUserIds.get(i);
            String name = names.get(i);
            String email = emails.get(i);
            String tel = tels.get(i);

            validateProfile(name, email, tel);

            UserVO targetUser = userManageMapper.findByIdIncludeDeleted(userId);

            if (targetUser == null) {
                throw new IllegalArgumentException("존재하지 않는 회원입니다: " + userId);
            }

            // ADMIN은 전부 가능
            if (!isAdmin) {
                // MANAGER도 아니면 불가
                if (!isManager) {
                    throw new IllegalArgumentException("회원 정보 변경 권한이 없습니다.");
                }

                // MANAGER는 일반 회원만 변경 가능
                if (!"USER".equals(targetUser.getRole())) {
                    throw new IllegalArgumentException("매니저는 일반 회원의 정보만 변경할 수 있습니다.");
                }
            }
        
        // 이메일 중복 체크
        
        if (!email.trim().equals(targetUser.getEmail())) {
            int duplicateEmailCount = userMapper.countByEmail(email.trim());

            if (duplicateEmailCount > 0) {
                throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + email);
            }
        }
        
        updatedCount += userManageMapper.updateUserProfileById(
                userId,
                name.trim(),
                email.trim(),
                tel.trim()
        );
    }

        return updatedCount;
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
    private void validateProfile(String name, String email, String tel) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("이름은 필수입니다.");
        }

         String trimmedName = name.trim();

        // 한글 완성형(가-힣), 영문 대소문자만 허용
        // ㅁㄴㅇㄹ 같은 한글 자음/모음 단독 입력은 막힘
        if (!trimmedName.matches("^[가-힣a-zA-Z]{2,20}$")) {
            throw new IllegalArgumentException("이름은 한글 또는 영문 2~20자로 입력해야 합니다.");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }

        String trimmedEmail = email.trim();

        // 영문, 숫자, 일부 특수문자만 허용
        // 한글 포함 불가
        // 반드시 .com으로 끝나야 함
        if (!trimmedEmail.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.com$")) {
            throw new IllegalArgumentException("이메일은 영문/숫자 형식이어야 하며 .com으로 끝나야 합니다.");
        }

        if (!email.contains("@")) {
            throw new IllegalArgumentException("이메일 형식이 올바르지 않습니다.");
        }

         if (tel == null || tel.trim().isEmpty()) {
            throw new IllegalArgumentException("전화번호는 필수입니다.");
        }

        String trimmedTel = tel.trim();

        // 숫자만 11자리
        if (!trimmedTel.matches("^010[0-9]{8}$")) {
            throw new IllegalArgumentException("전화번호는 숫자 11자리로 입력해야 합니다.");
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


}
