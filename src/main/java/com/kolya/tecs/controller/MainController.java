package com.kolya.tecs.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kolya.tecs.model.Cart;
import com.kolya.tecs.model.Category;
import com.kolya.tecs.model.Product;
import com.kolya.tecs.model.User;
import com.kolya.tecs.service.CartService;
import com.kolya.tecs.service.CategoryService;
import com.kolya.tecs.service.ProductService;
import com.kolya.tecs.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.*;

import static com.kolya.tecs.controller.utils.ControllerUtils.getPriceFromForm;

@Controller
public class MainController {

    @Autowired
    private CartService cartService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ProductService productService;
    @Autowired
    private UserService userService;


    @GetMapping("/")
    public String main(@AuthenticationPrincipal User user, Model model,
                       @PageableDefault(size = 9) Pageable pageable,
                       @RequestParam(name = "search",required = false) String search,
                       HttpSession session) {


        Page<Product> products = productService.getProductsMain(pageable,search);
        List<Category> cats = categoryService.getCats();
        Map<Integer, List<Integer>> catTree = categoryService.getCatTree(cats);
        Map<Integer, CategoryService.CategoryInfo> catalog = categoryService.createCatalog(cats,catTree);
        List<Integer> pageNumbers = productService.getPaginationNumbers(products.getNumber(),products.getTotalPages());
        Map<Long,List<String>> descs=  productService.getDescriptionsForModel(products.getContent());


        model.addAttribute("user",user);
        model.addAttribute("descs",descs);
        model.addAttribute("search",search);
        model.addAttribute("pageNumbers",pageNumbers);
        model.addAttribute("catalog",catalog);
        model.addAttribute("products",products);


        return "main";
    }



    @GetMapping(value = {"/{alias1:[a-z[-]]+}","/{alias1:[a-z[-]]+}/{alias2:[a-z[-]]+}"})
    public String catalog(@PathVariable("alias1") String alias1,
                          @PathVariable(value = "alias2",required = false) String alias2,
                          @AuthenticationPrincipal User user,
                          @PageableDefault(size = 9) Pageable pageable,
                          @RequestParam(required = false) MultiValueMap<String, String> form,
                          Model model
    ) throws JsonProcessingException {

        List<String> filtersCheckedForModel = new ArrayList<>();
        Map<String, Set<String>> filterBuilder = null;
        Page<Product> products =null;
        Map<Long,List<String>> descs = null;
        List<Integer> pageNumbers = null;

        List<Category> cats = categoryService.getCats();
        Map<Integer, List<Integer>> catTree = categoryService.getCatTree(cats);
        Map<Integer, CategoryService.CategoryInfo> catalog = categoryService.createCatalog(cats,catTree);

        String alias = alias2!=null?alias2:alias1;
        Category thisCat = categoryService.getThisCat(alias);

        List<Integer> childIds = categoryService.getChildIds(thisCat.getId(), catTree);
        Float priceFromValue = getPriceFromForm("priceFrom",form);
        Float priceToValue = getPriceFromForm("priceTo",form);

        Float priceFrom = productService.getPrice(true,priceFromValue, childIds);
        Float priceTo = productService.getPrice(false,priceToValue, childIds);

        if (priceFrom!=null){
            if (alias2!=null){
                filterBuilder = productService.getFilterBuilder(childIds);
                Map<String, List<String>> filtersForRepo = productService.getFiltersForRepo(filterBuilder, form);
                filtersCheckedForModel = productService.getFiltersCheckedForModel(filtersForRepo);
                products = productService.findByDescContainStrings(pageable,priceFrom,priceTo,filtersForRepo,childIds);
            }else{
                products = productService.findByPriceBetweenAndCategoryIdIn(pageable,priceFrom,priceTo,childIds);
            }

            descs=  productService.getDescriptionsForModel(products.getContent());
            pageNumbers = productService.getPaginationNumbers(products.getNumber(),products.getTotalPages());
        }


        model.addAttribute("filters",filterBuilder);
        model.addAttribute("catalog",catalog);
        model.addAttribute("descs",descs);
        model.addAttribute("priceFrom",priceFrom);
        model.addAttribute("priceTo",priceTo);
        model.addAttribute("filterChecked",filtersCheckedForModel);
        model.addAttribute("pageNumbers",pageNumbers);
        model.addAttribute("products",products);
        model.addAttribute("user",user);
        model.addAttribute("childIds",childIds);
        model.addAttribute("thisCat",thisCat);
        return "main";
    }




    @GetMapping("/**/{preId:[a-z[0-9[-]]]*id-}{id}")
    public String product(@AuthenticationPrincipal User user,
                          Model model, @PathVariable("id")
                          Product product) throws JsonProcessingException {

        List<Category> cats = categoryService.getCats();
        Map<Integer, List<Integer>> catTree = categoryService.getCatTree(cats);
        Map<Integer, CategoryService.CategoryInfo> catalog = categoryService.createCatalog(cats,catTree);
        Map<String,String> desc = new ObjectMapper().readValue(product.getDescription(),HashMap.class);
        List<Product> moreProducts = productService.findAdviceProducts(product.getCategory(),product.getId());

        Map<Long,List<String>> descs=  productService.getDescriptionsForModel(moreProducts);

        model.addAttribute("catalog",catalog);
        model.addAttribute("moreProducts",moreProducts);
        model.addAttribute("desc",desc);
        model.addAttribute("descs",descs);
        model.addAttribute("product",product);
        model.addAttribute("user",user);
        return "showProduct";
    }


    @PostMapping("/add-product-to-cart")
    @ResponseBody
    public String addProductToCart(@AuthenticationPrincipal User user,
                                   @RequestParam("productId") Product product,
                                   @RequestParam("quantity") int quantity,
                                   HttpSession session){
        if (user!=null){
            cartService.addProductToUserCart(product,quantity,user);
        }
        else{
            cartService.addProductToSessionCart(product,quantity,session);
        }
        return "ok";
    }

    @PostMapping("/del-cart")
    @ResponseBody
    public String delCart(@AuthenticationPrincipal User user,
                                   @RequestParam("productId") Product product,
                                   HttpSession session){
        if (user!=null){
            cartService.deleteByUserAndProduct(user,product);
        }
        else{
            Map<Long, Integer> sesCart = (Map<Long, Integer>) session.getAttribute("sesCart");
            sesCart.remove(product.getId());
            session.setAttribute("sesCart",sesCart);
        }
        return "ok";
    }

    @ModelAttribute("currentUrl")
    public String currentUrl(HttpServletRequest httpRequest){
        return  httpRequest.getRequestURI();
    }

    @ModelAttribute("urlBuilder")
    public ServletUriComponentsBuilder urlBuilder(){
        return ServletUriComponentsBuilder.fromCurrentRequest();
    }

    @ModelAttribute("sort")
    public String sort(@PageableDefault Pageable pageable,Model model){
        String sortUrl;
        String sort;
        switch(pageable.getSort().toString()) {
            case ("price: ASC"):
                sort = "Сначала дешевые";
                sortUrl="price,asc";
                break;
            case ("price: DESC"):
                sort = "Сначала дорогие";
                sortUrl="price,desc";
                break;
            default:
                sort = "Без сортировки";
                sortUrl=null;
                break;
        };
        model.addAttribute("sortUrl",sortUrl);
        return sort;
    }

    @ModelAttribute("cartMap")
    public Map<Long,Integer> cartMap(@AuthenticationPrincipal User user, HttpSession session, Model model){
        //model.addAttribute("cartQuantity",cartService.getCartQuantity(user, session));
        Map<Long, Integer> cartMap = new HashMap<>();
        if (user!=null){
            List<Cart> cartList = cartService.findByUser(user);
            for (Cart cart:cartList){
                cartMap.put(cart.getProduct().getId(),cart.getQuantity());
            }
        }
        else{
            Map<Long, Integer> sesCart = (Map<Long, Integer>) session.getAttribute("sesCart");
            if (sesCart!=null){
                cartMap=sesCart;
            }
        }
        return cartMap;
    }


}
