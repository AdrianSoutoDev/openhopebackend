package es.udc.OpenHope.dto.mappers;

import es.udc.OpenHope.dto.OrganizationDto;
import es.udc.OpenHope.model.Organization;
import org.aspectj.weaver.ast.Or;

public class OrganizationMapper {

  public static OrganizationDto toOrganizationDto(Organization organization){
    OrganizationDto organizationDto = new OrganizationDto();
    organizationDto.setId(organization.getId());
    organizationDto.setEmail(organization.getEmail());
    organizationDto.setName(organization.getName());
    organizationDto.setDescription(organization.getDescription());
    organizationDto.setImage(organization.getImage());
    return organizationDto;
  }
}
