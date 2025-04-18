package com.homestay.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "homestays")
public class Homestay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;
    private String location;
    private String province;

    @Lob
    private String description;

    @ElementCollection
    @CollectionTable(name = "homestay_images", joinColumns = @JoinColumn(name = "homestay_id"))
    @Column(name = "image_url")
    private List<String> extraImages = new ArrayList<>();

    @Column(precision = 15, scale = 2)
    private BigDecimal pricePerNight;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    @JsonIgnore
    private User owner;

    private String image;
    
    private String ownerName;
    private String ownerPhone;

    @OneToMany(mappedBy = "homestay", cascade = CascadeType.ALL)
    private List<Review> reviews = new ArrayList<>();

    public Homestay() {}

    public Homestay(Long id, String name, String address, String location, String description, BigDecimal pricePerNight, User owner, String image, String ownerName, String ownerPhone) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.location = location;
        this.description = description;
        this.pricePerNight = pricePerNight;
        this.owner = owner;
        this.image = image;
        this.ownerName = ownerName;
        this.ownerPhone = ownerPhone;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

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

    public List<String> getExtraImages() { return extraImages; }
    public void setExtraImages(List<String> extraImages) { this.extraImages = extraImages; }

    public double getPrice() {
        return pricePerNight.doubleValue();
    }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getOwnerPhone() { return ownerPhone; }
    public void setOwnerPhone(String ownerPhone) { this.ownerPhone = ownerPhone; }

    public List<Review> getReviews() { return reviews; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }
}
