package es.udc.OpenHope.dto.mappers;

import es.udc.OpenHope.dto.CategoryDto;
import es.udc.OpenHope.model.Category;

import java.util.ArrayList;
import java.util.List;

public abstract class CategoryMapper {
  public static CategoryDto toCategoryDto(Category category){
    CategoryDto categoryDto = new CategoryDto();
    categoryDto.setId(category.getId());
    categoryDto.setName(category.getName());
    return categoryDto;
  }

  public static List<CategoryDto> toCategoriesDto(List<Category> categories) {
    List<CategoryDto> categoryDtos = new ArrayList<>();

    categories.forEach(c -> {
      CategoryDto categoryDto = toCategoryDto(c);
      categoryDtos.add(categoryDto);
    });

    return categoryDtos;
  }
}
