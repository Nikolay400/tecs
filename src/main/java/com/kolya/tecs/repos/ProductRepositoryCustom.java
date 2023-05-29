package com.kolya.tecs.repos;

import com.kolya.tecs.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface ProductRepositoryCustom {
    Page<Product> findByDescContainStrings(Pageable pageable,
                                           Float priceFrom,
                                           Float priceTo,
                                           Map<String,List<String>> filters,
                                           List<Integer> childIds);
}
