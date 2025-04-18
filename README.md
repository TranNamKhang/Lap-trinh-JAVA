# ChototTravel

ChototTravel là một ứng dụng Spring Boot được thiết kế để quản lý homestay và theo dõi lượt truy cập của người dùng.

## Mục Lục

- [Giới Thiệu](#giới-thiệu)
- [Cài Đặt](#cài-đặt)
- [Cấu Trúc Dự Án](#cấu-trúc-dự-án)
- [Tính Năng](#tính-năng)
- [Sử Dụng](#sử-dụng)
- [Liên Hệ](#liên-hệ)

---

## Giới Thiệu

Ứng dụng này cung cấp các tính năng như:
- Quản lý homestay.
- Theo dõi tổng số lượt truy cập bằng cách sử dụng `VisitCounterService`.
- Hỗ trợ xác thực người dùng bằng JWT (JSON Web Token).

---

## Cài Đặt

### Yêu Cầu Hệ Thống
- **Java**: Phiên bản 17 hoặc mới hơn.
- **Maven**: Phiên bản 3.8 hoặc mới hơn.
- **Cơ sở dữ liệu**: MySQL hoặc H2 (tùy chỉnh trong `application.properties`).

