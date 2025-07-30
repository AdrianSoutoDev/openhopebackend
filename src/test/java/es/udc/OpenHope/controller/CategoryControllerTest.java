package es.udc.OpenHope.controller;

import es.udc.OpenHope.model.Category;
import es.udc.OpenHope.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static es.udc.OpenHope.utils.Constants.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CategoryControllerTest {

  private final CategoryRepository categoryRepository;
  private final MockMvc mockMvc;

  @Autowired
  public CategoryControllerTest(final CategoryRepository categoryRepository, final MockMvc mockMvc) {
    this.categoryRepository = categoryRepository;
    this.mockMvc = mockMvc;
  }

  private void initCategories() {
    List<Category> categories = getCategories();
    categoryRepository.saveAll(categories);
  }

  private List<Category> getCategories(){
    List<Category> categories = new ArrayList<>();
    getCategoryNames().forEach(c -> categories.add(new Category(c)));
    return categories;
  }

  private List<String> getCategoryNames() {
    return new ArrayList<>(Arrays.asList(CATEGORY_1, CATEGORY_2, CATEGORY_3));
  }

  @Test
  void getAllCategoriesTest() throws Exception {
    initCategories();
    ResultActions result = mockMvc.perform(get("/api/categories"));
    result.andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isNotEmpty());
  }
}
