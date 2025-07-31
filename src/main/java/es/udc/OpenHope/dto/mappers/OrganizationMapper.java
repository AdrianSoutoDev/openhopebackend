package es.udc.OpenHope.dto.mappers;

import es.udc.OpenHope.dto.CategoryDto;
import es.udc.OpenHope.dto.OrganizationDto;
import es.udc.OpenHope.model.Organization;

import java.util.List;

public abstract class OrganizationMapper {

  public static OrganizationDto toOrganizationDto(Organization organization){
    OrganizationDto organizationDto = new OrganizationDto();
    organizationDto.setId(organization.getId());
    organizationDto.setEmail(organization.getEmail());
    organizationDto.setName(organization.getName());
    organizationDto.setDescription(organization.getDescription());
    organizationDto.setImage(organization.getImage());

    List<CategoryDto> categories = CategoryMapper.toCategoriesDto(organization.getCategories().stream().toList());
    organizationDto.setCategories(categories);
    return organizationDto;
  }

}
