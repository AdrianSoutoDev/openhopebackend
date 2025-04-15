package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.CategoryDto;
import es.udc.OpenHope.model.Category;
import es.udc.OpenHope.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static es.udc.OpenHope.utils.Constants.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class CategoryServiceTest {

  private final CategoryRepository categoryRepository;
  private final CategoryService categoryService;

  @Autowired
  public CategoryServiceTest(final CategoryRepository categoryRepository, final CategoryService categoryService) {
    this.categoryRepository = categoryRepository;
    this.categoryService = categoryService;
  }

  private void initCategories() {
    List<Category> categories = getAll();
    categoryRepository.saveAll(categories);
  }

  private List<Category> getAll(){
    List<Category> categories = new ArrayList<>();
    getCategoryNames().forEach(c -> categories.add(new Category(c)));
    return categories;
  }

  private List<String> getCategoryNames() {
    return new ArrayList<>(Arrays.asList(CATEGORY_1, CATEGORY_2, CATEGORY_3));
  }

  @Test
  public void getAllCategoriesTest() {
    initCategories();
    List<CategoryDto> categoriesfinded = categoryService.getAll();

    assertFalse(categoriesfinded.isEmpty());
    assertEquals(3, categoriesfinded.size());
    assertTrue(categoriesfinded.stream().anyMatch(c -> c.getName().equals(CATEGORY_1)));
    assertTrue(categoriesfinded.stream().anyMatch(c -> c.getName().equals(CATEGORY_2)));
    assertTrue(categoriesfinded.stream().anyMatch(c -> c.getName().equals(CATEGORY_3)));
  }

  @Test
  public void getAllByNamesTest() {
    initCategories();
    List<String> categoryNames = getCategoryNames();
    List<Category> categories = categoryRepository.findByNameIn(categoryNames);

    assertFalse(categories.isEmpty());
    assertEquals(3, categories.size());
    assertTrue(categories.stream().anyMatch(c -> c.getName().equals(CATEGORY_1)));
    assertTrue(categories.stream().anyMatch(c -> c.getName().equals(CATEGORY_2)));
    assertTrue(categories.stream().anyMatch(c -> c.getName().equals(CATEGORY_3)));
  }
}
