package com.kolya.tecs.service;

import com.kolya.tecs.model.Order;
import com.kolya.tecs.repos.OrderRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    @Autowired
    private OrderRepo orderRepo;

    public boolean findByFileLike(String orderId){
        return orderRepo.findByFileLike("%"+orderId+".txt")!=null;
    }

    public void save(Order order) {
        orderRepo.save(order);
    }
}
