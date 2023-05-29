package com.kolya.tecs.repos;

import com.kolya.tecs.model.Category;
import com.kolya.tecs.model.Product;
import com.kolya.tecs.service.CategoryService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProductRepositoryCustomImpl implements ProductRepositoryCustom{

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private CategoryService categoryService;
    @Override
    public Page<Product> findByDescContainStrings(Pageable pageable,
                                                  Float priceFrom,
                                                  Float priceTo,
                                                  Map<String,List<String>> filters,
                                                  List<Integer> childIds) {
        List<Category> cats = new ArrayList<>();
        for (Integer childId:childIds){
            cats.add(categoryService.findById(childId));
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> query = cb.createQuery(Product.class);
        Root<Product> product = query.from(Product.class);

        Path<String> productPath = product.get("description");
        Path<Category> productPath2 = product.get("category");

        List<Predicate> predicates1 = new ArrayList<>();


        CriteriaBuilder.In<Category> inClause = cb.in(productPath2);
        for (Category c:cats){
            inClause.value(c);
        }

        predicates1.add(inClause);
        predicates1.add(cb.between(product.get("price"),priceFrom,priceTo));

        for (String filter : filters.keySet()) {
            List<Predicate> predicates2 = new ArrayList<>();
            for(String value:filters.get(filter)){
                predicates2.add(cb.like(productPath, "%\""+filter+"\":\""+value+"\"%"));
            }
            predicates1.add(cb.or(predicates2.toArray(new Predicate[predicates2.size()])));
        }


        Order ord;
        if (pageable.getSort().toList().size()!=0){
            ord=pageable.getSort().toList().get(0).equals(Sort.Order.asc("price")) ?cb.asc(product.get("price")):cb.desc(product.get("price"));
        }else{
            ord = cb.desc(product.get("id"));
        }

        query.select(product)
                .where(cb.and(predicates1.toArray(new Predicate[predicates1.size()])))
                .orderBy(ord);

        List<Product> result = entityManager.createQuery(query).setFirstResult((int) pageable.getOffset()).setMaxResults(pageable.getPageSize()).getResultList();


        // Create Count Query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Product> productCount = countQuery.from(Product.class);
        List<Predicate> predicates3 = new ArrayList<>();


        CriteriaBuilder.In<Category> inClause2 = cb.in(productCount.get("category"));
        for (Category c:cats){
            inClause2.value(c);
        }

        predicates3.add(inClause2);
        predicates3.add(cb.between(productCount.get("price"),priceFrom,priceTo));
        Path<String> productCountPath = productCount.get("description");
        for (String filter : filters.keySet()) {
            List<Predicate> predicates2 = new ArrayList<>();
            for(String value:filters.get(filter)){
                predicates2.add(cb.like(productCountPath, "%\""+filter+"\":\""+value+"\"%"));
            }
            predicates3.add(cb.or(predicates2.toArray(new Predicate[predicates2.size()])));
        }

        countQuery.select(cb.count(productCount)).where(cb.and(predicates3.toArray(new Predicate[predicates3.size()])));

        Long count = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<Product>(result,pageable,count);
    }
}
