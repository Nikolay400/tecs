package com.kolya.tecs.model;

import com.kolya.tecs.customAnnotation.Phone;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Buyer {
    @NotBlank(message = "Name can't be empty!")
    private String name;
    @NotBlank(message = "Surname can't be empty!")
    private String surname;
    @Email(message = "This isn't email!")
    @NotBlank(message = "Email can't be empty!")
    private String email;
    @Phone
    private String phone;
    @NotBlank(message = "Address can't be empty!")
    private String address;
}
