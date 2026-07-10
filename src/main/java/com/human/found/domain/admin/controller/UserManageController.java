package com.human.found.domain.admin.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.human.found.domain.admin.dto.UserBulkInfoDTO;
import com.human.found.domain.admin.dto.UserSearchConditionDTO;
import com.human.found.domain.admin.service.UserManageService;
import com.human.found.domain.user.vo.UserVO;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class UserManageController {
    
    private final UserManageService userManageService;

    // 회원 목록 조회 + 필터 + 페이징
    @GetMapping("/admin/users")
    public String userManageView(
                @ModelAttribute("conditionDTO") UserSearchConditionDTO conditionDTO,
                @RequestParam(defaultValue="false") boolean searched,
                Model model, 
                Authentication authentication) {

        boolean isAdmin = hasRole(authentication, "ADMIN");
        boolean isManager = hasRole(authentication, "MANAGER");

        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isManager", isManager);
        model.addAttribute("searched", searched);
        
        conditionDTO.setSize(50);

        if (!searched) {
            conditionDTO.pageInfo(0);

            model.addAttribute("userList", List.of());
            model.addAttribute("paging", conditionDTO);
            model.addAttribute("totalUserCount", 0);

            return "admin/userManage";
        }

        if (searched && hasNoCheckedFilter(conditionDTO)) {
            conditionDTO.pageInfo(0);

            model.addAttribute("userList", List.of());
            model.addAttribute("paging", conditionDTO);
            model.addAttribute("totalUserCount", 0);

            return "admin/userManage";
        }

        int totalCount = userManageService.countUsers(conditionDTO);
        conditionDTO.pageInfo(totalCount);

        List<UserVO> userList = userManageService.searchUsers(conditionDTO);
        
        model.addAttribute("userList", userList);
        model.addAttribute("conditionDTO", conditionDTO);
        model.addAttribute("paging", conditionDTO);
        
        model.addAttribute("totalUserCount", totalCount);

        return "admin/userManage";
    }


    // 회원 정보 엑셀로 다운로드
    @GetMapping("/api/admin/users/download")
    public void downloadUsers(
            @ModelAttribute UserSearchConditionDTO conditionDTO,
            HttpServletResponse response
        ) throws IOException {
        
        userManageService.userInfoDownload(conditionDTO, response);
    }
    

    // 유저 정보 변경
    @PostMapping("/admin/users/edit")
    public String updateUserBulk(
                @ModelAttribute UserBulkInfoDTO userInfo,
                @RequestParam(required = false) String returnQuery,
                RedirectAttributes redirectAttributes,
                Authentication authentication) {

        try {
            boolean isAdmin = hasRole(authentication, "ADMIN");
            boolean isManager = hasRole(authentication, "MANAGER");

            userManageService.updateUserBulk(
                    userInfo,
                    isAdmin,
                    isManager
            );

            redirectAttributes.addFlashAttribute(
                "message", "회원 정보가 성공적으로 저장되었습니다.");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                "회원 정보 저장 중 오류가 발생했습니다.");
        }

        if(returnQuery != null && !returnQuery.isBlank()){
            return "redirect:/admin/users?" + returnQuery;
        }

        return "redirect:/admin/users?searched=true";
    }

    // 검색어만 입력하면 검색어 + 전체 필터
    // 검색어 없이 필터만 선택하면 선택된 필터만 검색하기 위한 메서드
    private boolean hasNoCheckedFilter(UserSearchConditionDTO conditionDTO) {
        boolean noKeyword = 
                conditionDTO.getKeyword() == null
                || conditionDTO.getKeyword().trim().isEmpty();            
        
        boolean noStatus =
                conditionDTO.getStatuses() == null
                || conditionDTO.getStatuses().isEmpty();

        boolean noRole =
                conditionDTO.getRoles() == null
                || conditionDTO.getRoles().isEmpty();

        boolean noStartDate = 
                conditionDTO.getStartDate() == null;

        boolean noEndDate = 
                conditionDTO.getEndDate() == null;

        return noKeyword && noStatus && noRole && noStartDate && noEndDate;
    }   

    // 권한 설정용 메서드
    private boolean hasRole(Authentication authentication, String role) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role)
                            || auth.getAuthority().equals(role));
    }

    

}

