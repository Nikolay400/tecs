package com.kolya.tecs.controller;

import com.kolya.tecs.model.User;
import com.kolya.tecs.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class CommonModelAttributes {

    @Autowired
    private CartService cartService;

    @ModelAttribute("cartQuantity")
    public int cartQuantity(@AuthenticationPrincipal User user, HttpSession session){
        return cartService.getCartQuantity(user, session);
    }
}
