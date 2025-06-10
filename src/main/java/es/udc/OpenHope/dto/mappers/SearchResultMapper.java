package es.udc.OpenHope.dto.mappers;

import es.udc.OpenHope.dto.searcher.CampaignSearchResultDto;
import es.udc.OpenHope.dto.searcher.OrganizationSearchResultDto;
import es.udc.OpenHope.model.Campaign;
import es.udc.OpenHope.model.Organization;

public abstract class SearchResultMapper {

  public static OrganizationSearchResultDto toOrganizationSearchResultDto(Organization organization){
    OrganizationSearchResultDto organizationSearchResultDto = new OrganizationSearchResultDto();
    organizationSearchResultDto.setId(organization.getId());
    organizationSearchResultDto.setName(organization.getName());
    organizationSearchResultDto.setDescription(organization.getDescription());
    organizationSearchResultDto.setImage(organization.getImage());

    return organizationSearchResultDto;
  }

  public static CampaignSearchResultDto toCampaignSearchResultDto(Campaign campaign){
    CampaignSearchResultDto campaignSearchResultDto = new CampaignSearchResultDto();
    campaignSearchResultDto.setId(campaign.getId());
    campaignSearchResultDto.setName(campaign.getName());
    campaignSearchResultDto.setDescription(campaign.getDescription());
    campaignSearchResultDto.setImage(campaign.getImage());

    return campaignSearchResultDto;
  }

}
