document.addEventListener("DOMContentLoaded", function() {
    // Tạo phần tử thông báo
    let welcomeMessage = document.createElement("div");
    welcomeMessage.textContent = "Chào mừng bạn đến với Chỗ Tốt Travel!";
    welcomeMessage.style.position = "fixed";
    welcomeMessage.style.top = "20px";
    welcomeMessage.style.left = "50%";
    welcomeMessage.style.transform = "translateX(-50%)";
    welcomeMessage.style.backgroundColor = "#4CAF50";
    welcomeMessage.style.color = "white";
    welcomeMessage.style.padding = "10px 20px";
    welcomeMessage.style.borderRadius = "5px";
    welcomeMessage.style.boxShadow = "0px 4px 6px rgba(0, 0, 0, 0.1)";
    welcomeMessage.style.zIndex = "1000";
    welcomeMessage.style.fontSize = "16px";
    welcomeMessage.style.opacity = "1"; // Hiển thị ban đầu
    welcomeMessage.style.transition = "opacity 1s ease-out"; // Thêm hiệu ứng mờ dần

    document.body.appendChild(welcomeMessage);

    // Sau 3s, làm mờ dần thông báo
    setTimeout(function() {
        welcomeMessage.style.opacity = "0"; // Mờ dần
        setTimeout(() => {
            welcomeMessage.remove(); // Xóa khỏi DOM sau khi hiệu ứng kết thúc
        }, 1000);
    }, 3000);
});
