package es.udc.OpenHope.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrganizationDto {
  private Long id;
  private String email;
  private String name;
  private String description;
  private List<CategoryDto> categories;
  private String image;
}
