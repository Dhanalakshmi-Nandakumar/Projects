package com.ecommerce.project.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long productId;

    @NotBlank
    @Size(min=5,message =" Product name must contain atleast 5 characters")
    private String productName;

    private String image;
    private Integer quantity;
    @NotBlank
    @Size(min=5,message =" Product description must contain atleast 5 characters")
    private String description;

    private double price;
    private double specialPrice;
    private double discount;
}
