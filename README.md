# BÀI TẬP THỰC HÀNH: VÍ DỤ 1 & VÍ DỤ 2 (SPRING BOOT)

Dự án xây dựng ứng dụng web quản lý sản phẩm (Product Management) sử dụng kiến trúc 3 lớp chuẩn trong **Spring Boot**, kết nối cơ sở dữ liệu **Microsoft SQL Server**, sử dụng **DTO Pattern & Mapper**, giao diện **Thymeleaf & Thymeleaf Layout Dialect (Bootstrap 5)**.

Dự án hoàn thành đầy đủ yêu cầu của **Ví dụ 1** và **Ví dụ 2** trong tài liệu hướng dẫn:
- **Ví dụ 1**: Xây dựng chức năng CRUD (Thêm, Xem, Sửa, Xóa) có tìm kiếm và phân trang cho bảng `products`.
- **Ví dụ 2**: Thêm thuộc tính `images`, cấu hình Upload và hiển thị hình ảnh sản phẩm từ thư mục `uploads/products/`.

---

## 🛠 Công nghệ sử dụng

- **Java**: Java 17 / 21
- **Framework**: Spring Boot (Spring Web MVC, Spring Data JPA, Validation)
- **Database**: Microsoft SQL Server
- **Template Engine**: Thymeleaf, Thymeleaf Layout Dialect
- **CSS Framework**: Bootstrap 5
- **Tiện ích**: Project Lombok, MapStruct / Manual Component Mapper
- **Build Tool**: Maven (`mvnw`)

---

## 🌟 Các tính năng chính

### 1. Ví dụ 1: CRUD Sản phẩm + Tìm kiếm + Phân trang
- **Kiến trúc 3 lớp & DTO**: 
  - Tách bạch giữa dữ liệu cơ sở dữ liệu (`Product` Entity) và đối tượng trao đổi với tầng giao diện (`ProductDTO`).
  - Sử dụng `ProductMapper` để chuyển đổi qua lại an toàn giữa Entity và DTO.
- **Validation**:
  - Kiểm tra tính hợp lệ dữ liệu ngay tại DTO (`@NotBlank`, `@NotNull`, `@DecimalMin`, `@Min`, `@Size`).
- **Tìm kiếm**:
  - Tìm kiếm sản phẩm theo tên không phân biệt hoa thường (`findByNameContainingIgnoreCase`).
- **Phân trang**:
  - Tích hợp `Pageable` và `PageRequest`, cho phép tùy chọn hiển thị `5`, `10` hoặc `20` sản phẩm trên mỗi trang.

### 2. Ví dụ 2: Quản lý & Upload hình ảnh sản phẩm
- **Cấu trúc lưu trữ**:
  - Cơ sở dữ liệu SQL Server chỉ lưu đường dẫn / tên file (`images`), không lưu binary.
  - File hình ảnh thực tế được lưu vào thư mục vật lý `uploads/products/`.
- **Tên file an toàn & Không trùng lặp**:
  - Tên file khi tải lên được sinh tự động bằng mã `UUID` kết hợp định dạng gốc (`.jpg`, `.png`, `.webp`...).
- **Cấu hình WebConfig Resource Handler**:
  - Cấu hình ánh xạ đường dẫn URL `/uploads/**` tới thư mục file cục bộ `file:uploads/`.
- **Quản lý vòng đời ảnh thông minh**:
  - Khi cập nhật sản phẩm: nếu người dùng không chọn ảnh mới thì giữ nguyên ảnh cũ; nếu chọn ảnh mới thì tải lên ảnh mới và tự động xóa ảnh cũ khỏi ổ cứng.
  - Khi xóa sản phẩm: tự động xóa luôn file ảnh vật lý tương ứng trên hệ thống.
  - Xử lý kiểm tra an toàn đường dẫn (chống tấn công *Path Traversal*).

---

## 📂 Cấu trúc thư mục dự án

```text
springboot1-7/
├── pom.xml                                      # Cấu hình Maven & dependencies
├── uploads/                                     # Thư mục lưu trữ hình ảnh upload
│   └── products/
└── src/
    ├── main/
    │   ├── java/vn/iotstar/
    │   │   ├── Springboot17Application.java     # Class chạy chính & cấu hình UTF-8 Filter
    │   │   ├── configs/
    │   │   │   └── WebConfig.java               # Cấu hình Static Resource Handler (/uploads/**)
    │   │   ├── controllers/
    │   │   │   ├── HomeController.java          # Điều hướng "/" -> "/products"
    │   │   │   └── ProductController.java       # Controller xử lý CRUD, search & pagination
    │   │   ├── dto/
    │   │   │   └── ProductDTO.java              # Data Transfer Object + MultipartFile image
    │   │   ├── entity/
    │   │   │   └── Product.java                 # Entity ánh xạ bảng "products"
    │   │   ├── mapper/
    │   │   │   └── ProductMapper.java           # Chuyển đổi giữa Product Entity và ProductDTO
    │   │   ├── repository/
    │   │   │   └── ProductRepository.java       # Spring Data JPA Repository
    │   │   └── services/
    │   │       ├── ProductService.java          # Interface Service
    │   │       └── impl/
    │   │           └── ProductServiceImpl.java  # Triển khai nghiệp vụ CRUD & xử lý file ảnh
    │   └── resources/
    │       ├── application.properties           # Cấu hình DataSource, JPA, Port, Multipart
    │       └── templates/
    │           ├── layouts/
    │           │   └── layout.html              # Layout Thymeleaf tổng thể
    │           ├── fragments/
    │           │   ├── header.html              # Thanh menu Header
    │           │   └── footer.html              # Chân trang Footer
    │           └── products/
    │               ├── list.html                # Giao diện danh sách sản phẩm + ảnh + tìm kiếm + phân trang
    │               └── form.html                # Form thêm mới / cập nhật sản phẩm + upload ảnh
    └── test/
```

---

## 🚀 Hướng dẫn cài đặt và khởi chạy

### Bước 1: Chuẩn bị Cơ sở dữ liệu (SQL Server)
Mở SQL Server Management Studio (SSMS) hoặc `sqlcmd` và tạo cơ sở dữ liệu `webst2`:
```sql
CREATE DATABASE webst2;
GO
```

### Bước 2: Cấu hình `application.properties`
Mở file `src/main/resources/application.properties` và điều chỉnh lại thông tin tài khoản SQL Server của bạn (nếu có thay đổi):
```properties
server.port=8099

# Cấu hình kết nối SQL Server
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=webst2;encrypt=false;trustServerCertificate=true;sslProtocol=TLSv1.2;characterEncoding=UTF-8
spring.datasource.username=sa
spring.datasource.password=123
spring.datasource.driverClassName=com.microsoft.sqlserver.jdbc.SQLServerDriver

# Hibernate DDL Auto (tự động tạo và cập nhật bảng products)
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.format_sql=true

# Giới hạn kích thước upload file
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=10MB
```

### Bước 3: Khởi chạy dự án
Bạn có thể chạy dự án thông qua IDE (IntelliJ IDEA / Eclipse / VS Code) bằng cách Run file `Springboot17Application.java`, hoặc qua terminal:
```powershell
# Chạy với Maven Wrapper
.\mvnw.cmd spring-boot:run
```

### Bước 4: Trải nghiệm ứng dụng
Mở trình duyệt web và truy cập vào địa chỉ:
👉 **[http://localhost:8099/products](http://localhost:8099/products)**

---

## 📡 Danh sách URL / Endpoints

| Phương thức | Đường dẫn URL | Mô tả |
| :--- | :--- | :--- |
| `GET` | `/` | Chuyển hướng tự động về danh sách `/products` |
| `GET` | `/products` | Hiển thị danh sách sản phẩm, lọc theo `keyword`, chọn `size` và chuyển trang |
| `GET` | `/products/create` | Hiển thị form thêm mới sản phẩm |
| `POST` | `/products/create` | Nhận dữ liệu form (kèm upload hình ảnh) và tạo mới sản phẩm |
| `GET` | `/products/edit/{id}` | Hiển thị form chỉnh sửa sản phẩm kèm hình ảnh hiện tại |
| `POST` | `/products/edit/{id}` | Cập nhật thông tin sản phẩm và thay đổi hình ảnh (nếu có) |
| `GET` | `/products/delete/{id}` | Xóa sản phẩm khỏi database và xóa file ảnh vật lý trên ổ đĩa |
| `GET` | `/uploads/**` | Xem các file hình ảnh đã upload trên trình duyệt |
