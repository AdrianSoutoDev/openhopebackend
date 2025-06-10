package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.BankAccountParams;
import es.udc.OpenHope.dto.CampaignDto;
import es.udc.OpenHope.dto.searcher.SearchParamsDto;
import es.udc.OpenHope.dto.searcher.SearchResultDto;
import es.udc.OpenHope.exception.DuplicatedCampaignException;
import es.udc.OpenHope.exception.MaxTopicsExceededException;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface CampaignService {
  CampaignDto create(Long organizationId, String owner, String name, String description, LocalDate startAt,
                     LocalDate dateLimit, Long economicTarget, Float minimumDonation, List<String> categoryNames, List<String> topics, MultipartFile image) throws DuplicatedCampaignException, MaxTopicsExceededException;

  Page<CampaignDto> getByOrganization(Long organizationId, int page, int size);

  CampaignDto get(Long id);
  CampaignDto updateBankAccount(Long id, BankAccountParams bankAccountParams, String owner);
  Page<SearchResultDto> search(SearchParamsDto searchParamsDto, int page, int size);
}
