package com.homestay.models;

import jakarta.persistence.*;
import java.util.List;

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
    private double pricePerNight;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "homestay", cascade = CascadeType.ALL)
    private List<Booking> bookings;

    // Thuộc tính lưu ảnh
    private String image;

    // Constructor không tham số
    public Homestay() {}

    // Constructor đầy đủ tham số
    public Homestay(Long id, String name, String address, String location, String description, double pricePerNight, User owner, List<Booking> bookings, String image) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.location = location;
        this.description = description;
        this.pricePerNight = pricePerNight;
        this.owner = owner;
        this.bookings = bookings;
        this.image = image;
    }

    // Getter và Setter
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

    public double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
