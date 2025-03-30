package com.homestay.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Tên đăng nhập không được để trống.")
    @Size(min = 3, max = 20, message = "Tên đăng nhập phải có từ 3 đến 20 ký tự.")
    private String username;

    @Column(nullable = false)
    @NotBlank(message = "Mật khẩu không được để trống.")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự.")
    private String password;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Email không được để trống.")
    @Email(message = "Email không hợp lệ.")
    private String email;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Số điện thoại không được để trống.")
    @Size(min = 10, max = 10, message = "Số điện thoại phải có đúng 10 chữ số.")
    @Pattern(regexp = "^[0-9]{10}$", message = "Số điện thoại chỉ được chứa các chữ số và có 10 ký tự.")
    private String phone;

    @Enumerated(EnumType.STRING)
    private Role role;

    public User() {}

    public User(String username, String password, String email, String phone, Role role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
