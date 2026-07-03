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
    @GetMapping("/test/users")
    public String userManageView(
                @ModelAttribute("conditionDTO") UserSearchConditionDTO conditionDTO,
                Model model, 
                Authentication authentication) {

        boolean isAdmin = hasRole(authentication, "ADMIN");
        boolean isManager = hasRole(authentication, "MANAGER");

        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isManager", isManager);
        
        conditionDTO.setSize(50);

        int totalCount = userManageService.countUsers(conditionDTO);
        conditionDTO.pageInfo(totalCount);

        List<UserVO> userList = userManageService.searchUsers(conditionDTO);
        
        model.addAttribute("userList", userList);
        model.addAttribute("conditionDTO", conditionDTO);
        model.addAttribute("paging", conditionDTO);
        return "admin/userManage";
    }

    @GetMapping("/api/test/users/download")
    public void downloadUsers(
            @ModelAttribute UserSearchConditionDTO conditionDTO,
            HttpServletResponse response
        ) throws IOException {
        
        userManageService.userInfoDownload(conditionDTO, response);
    }
    


    // 유저 권한 변경
    @PostMapping("/test/users/edit")
    public String updateUserBulk(
                @RequestParam(value = "statusUserIds", required = false) List<String> statusUserIds,
                @RequestParam(value="statuses", required = false) List<String> statuses,
                @RequestParam(value = "isDeletedList", required = false) List<Integer> isDeletedList,
                @RequestParam(value = "roleUserIds", required = false) List<String> roleUserIds,
                @RequestParam(value="roles", required = false) List<String> roles,
                @RequestParam(required = false) List<String> profileUserIds,
                @RequestParam(required = false) List<String> names,
                @RequestParam(required = false) List<String> emails,
                @RequestParam(required = false) List<String> tels,
                RedirectAttributes redirectAttributes,
                Authentication authentication) {


        boolean isAdmin = hasRole(authentication, "ADMIN");
        boolean isManager = hasRole(authentication, "MANAGER");

        userManageService.updateUserBulk(
                statusUserIds,
                statuses, 
                isDeletedList,
                roleUserIds,
                roles,
                profileUserIds,
                names,
                emails,
                tels,
                isAdmin,
                isManager
        );

        redirectAttributes.addFlashAttribute(
            "message", "회원 정보가 수정되었습니다.");

        return "redirect:/test/users";
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

