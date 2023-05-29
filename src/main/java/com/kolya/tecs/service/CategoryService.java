package com.kolya.tecs.service;

import com.kolya.tecs.model.Category;
import com.kolya.tecs.repos.CategoryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CategoryService {


    @Autowired
    private CategoryRepo categoryRepo;

    public List<Integer> childIds(int id, Map<Integer,List<Integer>> catTree){
        List<Integer> childIds = catTree.get(id);
        for (Integer catId:childIds){
            if (catTree.get(catId)!=null){
                childIds.addAll(childIds(catId,catTree));
            }
        }
        return childIds;
    }


    public Map<Integer,CategoryInfo> createCatalog(List<Category> cats, Map<Integer,List<Integer>> catTree){

        Map<Integer,CategoryInfo> catalog = new LinkedHashMap<>();

        Map<Integer,Category> mapCats = cats.stream().collect(Collectors.toMap(c->c.getId(), c->c));
        loop(0,0,"",new ArrayList<>(),mapCats,catalog,catTree);
        return catalog;
    }

    private void loop(int id,int depth,String alias,  List<Integer> breadcrumb,
                             Map<Integer,Category> cats,
                             Map<Integer,CategoryInfo> catalog,
                             Map<Integer,List<Integer>> catTree
    ){
        List<Integer> childIds = catTree.get(id);

        if (childIds!=null){
            for(Integer childId:childIds){
                Category category = cats.get(childId);
                String thisAlias = alias+"/"+category.getAlias();
                List<Integer> thisBreadcrumb = new ArrayList<>(breadcrumb);
                thisBreadcrumb.add(childId);
                catalog.put(category.getId(),new CategoryInfo(category.getId(),category.getName(),thisAlias,depth,thisBreadcrumb));
                loop(childId,depth+1,thisAlias,thisBreadcrumb,cats,catalog,catTree);
            }
        }
    }


    public List<Category> getCats(){
        return categoryRepo.findByOrderByOrdrAsc();
    }

    public Category getThisCat(String alias){
        return categoryRepo.findByAlias(alias);
    }

    public Map<Integer,List<Integer>> getCatTree(List<Category> cats){
        Map<Integer,List<Integer>> catTree = new HashMap<>();
        for (Category cat:cats){
            int parentId = cat.getParent();
            if (catTree.get(parentId)==null) {
                catTree.put(parentId, new ArrayList<>());
            }
            catTree.get(parentId).add(cat.getId());
        }
        return catTree;
    }

    public List<Integer> getChildIds(int thisCatId,Map<Integer, List<Integer>> catTree){
        List<Integer> childIds = new ArrayList<>();
        if (catTree.get(thisCatId)!=null){
            for (Integer val:catTree.get(thisCatId)){
                childIds.addAll(getChildIds(val,catTree));
            }

        }else{
            childIds = List.of(thisCatId);
        }

        return childIds;
    }

    public Category findByAlias(String alias) {
        return categoryRepo.findByAlias(alias);
    }

    public Category findById(int categoryId) {
        return categoryRepo.findById(categoryId).orElse(null);
    }

    public List<Category> findAll() {
        return categoryRepo.findAll();
    }


    public class CategoryInfo{
        private int id;
        private String name;
        private String alias;
        private int depth;

        private List<Integer> breadcrumb;

        public CategoryInfo(int id,String name, String alias, int depth,List<Integer> breadcrumb) {
            this.id = id;
            this.name = name;
            this.alias = alias;
            this.depth = depth;
            this.breadcrumb = breadcrumb;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

        public int getDepth() {
            return depth;
        }

        public void setDepth(int depth) {
            this.depth = depth;
        }

        public List<Integer> getBreadcrumb() {
            return breadcrumb;
        }

        public void setBreadcrumb(List<Integer> breadcrumb) {
            this.breadcrumb = breadcrumb;
        }
    }
}
