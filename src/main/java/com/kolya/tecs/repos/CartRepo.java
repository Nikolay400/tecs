package com.kolya.tecs.repos;

import com.kolya.tecs.model.Cart;
import com.kolya.tecs.model.Product;
import com.kolya.tecs.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CartRepo extends JpaRepository<Cart, Long> {
    Cart findByProductAndUser(Product product, User user);
    List<Cart> findByUser(User user);
    int countByUser(User user);

    @Transactional
    void deleteByUserAndProduct(User user, Product product);
}
