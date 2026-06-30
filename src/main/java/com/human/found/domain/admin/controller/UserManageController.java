package com.human.found.domain.admin.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.human.found.domain.admin.service.UserManageService;

import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class UserManageController {
    
    private final UserManageService userManageService;

    @GetMapping("/test/usermanage")
    public String userManageView() {
        return "admin/userManage";
    }

    // 유저 권한 변경
    @PostMapping("/test/usermanage")
    public String updateUserStatus(@RequestParam("userIds") List<String> userIds,
                                   @RequestParam("status") String status,
                                   @RequestParam("isDeleted") int isDeleted) {
        userManageService.updateUserStatusByIds(userIds, status, isDeleted);
        return "redirect:/test/usermanage";
    }
    
    
}
