package com.kolya.tecs.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kolya.tecs.model.Category;
import com.kolya.tecs.model.Product;
import com.kolya.tecs.repos.ProductRepo;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class ProductService {

    @Autowired
    private ProductRepo productRepo;

    public List<Product> findAllByOrderById(){
       return productRepo.findAllByOrderById();
    }
    public Page<Product> getProductsMain(Pageable pageable,String search){
        if (!Strings.isEmpty(search)){
            return productRepo.findByNameContaining(pageable,search);
        }
        else{
            return productRepo.findAll(pageable);
        }
    }

    public List<Integer> getPaginationNumbers(int pageNow,int totalPage) {
        pageNow +=1;
        List<Integer> paginationNumbers;

        if (totalPage>9){
            if(pageNow<6){
                paginationNumbers= Stream.of(
                        IntStream.rangeClosed(1, 7).boxed(),
                        Stream.of(-1,totalPage)
                ).flatMap(i->i).toList();
            }
            else if(pageNow>totalPage-6){
                paginationNumbers= Stream.of(
                        Stream.of(1,-1),
                        IntStream.rangeClosed(totalPage-6, totalPage).boxed()
                ).flatMap(i->i).toList();
            }
            else{
                paginationNumbers= Stream.of(
                        Stream.of(1,-1),
                        IntStream.rangeClosed(pageNow-2, pageNow+2).boxed(),
                        Stream.of(-1,totalPage)
                ).flatMap(i->i).toList();
            }
        }
        else{
            paginationNumbers= IntStream.rangeClosed(1, totalPage).boxed().toList();
        }
        return paginationNumbers;
    }

    public Map<Long,List<String>> getDescriptionsForModel(List<Product> products){
        Map<Long,List<String>> descs = new HashMap<>();
        for (Product pr:products){
            String[] prDesc = pr.getDescription().replace("\":\"",": ").replaceAll("[{}\"]","").split(", ");
            descs.put(pr.getId(), Arrays.stream(prDesc).limit(4).collect(Collectors.toList()));
        }
        return descs;
    }

    public Float getPrice(boolean isMin, Float priceValue, List<Integer> childIds) {
        if (childIds==null||childIds.isEmpty()) return null;

        Float price;
        if (priceValue==null){
            price=isMin?productRepo.findMinPrice(childIds):productRepo.findMaxPrice(childIds);
        }else{
            price = priceValue;
        }

        return price;
    }

    public Map<String, Set<String>> getFilterBuilder( List<Integer> childIds) throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper();
        Map<String, Set<String>> filterBuilder= new HashMap<>();
        List<Product> allProducts = productRepo.findByCategoryIdIn(childIds);
        for(Product pr:allProducts){
            if(!Strings.isEmpty(pr.getDescription())){
                Map<String,String> decsObject = om.readValue(pr.getDescription(),HashMap.class);
                for(String key:decsObject.keySet()){
                    if (filterBuilder.get(key)==null){
                        filterBuilder.put(key,new HashSet<>());
                    }
                    filterBuilder.get(key).add(decsObject.get(key));
                }
            }
        }

        //Убираем фильтры с одним вариантом выбора
        Iterator<String> itr = filterBuilder.keySet().iterator();
        while (itr.hasNext()){
            if(filterBuilder.get(itr.next()).size()<2){
                itr.remove();
            }
        }
        return filterBuilder;
    }

    public Map<String,List<String>> getFiltersForRepo(Map<String, Set<String>> filterBuilder,MultiValueMap<String,String> form){
        Map<String,List<String>> filtersForRepo = new HashMap<>();
        for (String filter:filterBuilder.keySet()){
            if (form.get(filter)!=null){
                filtersForRepo.put(filter,form.get(filter));
            }
        }
        return filtersForRepo;
    }

    public List<String> getFiltersCheckedForModel(Map<String,List<String>> filtersForRepo){
        List<String> filtersCheckedForModel = new ArrayList<>();
        for(String filter:filtersForRepo.keySet()){
            for(String value:filtersForRepo.get(filter))
            filtersCheckedForModel.add(filter+"="+value);
        }
        return filtersCheckedForModel;
    }

    public Page<Product> findByDescContainStrings(Pageable pageable, float priceFrom, float priceTo, Map<String, List<String>> filtersForRepo, List<Integer> childIds) {
        return productRepo.findByDescContainStrings(pageable,priceFrom,priceTo,filtersForRepo,childIds);
    }

    public Page<Product> findByPriceBetweenAndCategoryIdIn(Pageable pageable, float priceFrom, float priceTo, List<Integer> childIds) {
        return productRepo.findByPriceBetweenAndCategoryIdIn(pageable,priceFrom,priceTo,childIds);
    }

    public List<Product> findAdviceProducts(Category category, long id) {
        return productRepo.findFirst4ByCategoryAndIdNot(category,id);
    }

    public void save(Product product) {
        productRepo.save(product);
    }

    public Product findById(Long id) {
        return productRepo.findById(id).orElse(null);
    }

    public void deleteById(Long id) {
        productRepo.deleteById(id);
    }
}
