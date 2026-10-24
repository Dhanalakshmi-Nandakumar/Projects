package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ManyToAny;

import java.util.ArrayList;
import java.util.List;

@Entity
@ToString
@Table(name="addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    @NotBlank
    @Size(min=5,message = "Street name must be atleast 5 character")
    private String street;

    @NotBlank
    @Size(min=5,message = "Building name must be atleast 5 character")
    private String buildingName;

    @NotBlank
    @Size(min=4,message = "City name must be atleast 4 character")
    private String city;

    @NotBlank
    @Size(min=2,message = "state name must be atleast 2 character")
    private String state;

    @NotBlank
    @Size(min=2,message = "country name must be atleast 2 character")
    private String country;

    @NotBlank
    @Size(min=6,message = "pincode name must be atleast 6 character")
    private String pincode;

    public Address(Long addressId, String pincode, String country, String state, String city, String buildingName, String street) {
        this.addressId = addressId;
        this.pincode = pincode;
        this.country = country;
        this.state = state;
        this.city = city;
        this.buildingName = buildingName;
        this.street = street;
    }

    @ToString.Exclude
    @ManyToMany(mappedBy = "addresses")
    private List<User> user=new ArrayList<>();

}
