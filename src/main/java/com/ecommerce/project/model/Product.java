package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="products")
@ToString
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @ManyToOne
    @JoinColumn(name="user_id/seller_id")
    private  User user;

    @NotBlank
    @Size(min=5,message =" Product name must contain atleast 5 characters")
    private String productName;

    @NotBlank
    @Size(min=5,message =" Product description must contain atleast 5 characters")
    private String description;

    private Integer quantity;
    private double price;
    private Double specialPrice;
    private double discount;
    private String image;


    @ManyToOne
    @JoinColumn(name="category_id")
    private Category category;

    @OneToMany(mappedBy = "product",cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    private List<CartItem> cartItemList=new ArrayList<>();
}
