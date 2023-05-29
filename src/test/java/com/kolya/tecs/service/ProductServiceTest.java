package com.kolya.tecs.service;

import com.kolya.tecs.model.Product;
import com.kolya.tecs.repos.ProductRepo;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @MockBean
    private ProductRepo productRepo;

    @Test
    void getProductsMain_withEmptyOrNullSearchString() {
        String[] search = {"",null};
        PageRequest pageable = PageRequest.of(1,1);
        productService.getProductsMain(pageable,search[0]);
        productService.getProductsMain(pageable,search[1]);
        Mockito.verify(productRepo,Mockito.times(2)).findAll(pageable);
    }

    @Test
    void getProductsMain_withNonEmptySearchString() {
        String search = "Hello";
        PageRequest pageable = PageRequest.of(1,1);
        productService.getProductsMain(pageable,search);
        Mockito.verify(productRepo,Mockito.times(1)).findByNameContaining(pageable,search);
    }

    @Test
    void getPaginationNumbers() {
        int[] pageNowArr =   {0,0,0, 0, 9, 4, 0, 0};
        int[] totalPageArr = {1,4,9,10,10,10,15,88};
        List[] results = new List[]{
                List.of(1),                         //0-1
                List.of(1, 2, 3, 4),                    //0-4
                List.of(1, 2, 3, 4, 5, 6, 7, 8, 9),     //0-9
                List.of(1, 2, 3, 4, 5, 6, 7, -1, 10),   //0-10
                List.of(1, -1, 4, 5, 6, 7, 8, 9, 10),   //9-10
                List.of(1, 2, 3, 4, 5, 6, 7, -1, 10),
                List.of(1, 2, 3, 4, 5, 6, 7, -1, 15),
                List.of(1, 2, 3, 4, 5, 6, 7, -1, 88)
        };

        for (int i=0;i<pageNowArr.length; i++){
            List<Integer> pageNumbers =  productService.getPaginationNumbers(pageNowArr[i],totalPageArr[i]);
            assertEquals(pageNumbers,results[i]);
        }
    }

    @Test
    void getDescriptionsForModel() {
        Product product1 = new Product();
        Product product2 = new Product();
        Product product3 = new Product();
        product1.setId(1);
        product2.setId(2);
        product3.setId(3);
        product1.setDescription("{\"key1\":\"value1\", \"key2\":\"value2\"}");
        product2.setDescription("{\"key1\":\"value1\", \"key2\":\"value2\", \"key3\":\"value3\", \"key4\":\"value4\"}");
        product3.setDescription("{\"key1\":\"value1\", \"key2\":\"value1\", \"key3\":\"value1\", \"key4\":\"value1\", \"key5\":\"value1\"}");

        List<Product> products= List.of(
                product1,
                product2,
                product3
        );

        List<String>[] resStrings = new List[]{
            List.of("key1: value1","key2: value2"),
            List.of("key1: value1","key2: value2","key3: value3","key4: value4"),
            List.of("key1: value1","key2: value1","key3: value1","key4: value1")
        };

        for (Product pr:products) {
            Map<Long,List<String>> methodResult = productService.getDescriptionsForModel(products);
            assertNotNull(methodResult);
            assertNotNull(methodResult.get(1L));
            assertNotNull(methodResult.get(2L));
            assertNotNull(methodResult.get(3L));
            assertEquals(resStrings[0],methodResult.get(1L));
            assertEquals(resStrings[1],methodResult.get(2L));
            assertEquals(resStrings[2],methodResult.get(3L));
        }

    }

    @Test
    void getPrice_shouldReturnNull() {
        boolean[] isMins = {true,true,false,false};
        Float priceValue = 1000f;
        List[] childIds = new List[]{
            List.of(),
            null,
            List.of(),
            null
        };
        for (int i=0;i<isMins.length;i++){
            Float methodResult = productService.getPrice(isMins[i], priceValue, childIds[i]);
            assertNull(methodResult);
            Mockito.verify(productRepo,Mockito.times(0)).findMaxPrice(ArgumentMatchers.any());
            Mockito.verify(productRepo,Mockito.times(0)).findMinPrice(ArgumentMatchers.any());
        }

    }

    @Test
    void getPrice_shouldReturnNotNull() {
        Float priceValue = null;
        List<Integer> childIds = List.of(1,5,8,3);

        Mockito.doReturn(90000f)
                .when(productRepo)
                .findMaxPrice(childIds);

        Mockito.doReturn(1000f)
                .when(productRepo)
                .findMinPrice(childIds);


        Float methodResultMin = productService.getPrice(true, priceValue, childIds);
        assertEquals(1000f,methodResultMin);
        Mockito.verify(productRepo,Mockito.times(1)).findMinPrice(childIds);
        Mockito.verify(productRepo,Mockito.times(0)).findMaxPrice(ArgumentMatchers.any());

        Mockito.clearInvocations(productRepo);

        Float methodResultMax = productService.getPrice(false, priceValue, childIds);
        assertEquals(90000f,methodResultMax);
        Mockito.verify(productRepo,Mockito.times(0)).findMinPrice(ArgumentMatchers.any());
        Mockito.verify(productRepo,Mockito.times(1)).findMaxPrice(childIds);

    }

    @Test
    void getFilterBuilder() {
    }

    @Test
    void getFiltersForRepo() {
    }

    @Test
    void getFiltersCheckedForModel() {
    }

    @Test
    void findByDescContainStrings() {
    }

    @Test
    void findByPriceBetweenAndCategoryIdIn() {
    }

    @Test
    void findAdviceProducts() {
    }

    @Test
    void save() {
    }

    @Test
    void findById() {
    }
}