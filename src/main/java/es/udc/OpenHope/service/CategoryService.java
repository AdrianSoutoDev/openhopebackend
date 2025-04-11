package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.CategoryDto;
import es.udc.OpenHope.model.Category;

import java.util.List;

public interface CategoryService {
  List<CategoryDto> getCategories();
  List<Category> getCategoriesByName(List<String> categoryNames);
}
