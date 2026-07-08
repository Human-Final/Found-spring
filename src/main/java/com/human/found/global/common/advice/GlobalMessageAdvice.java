package com.human.found.global.common.advice;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.servlet.http.HttpSession;

// 역할 : session에 errorMessage가 있는지 확인해서 있으면 model의 errorMessage로 옮김
//        session에서는 바로 제거하고 header에서 ${errorMessage}로 alert 출력

@ControllerAdvice
public class GlobalMessageAdvice {
    
    @ModelAttribute
    public void errorMessage(Model model, HttpSession session){
        String message = (String) session.getAttribute("errorMessage");

        if(message != null){

            model.addAttribute("errorMessage", message);
            session.removeAttribute("errorMessage");
        }
    }
}
