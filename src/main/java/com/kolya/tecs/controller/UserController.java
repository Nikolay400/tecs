package com.kolya.tecs.controller;

import com.kolya.tecs.controller.utils.ControllerUtils;
import com.kolya.tecs.model.Category;
import com.kolya.tecs.model.User;
import com.kolya.tecs.service.CategoryService;
import com.kolya.tecs.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Controller
public class UserController {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private UserService userService;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @GetMapping("/registration")
    public String registrationGet(){
        return "registration";
    }

    @PostMapping("/registration")
    public String registrationPost(//@RequestParam("g-recaptcha-response") String recaptchaResponse,
                                   Model model,
                                   @Valid @ModelAttribute("newUser") User user,
                                   BindingResult bindingResult){
        boolean isInvalid = false;
        if (bindingResult.hasErrors()){
            model.mergeAttributes(ControllerUtils.getErrors(bindingResult));
            isInvalid = true;
        }

        /*Map<String,Object> capRes = restTemplate.postForObject("https://www.google.com/recaptcha/api/siteverify?secret=6Lf0dFQkAAAAAETvmbk2oGAPaQJ01Gv6UQV9TpuM&response="+recaptchaResponse, Collections.emptyList(), Map.class);
        if (capRes.get("success").equals(false)){
            model.addAttribute("captchaError","Pass the recaptcha");
            isInvalid = true;
        }*/

        if (isInvalid) return "registration";

        if (userService.addUser(user)==false){
            model.addAttribute("emailError","This email already exists");
            return "registration";
        }

        return "redirect:/login";
    }

    @ModelAttribute("catalog")
    public Map<Integer, CategoryService.CategoryInfo> catalog(){
        List<Category> cats = categoryService.getCats();
        Map<Integer, List<Integer>> catTree = categoryService.getCatTree(cats);
        Map<Integer, CategoryService.CategoryInfo> catalog = categoryService.createCatalog(cats,catTree);
        return catalog;
    }
}
