package es.udc.OpenHope.dto;

import lombok.Data;

@Data
public class OrganizationDto {
  private Long id;
  private String email;
  private String name;
  private String description;
  private String image;
}
