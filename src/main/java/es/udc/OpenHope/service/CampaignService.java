package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.BankAccountParams;
import es.udc.OpenHope.dto.CampaignDto;
import es.udc.OpenHope.exception.DuplicatedCampaignException;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface CampaignService {
  CampaignDto create(Long organizationId, String owner, String name, String description, LocalDate startAt,
                     LocalDate dateLimit, Long economicTarget, Float minimumDonation, List<String> categoryNames, MultipartFile image) throws DuplicatedCampaignException;

  Page<CampaignDto> getByOrganization(Long organizationId, int page, int size);

  CampaignDto get(Long id);
  CampaignDto updateBankAccount(Long id, BankAccountParams bankAccountParams, String owner);
}
