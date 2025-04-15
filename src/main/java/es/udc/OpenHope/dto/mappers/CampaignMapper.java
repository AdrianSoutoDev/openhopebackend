package es.udc.OpenHope.dto.mappers;

import es.udc.OpenHope.dto.CampaignDto;
import es.udc.OpenHope.dto.CategoryDto;
import es.udc.OpenHope.dto.OrganizationDto;
import es.udc.OpenHope.model.Campaign;

import java.util.List;

public abstract class CampaignMapper {

  public static CampaignDto toCampaignDto(Campaign campaign) {
    CampaignDto campaignDto = new CampaignDto();
    campaignDto.setId(campaign.getId());
    campaignDto.setName(campaign.getName());
    campaignDto.setStartAt(campaign.getStartAt().toLocalDate());
    campaignDto.setDateLimit(campaign.getDateLimit().toLocalDate());
    campaignDto.setEconomicTarget(campaign.getEconomicTarget());
    campaignDto.setImage(campaign.getImage());
    campaignDto.setDescription(campaign.getDescription());

    OrganizationDto organizationDto = OrganizationMapper.toOrganizationDto(campaign.getOrganization());
    campaignDto.setOrganization(organizationDto);

    List<CategoryDto> categories = CategoryMapper.toCategoriesDto(campaign.getCategories().stream().toList());
    campaignDto.setCategories(categories);

    return campaignDto;
  }
}
