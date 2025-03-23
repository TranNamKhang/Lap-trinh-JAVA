package com.homestay.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "homestays")
public class Homestay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address; 
    private String location;
    private String description;

    @Column(precision = 15, scale = 2)
    private BigDecimal pricePerNight;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    @JsonIgnore // Tránh vòng lặp JSON khi trả về dữ liệu
    private User owner;

    private String image;

    public Homestay() {}

    public Homestay(Long id, String name, String address, String location, String description, BigDecimal pricePerNight, User owner, String image) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.location = location;
        this.description = description;
        this.pricePerNight = pricePerNight;
        this.owner = owner;
        this.image = image;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
}
