package com.homestay.models;

import jakarta.persistence.*;

@Entity
public class Category {
    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;

    //Constructors
    public Category() {}
    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }

    //Getter và Setter
    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
