package com.kolya.tecs.repos;

import com.kolya.tecs.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepo extends JpaRepository<Category,Integer> {
    List<Category> findByOrderByOrdrAsc();

    Category findByAlias(String alias);
}
