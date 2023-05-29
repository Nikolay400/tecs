package com.kolya.tecs.controller;

import com.kolya.tecs.controller.utils.ControllerUtils;
import com.kolya.tecs.model.*;
import com.kolya.tecs.service.CartService;
import com.kolya.tecs.service.CategoryService;
import com.kolya.tecs.service.FileService;
import com.kolya.tecs.service.OrderService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
public class BuyController {


    @Autowired
    private OrderService orderService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CartService cartService;

    @Autowired
    private FileService fileService;

    @GetMapping("/cart")
    public String cart(@AuthenticationPrincipal User user,
                       Model model,
                       HttpSession session){

        float bill = 0;
        List<Cart> cart = cartService.getCart(user,session);
        for(Cart c:cart){
            bill+=c.getProduct().getPrice()*c.getQuantity();
        }
        model.addAttribute("bill",bill);
        model.addAttribute("cart",cart);
        model.addAttribute("user",user);
        return  "cart";
    }

    @DeleteMapping("/cart/{id}")
    @ResponseBody
    public String cartDelete(@AuthenticationPrincipal User user,
                            HttpSession session,
                            @PathVariable long id){
        cartService.deleteFromCart(user,session,id);
        return  "ok";
    }

    @GetMapping("/buy-form")
    public String buyForm(@AuthenticationPrincipal User user,
                          Model model,
                          HttpSession session
    ){
        List<Cart> cart = cartService.getCart(user,session);
        model.addAttribute("cart",cart);
        model.addAttribute("user",user);
        model.addAttribute("newUser",user);
        return "buyForm";
    }

    @PostMapping("/buy-form")
    public String newOrder(@AuthenticationPrincipal User user,
                           @ModelAttribute("buyer") @Valid Buyer userFromForm,
                           BindingResult bindingResult,
                           @RequestParam(name = "address",required = true) String address,
                           Model model,
                           HttpSession session
    ) throws IOException {

        model.addAttribute("user",user);

        if (bindingResult.hasErrors()){
            model.mergeAttributes(ControllerUtils.getErrors(bindingResult));
            model.addAttribute("newUser",userFromForm);
            return "buyForm";
        }

        List<Cart> cart = cartService.getCart(user,session);
        Map.Entry<String,String> orderInfo = fileService.createOrderFile(userFromForm,cart);
        Order order = new Order(new Date(),orderInfo.getValue(),"Обрабатывается");
        orderService.save(order);
        if (user!=null){
            cartService.deleteAll(cart);
        }
        else{
            session.removeAttribute("sesCart");
        }
        return "redirect:/order-success/"+orderInfo.getKey();
    }

    @GetMapping("/order-success/{orderId}")
    public String orderSuccess(@AuthenticationPrincipal User user,
                               @PathVariable String orderId){
        if(orderService.findByFileLike(orderId)){
            return "orderFeedback";
        }
        return "redirect:/";
    }


    @ModelAttribute("catalog")
    private Map<Integer, CategoryService.CategoryInfo> catalogs(){
        List<Category> cats = categoryService.getCats();
        Map<Integer, List<Integer>> catTree = categoryService.getCatTree(cats);
        Map<Integer, CategoryService.CategoryInfo> catalog = categoryService.createCatalog(cats,catTree);
        return catalog;
    }
}
