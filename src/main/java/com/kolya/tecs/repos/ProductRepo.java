package com.kolya.tecs.repos;

import com.kolya.tecs.model.Category;
import com.kolya.tecs.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepo extends JpaRepository<Product,Long>,ProductRepositoryCustom {

    List<Product> findAllByOrderById();
    Page<Product> findAll(Pageable pageable);


    Page<Product> findByPriceBetweenAndCategoryIdIn(Pageable pageable,float priceFrom,float priceTo,List<Integer> childIds);

    @Query(value = "SELECT * FROM product WHERE LOWER(name) LIKE CONCAT('%',LOWER(:search),'%') ",nativeQuery = true)
    Page<Product> findByNameContaining(Pageable pageable,@Param("search") String search);

    List<Product> findFirst4ByCategoryAndIdNot(Category category,Long id);

    List<Product> findByCategoryIdIn(List<Integer> childIds);

    @Query(value = "SELECT MAX(price) FROM product WHERE category_id IN :list",nativeQuery = true)
    Float findMaxPrice(@Param("list") List<Integer> childIds);
    @Query(value = "SELECT MIN(price) FROM product WHERE category_id IN :list",nativeQuery = true)
    Float findMinPrice(@Param("list")List<Integer> childIds);
}
