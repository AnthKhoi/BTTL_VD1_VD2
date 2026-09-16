package vn.iotstar.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.services.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // =========================
    // LIST + SEARCH + PAGINATION
    // =========================
    @GetMapping
    public String listProducts(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {
        Page<ProductDTO> productPage = productService.findAll(keyword, page, size);
        model.addAttribute("products", productPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("size", size);
        return "products/list";
    }

    // =========================
    // CREATE FORM
    // =========================
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("product", new ProductDTO());
        model.addAttribute("formTitle", "Thêm sản phẩm");
        return "products/form";
    }

    // =========================
    // CREATE
    // =========================
    @PostMapping(value = "/create", consumes = "multipart/form-data")
    public String create(
            @Valid @ModelAttribute("product") ProductDTO dto,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Thêm sản phẩm");
            return "products/form";
        }
        productService.create(dto);
        return "redirect:/products";
    }

    // =========================
    // EDIT FORM
    // =========================
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        ProductDTO product = productService.findById(id);
        model.addAttribute("product", product);
        model.addAttribute("formTitle", "Cập nhật sản phẩm");
        return "products/form";
    }

    // =========================
    // UPDATE
    // =========================
    @PostMapping(value = "/edit/{id}", consumes = "multipart/form-data")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("product") ProductDTO dto,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Cập nhật sản phẩm");
            return "products/form";
        }
        productService.update(id, dto);
        return "redirect:/products";
    }

    // =========================
    // DELETE
    // =========================
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        productService.delete(id);
        return "redirect:/products";
    }
}
