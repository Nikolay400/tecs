package com.kolya.tecs.controller;

import com.kolya.tecs.model.Category;
import com.kolya.tecs.model.Product;
import com.kolya.tecs.service.CategoryService;
import com.kolya.tecs.service.FileService;
import com.kolya.tecs.service.ProductService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private FileService fileService;

    @GetMapping("/product")
    public String products(Model model){
        List<Product> products = productService.findAllByOrderById();
        model.addAttribute("products",products);
        return "adminProducts";
    }

    @GetMapping("/product/{id}")
    public String product(@PathVariable("id") Product product, Model model){
        List<Category> categories = categoryService.findAll();
        model.addAttribute("product",product);
        model.addAttribute("categories",categories);
        return "product";
    }

    @PostMapping("/product/{id}")
    public String updateProduct(@PathVariable("id") Product oldProduct,
                                @ModelAttribute Product newProduct
    ){
        productService.save(newProduct);
        return "redirect:/admin/product";
    }

    @GetMapping("/product/new")
    public String newProduct(Model model){
        Product product = new Product();
        List<Category> categories = categoryService.findAll();
        model.addAttribute("product",product);
        model.addAttribute("categories",categories);
        return "newProduct";
    }


    @PostMapping("/product")
    public String addProduct(@ModelAttribute Product newProduct
    ){
        productService.save(newProduct);
        return "redirect:/admin/product";
    }

    @GetMapping("/product/{id}/delete")
    public String deleteProduct(@PathVariable("id") Long id){
        productService.deleteById(id);
        return "redirect:/admin/product";
    }


    @RequestMapping(value = "/file",method = GET, produces = "application/text")
    @ResponseBody
    public String getImage(HttpServletResponse response) throws IOException {

        List<Product> products = productService.findAllByOrderById();
        String resultCsv = fileService.getCsvResult(products);

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "inline; filename=products.csv");
        response.setHeader("Content-Length", String.valueOf(resultCsv.length()));
        return resultCsv;
    }


    @PostMapping("/newCsv")
    public String newCsv(@RequestParam("csvFile") MultipartFile multipartFile,
                         @RequestParam("imgs") MultipartFile imgsZip) throws IOException {
        fileService.newCsvSave(multipartFile);
        fileService.imgsSave(imgsZip);
        return "redirect:/admin/product";
    }



    @ModelAttribute("catalog")
    private Map<Integer, CategoryService.CategoryInfo> catalogs(){
        List<Category> cats = categoryService.getCats();
        Map<Integer, List<Integer>> catTree = categoryService.getCatTree(cats);
        Map<Integer, CategoryService.CategoryInfo> catalog = categoryService.createCatalog(cats,catTree);
        return catalog;
    }
}

