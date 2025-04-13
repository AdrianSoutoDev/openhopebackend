package es.udc.OpenHope.controller;

import es.udc.OpenHope.dto.CategoryDto;
import es.udc.OpenHope.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {

  private final CategoryService categoryService;

  @GetMapping
  public ResponseEntity<List<CategoryDto>> getCategories() {
    List<CategoryDto> categories = categoryService.getAll();
    return ResponseEntity.ok(categories);
  }


}
