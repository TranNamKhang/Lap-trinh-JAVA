CREATE DATABASE ChoTotTravel;
USE ChoTotTravel;

-- Bảng Người Dùng (Guest & Customer)
CREATE TABLE Users (
    UserID INT AUTO_INCREMENT PRIMARY KEY,
    FullName VARCHAR(100) NOT NULL,
    Email VARCHAR(100) UNIQUE NOT NULL,
    PhoneNumber VARCHAR(15) UNIQUE NOT NULL,
    PasswordHash VARCHAR(255) NOT NULL,
    UserType ENUM('Guest', 'Customer') NOT NULL
);

-- Bảng Chủ Homestay/Camping
CREATE TABLE Owners (
    OwnerID INT AUTO_INCREMENT PRIMARY KEY,
    FullName VARCHAR(100) NOT NULL,
    Email VARCHAR(100) UNIQUE NOT NULL,
    PhoneNumber VARCHAR(15) UNIQUE NOT NULL,
    PasswordHash VARCHAR(255) NOT NULL
);

-- Bảng Homestay/Camping
CREATE TABLE Accommodations (
    AccommodationID INT AUTO_INCREMENT PRIMARY KEY,
    OwnerID INT NOT NULL,
    Name VARCHAR(100) NOT NULL,
    Description TEXT,
    Location VARCHAR(255) NOT NULL,
    PricePerNight DECIMAL(10,2) NOT NULL,
    Capacity INT NOT NULL,
    ImageURL VARCHAR(255),
    FOREIGN KEY (OwnerID) REFERENCES Owners(OwnerID) ON DELETE CASCADE
);

-- Bảng Dịch Vụ (Ăn uống, giải trí, v.v.)
CREATE TABLE Services (
    ServiceID INT AUTO_INCREMENT PRIMARY KEY,
    AccommodationID INT NOT NULL,
    Name VARCHAR(100) NOT NULL,
    Description TEXT,
    Price DECIMAL(10,2) NOT NULL,
    ServiceType ENUM('Meal', 'Drink', 'Entertainment') NOT NULL,
    FOREIGN KEY (AccommodationID) REFERENCES Accommodations(AccommodationID) ON DELETE CASCADE
);

-- Bảng Đặt Chỗ
CREATE TABLE Bookings (
    BookingID INT AUTO_INCREMENT PRIMARY KEY,
    UserID INT NOT NULL,
    AccommodationID INT NOT NULL,
    CheckIn DATE NOT NULL,
    CheckOut DATE NOT NULL,
    TotalPrice DECIMAL(10,2) NOT NULL,
    Status ENUM('Pending', 'Confirmed', 'Cancelled') NOT NULL,
    FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE,
    FOREIGN KEY (AccommodationID) REFERENCES Accommodations(AccommodationID) ON DELETE CASCADE
);

-- Bảng Đặt Dịch Vụ
CREATE TABLE BookingServices (
    BookingServiceID INT AUTO_INCREMENT PRIMARY KEY,
    BookingID INT NOT NULL,
    ServiceID INT NOT NULL,
    Quantity INT NOT NULL,
    TotalPrice DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (BookingID) REFERENCES Bookings(BookingID) ON DELETE CASCADE,
    FOREIGN KEY (ServiceID) REFERENCES Services(ServiceID) ON DELETE CASCADE
);

-- Bảng Đánh Giá & Phản Hồi
CREATE TABLE Reviews (
    ReviewID INT AUTO_INCREMENT PRIMARY KEY,
    UserID INT NOT NULL,
    AccommodationID INT NOT NULL,
    Rating INT CHECK (Rating BETWEEN 1 AND 5),
    Comment TEXT,
    ReviewDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE,
    FOREIGN KEY (AccommodationID) REFERENCES Accommodations(AccommodationID) ON DELETE CASCADE
);


