package com.kolya.tecs.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "ordr")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private Date date;
    private String file;
    private String status;

    public Order() {
    }

    public Order(Date date, String file, String status) {
        this.date = date;
        this.file = file;
        this.status = status;
    }
}
