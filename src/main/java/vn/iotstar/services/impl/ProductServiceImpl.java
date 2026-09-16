package vn.iotstar.services.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.entity.Product;
import vn.iotstar.mapper.ProductMapper;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.services.ProductService;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final Path uploadDir = Paths.get("uploads/products");

    // ==========================================
    // SEARCH + PAGINATION
    // ==========================================
    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findAll(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Product> products;
        if (keyword == null || keyword.isBlank()) {
            products = productRepository.findAll(pageable);
        } else {
            products = productRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable);
        }
        return products.map(productMapper::toDTO);
    }

    // ==========================================
    // FIND BY ID
    // ==========================================
    @Override
    @Transactional(readOnly = true)
    public ProductDTO findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + id));
        return productMapper.toDTO(product);
    }

    // ==========================================
    // CREATE
    // ==========================================
    @Override
    public ProductDTO create(ProductDTO dto) {
        try {
            String fileName = saveImage(dto.getImage());
            dto.setImages(fileName);
            Product product = productMapper.toEntity(dto);
            Product saved = productRepository.save(product);
            return productMapper.toDTO(saved);
        } catch (IOException e) {
            throw new RuntimeException("Không thể upload ảnh", e);
        }
    }

    // ==========================================
    // UPDATE
    // ==========================================
    @Override
    public ProductDTO update(Long id, ProductDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        try {
            MultipartFile image = dto.getImage();
            // Có upload ảnh mới
            if (image != null && !image.isEmpty()) {
                String oldImage = product.getImages();
                String newImage = saveImage(image);
                dto.setImages(newImage);
                // Xóa ảnh cũ
                deleteImage(oldImage);
            } else {
                // Không upload ảnh mới, giữ nguyên ảnh cũ
                dto.setImages(product.getImages());
            }
            productMapper.updateEntity(dto, product);
            Product updated = productRepository.save(product);
            return productMapper.toDTO(updated);
        } catch (IOException e) {
            throw new RuntimeException("Không thể upload ảnh", e);
        }
    }

    // ==========================================
    // DELETE
    // ==========================================
    @Override
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        // Xóa ảnh vật lý
        deleteImage(product.getImages());
        // Xóa product
        productRepository.delete(product);
    }

    // ==========================================
    // SAVE IMAGE
    // ==========================================
    private String saveImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        // Tạo thư mục nếu chưa tồn tại
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        // Lấy extension
        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        // UUID chống trùng tên
        String fileName = UUID.randomUUID().toString() + extension;
        Path target = uploadDir.resolve(fileName);
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return fileName;
    }

    // ==========================================
    // DELETE IMAGE
    // ==========================================
    private void deleteImage(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return;
        }
        try {
            Path uploadRoot = uploadDir.toAbsolutePath().normalize();
            Path file = uploadRoot.resolve(fileName).normalize();
            // Chống Path Traversal
            if (!file.startsWith(uploadRoot)) {
                throw new SecurityException("Tên file không hợp lệ");
            }
            Files.deleteIfExists(file);
        } catch (IOException e) {
            System.err.println("Không thể xóa ảnh: " + fileName);
        }
    }
}
