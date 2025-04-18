# ChototTravel

ChototTravel là một ứng dụng Spring Boot được thiết kế để quản lý homestay và theo dõi lượt truy cập của người dùng.

## Mục Lục

- [Giới Thiệu](#giới-thiệu)
- [Cài Đặt](#cài-đặt)
- [Cấu Trúc Dự Án](#cấu-trúc-dự-án)
- [Tính Năng](#tính-năng)
- [Sử Dụng](#sử-dụng)
- [API Endpoints](#api-endpoints)
- [Bảo Mật](#bảo-mật)
- [Liên Hệ](#liên-hệ)

---

## Giới Thiệu

ChototTravel là một nền tảng quản lý homestay hiện đại, cung cấp các tính năng:
- Quản lý homestay với đầy đủ thông tin và hình ảnh
- Theo dõi tổng số lượt truy cập bằng cách sử dụng `VisitCounterService`
- Hỗ trợ xác thực người dùng bằng JWT (JSON Web Token)
- Quản lý đặt phòng và thanh toán
- Đánh giá và phản hồi từ khách hàng

---

## Cài Đặt

### Yêu Cầu Hệ Thống
- **Java**: Phiên bản 17 hoặc mới hơn
- **Maven**: Phiên bản 3.8 hoặc mới hơn
- **Cơ sở dữ liệu**: MySQL hoặc H2 (tùy chỉnh trong `application.properties`)

### Các Bước Cài Đặt
1. Clone repository:
```bash
git clone https://github.com/your-username/chotottravel.git
cd chotottravel
```

2. Cấu hình cơ sở dữ liệu trong `src/main/resources/application.properties`

3. Build và chạy ứng dụng:
```bash
mvn clean install
mvn spring-boot:run
```

---

## Cấu Trúc Dự Án

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── chotottravel/
│   │           ├── controller/    # API Controllers
│   │           ├── model/         # Entity classes
│   │           ├── repository/    # JPA Repositories
│   │           ├── service/       # Business logic
|   |           ├── security/
│   └── resources/
│       ├── static/               # Static resources
│       └── templates/            # Thymeleaf templates
```

---

## Tính Năng

### Quản Lý Homestay
- Thêm, sửa, xóa thông tin homestay
- Quản lý hình ảnh và mô tả
- Quản lý giá cả và khuyến mãi
- Theo dõi trạng thái phòng

### Quản Lý Người Dùng
- Đăng ký và đăng nhập
- Phân quyền người dùng
- Quản lý thông tin cá nhân
- Lịch sử đặt phòng

### Đặt Phòng
- Tìm kiếm và lọc homestay
- Đặt phòng trực tuyến
- Thanh toán an toàn
- Hủy đặt phòng

### Thống Kê
- Theo dõi lượt truy cập
- Báo cáo doanh thu
- Phân tích đánh giá
- Thống kê người dùng

---

## API Endpoints

### Authentication
- `POST /api/auth/register` - Đăng ký tài khoản
- `POST /api/auth/login` - Đăng nhập
- `POST /api/auth/refresh-token` - Làm mới token

### Homestay
- `GET /api/homestays` - Lấy danh sách homestay
- `GET /api/homestays/{id}` - Lấy thông tin chi tiết homestay
- `POST /api/homestays` - Thêm homestay mới
- `PUT /api/homestays/{id}` - Cập nhật thông tin homestay
- `DELETE /api/homestays/{id}` - Xóa homestay

### Booking
- `POST /api/bookings` - Tạo đặt phòng mới
- `GET /api/bookings/{id}` - Lấy thông tin đặt phòng
- `PUT /api/bookings/{id}/cancel` - Hủy đặt phòng

---

## Bảo Mật

- Xác thực JWT
- Mã hóa mật khẩu
- CSRF Protection
- Rate Limiting
- Input Validation

---

## Liên Hệ

- Email: support@chotottravel.com
- Website: https://chotottravel.com
- Hotline: 1900-xxxx

