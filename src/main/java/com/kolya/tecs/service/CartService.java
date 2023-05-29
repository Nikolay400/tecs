package com.kolya.tecs.service;

import com.kolya.tecs.model.Cart;
import com.kolya.tecs.model.Product;
import com.kolya.tecs.model.User;
import com.kolya.tecs.repos.CartRepo;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartService {

    @Autowired
    private CartRepo cartRepo;

    @Autowired
    private ProductService productService;

    public void addProductToUserCart(Product product, int quantity, User user){
        Cart cart = cartRepo.findByProductAndUser(product,user);
        if (cart!=null){
            cart.setQuantity(cart.getQuantity()+quantity);
        }
        else{
            cart=new Cart();
            cart.setUser(user);
            cart.setProduct(product);
            cart.setQuantity(quantity);
        }
        cartRepo.save(cart);
    }

    public int getCartQuantity(User user, HttpSession session){
        List<Cart> cart = getCart(user,session);
        int result = 0;
        for (Cart c:cart){
            result+=c.getQuantity();
        }
        return result;
    }

    public void addProductToSessionCart(Product product, int quantity, HttpSession session){
        Map<Long,Integer> sesCart= (Map<Long,Integer>) session.getAttribute("sesCart");
        if (sesCart==null){
            sesCart = new HashMap<>();
        }
        int newQuantity = quantity;
        Long productId = product.getId();
        if (sesCart.get(productId)!=null){
            newQuantity +=sesCart.get(productId);
        }
        sesCart.put(productId,newQuantity);
        session.setAttribute("sesCart",sesCart);
    }

    public List<Cart> findByUser(User user) {
        return cartRepo.findByUser(user);
    }

    public List<Cart> getCart(User user, HttpSession session){
        List<Cart> cartList = new ArrayList<>();
        if (user!=null){
            cartList = findByUser(user);
        }
        else{
            Map<Long, Integer> sesCart = (Map<Long, Integer>) session.getAttribute("sesCart");
            if (sesCart!=null){
                for(Long id:sesCart.keySet()){
                    Cart cart = new Cart();
                    cart.setId(id);
                    cart.setProduct(productService.findById(id));
                    cart.setQuantity(sesCart.get(id));
                    cartList.add(cart);
                }
            }
        }
        return cartList;
    }

    public void deleteFromCart(User user, HttpSession session,long id){
        if (user!=null){
            cartRepo.deleteById(id);
        }
        else{
            Map<Long, Integer> sesCart = (Map<Long, Integer>) session.getAttribute("sesCart");
            if (sesCart!=null){
                sesCart.remove(id);
                session.setAttribute("sesCart",sesCart);
            }
        }
    }

    public void deleteByUserAndProduct(User user, Product product) {
        cartRepo.deleteByUserAndProduct(user,product);
    }

    public void deleteAll(List<Cart> cart) {
        cartRepo.deleteAll(cart);
    }
}
