package es.udc.OpenHope.utils;

import es.udc.OpenHope.dto.AspspParamsDto;
import es.udc.OpenHope.dto.BankAccountParams;
import es.udc.OpenHope.model.Category;
import es.udc.OpenHope.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static es.udc.OpenHope.utils.Constants.*;

@Component
public class Utils {

  private final CategoryRepository categoryRepository;

  @Autowired
  public Utils(CategoryRepository categoryRepository) {
    this.categoryRepository = categoryRepository;
  }

  public void initCategories() {
    List<String> categoryNames = new ArrayList<>(Arrays.asList(CATEGORY_1, CATEGORY_2, CATEGORY_3));
    List<Category> categories = getCategories(categoryNames);
    categoryRepository.saveAll(categories);
  }

  public List<String> getCategoryNames() {
    return new ArrayList<>(Arrays.asList(CATEGORY_1, CATEGORY_2, CATEGORY_3));
  }

  public MockMultipartFile getTestImg(String fileName) throws IOException {
    ClassPathResource resource = new ClassPathResource("test-images/" + fileName);
    byte[] fileContent = Files.readAllBytes(resource.getFile().toPath());
    return new MockMultipartFile(
        "file",
        "test-image.png",
        "image/png",
        fileContent
    );
  }

  public MockMultipartFile getTestImg() throws IOException {
    return getTestImg("test-image.png");
  }

  private List<Category> getCategories(List<String> categoryNames){
    List<Category> categories = new ArrayList<>();
    categoryNames.forEach(c -> categories.add(new Category(c)));
    return categories;
  }

  public static AspspParamsDto getAspspParams(){
    AspspParamsDto aspspParamsDto = new AspspParamsDto();
    aspspParamsDto.setCode(ASPSP_CODE);
    aspspParamsDto.setName(ASPSP_NAME);
    aspspParamsDto.setProvider(ASPSP_PROVIDER);

    return aspspParamsDto;
  }

  public static BankAccountParams getBankAccountParams() {
    BankAccountParams bankAccountParams = new BankAccountParams();
    bankAccountParams.setIban(BANK_IBAN);
    bankAccountParams.setResourceId(BANK_RESOURCE_ID);
    bankAccountParams.setOwnerName(BANK_OWNER_NAME);
    bankAccountParams.setOriginalName(BANK_ORIGINAL_NAME);
    return bankAccountParams;
  }

  public static List<String> getTopics() {
    List<String> topics = new ArrayList<>();
    topics.add("topic1");
    topics.add("topic2");
    topics.add("topic3");
    topics.add("topic4");
    topics.add("topic5");
    return topics;
  }

  public static List<String> getAnotherTopics() {
    List<String> topics = new ArrayList<>();
    topics.add("topic7");
    topics.add("topic6");
    topics.add("topic4");
    topics.add("topic5");
    return topics;
  }

}
