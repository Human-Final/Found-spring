package com.human.found.domain.found.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;

@Controller
public class ViewTestController {
    
    @GetMapping("test/api/lost")
    public String lostBoard() {
        return "lost/list";
    }

    @GetMapping("test/api/found")
    public String foundBoard() {
        return "found/list";
    }

    @GetMapping("/mypage")
    public String mypage() {
        return "user/mypage";
    }

//     @PostMapping("/found/write")
//     public String createFound(@Valid @ModelAttribute("postForm") PostForm postForm,
//                             BindingResult bindingResult,
//                             Model model) {
//         if (bindingResult.hasErrors()) {
//             model.addAttribute("categories", categories());
//             return "write-post";
//     }

//     foundService.save(postForm);
//     return "redirect:/found";

//     @PostMapping("/lost/write")
//     public String createLost(@Valid @ModelAttribute("postForm") PostForm postForm,
//                             BindingResult bindingResult,
//                             Model model) {
//         if (bindingResult.hasErrors()) {
//             model.addAttribute("categories", categories());
//             return "write-post";
//         }

//         lostService.save(postForm);
//         return "redirect:/lost";
//     }
// }
    @GetMapping("/api/write")
    public String writeForm() {
        // model.addAttribute("postForm", new PostForm());
        // model.addAttribute("categories", categories());
        return "found/write";
    }


}
