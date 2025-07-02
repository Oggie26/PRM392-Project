package com.example.prm391_project.response

import com.google.gson.annotations.SerializedName

// Đây là data class cho mỗi item trong mảng "items" của giỏ hàng
// Điều chỉnh các trường này để khớp chính xác với cấu trúc JSON của từng sản phẩm trong giỏ hàng từ API của bạn
// Dựa vào CartItem bạn đã có, tôi sẽ giả định các trường tương ứng.
// Nếu API trả về Product Object đầy đủ bên trong items, bạn cần định nghĩa nó ở đây.
data class CartItemDto(
    // Giả sử API trả về các trường sau cho mỗi item trong giỏ hàng.
    // Bạn cần kiểm tra lại JSON thực tế từ API để ánh xạ chính xác.
    // Ví dụ, API có thể trả về:
    // "product": { "id": "p1", "name": "Shirt", "image": "url", "price": 100.0 },
    // "quantity": 2
    // Nếu vậy, bạn sẽ cần một ProductDto và thuộc tính product: ProductDto
    val id: String?, // ID của sản phẩm trong giỏ hàng (hoặc ID của item trong API nếu có)
    val name: String?, // Tên sản phẩm
    val price: Double?, // Giá sản phẩm
    val imageUrl: String?, // URL hình ảnh sản phẩm
    val quantity: Int? // Số lượng sản phẩm
)

// Đây là data class cho đối tượng "result" của API giỏ hàng
data class CartResult(
    val cartId: Int?,
    val username: String?,
    val items: List<CartItemDto>?, // Danh sách các sản phẩm trong giỏ hàng
    val totalPrice: Double?
)

// Đây là response chung mà Retrofit sẽ nhận được
// Bạn đã có IResponse<T>, vậy nên CartResponse sẽ là T ở đây
// class CartResponse (không cần tạo file riêng nếu đã có IResponse)