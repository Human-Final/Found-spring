package com.human.found.domain.admin.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.human.found.domain.admin.service.UserManageService;
import com.human.found.domain.user.vo.UserVO;

import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class UserManageController {
    
    private final UserManageService userManageService;

    // 전체 회원 조회
    @GetMapping("/test/users")
    public String userManageView(Model model, Authentication authentication) {

        boolean isAdmin = hasRole(authentication, "ADMIN");
        boolean isManager = hasRole(authentication, "MANAGER");

        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isManager", isManager);

        List<UserVO> userList = userManageService.totalUserList();
        
        model.addAttribute("userList", userList);
        return "admin/userManage";
    }


    // 유저 권한 변경
    @PostMapping("/test/users/edit")
    public String updateUserBulk(
                @RequestParam(value = "statusUserIds", required = false) List<String> statusUserIds,
                @RequestParam(value="statuses", required = false) List<String> statuses,
                @RequestParam(value = "isDeletedList", required = false) List<Integer> isDeletedList,
                @RequestParam(value = "roleUserIds", required = false) List<String> roleUserIds,
                @RequestParam(value="roles", required = false) List<String> roles,
                Authentication authentication) {


        boolean isAdmin = hasRole(authentication, "ADMIN");
        boolean isManager = hasRole(authentication, "MANAGER");

        userManageService.updateUserBulk(
                statusUserIds,
                statuses, 
                isDeletedList,
                roleUserIds,
                roles,
                isAdmin,
                isManager
        );

        return "redirect:/test/users";
    }
    
    private boolean hasRole(Authentication authentication, String role) {
    if (authentication == null || !authentication.isAuthenticated()) {
        return false;
    }

    return authentication.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role)
                           || auth.getAuthority().equals(role));
}
}
