package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.CampaignDto;
import es.udc.OpenHope.exception.DuplicatedCampaignException;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface CampaignService {
  CampaignDto create(long organizationId, String owner, String name, String description, LocalDate startAt,
                     LocalDate dateLimit, Long economicTarget, Float minimumDonation, List<String> categoryNames, MultipartFile image) throws DuplicatedCampaignException;
}
