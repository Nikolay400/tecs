package com.kolya.tecs.model;

import lombok.Data;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
@Data
public class Category implements Comparable<Category>{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private int  parent;
    private String alias;
    private int ordr;



    @Override
    public int compareTo(Category o) {
        return ordr-o.getOrdr();
    }
}
