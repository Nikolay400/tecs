package com.kolya.tecs.repos;

import com.kolya.tecs.model.Order;
import org.springframework.data.repository.CrudRepository;

public interface OrderRepo extends CrudRepository<Order,Long> {
    Order findByFileLike(String file);
}
