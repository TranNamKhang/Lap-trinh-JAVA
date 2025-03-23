package com.homestay.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Entity
@Table(name = "homestays")
public class Homestay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address; // Địa chỉ
    private String location;
    private String description;

    @Column(precision = 15, scale = 2) // Định dạng tiền tệ VNĐ
    private BigDecimal pricePerNight;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;


    private String image;
    @Transient
    private String formattedPrice;

    public Homestay() {}

    public Homestay(Long id, String name, String address, String location, String description, BigDecimal pricePerNight, User owner,  String image) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.location = location;
        this.description = description;
        this.pricePerNight = pricePerNight;
        this.owner = owner;
        this.image = image;
        this.formattedPrice = formatPrice(pricePerNight);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
        this.formattedPrice = formatPrice(pricePerNight);
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getFormattedPrice() {
        return formattedPrice;
    }

    public void setFormattedPrice(String formattedPrice) {
        this.formattedPrice = formattedPrice;
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) return "0 VNĐ";
        NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        return currencyFormat.format(price) + " VNĐ";
    }
}
