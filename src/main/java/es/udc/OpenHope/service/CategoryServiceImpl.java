package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.CategoryDto;
import es.udc.OpenHope.dto.mappers.CategoryMapper;
import es.udc.OpenHope.model.Category;
import es.udc.OpenHope.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

  private final CategoryRepository categoryRepository;

  @Override
  public List<CategoryDto> getAll() {
    List<Category> categories = categoryRepository.findAll();
    return CategoryMapper.toCategoriesDto(categories);
  }
}
