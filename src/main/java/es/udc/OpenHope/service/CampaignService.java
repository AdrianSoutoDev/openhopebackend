package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.BankAccountParams;
import es.udc.OpenHope.dto.CampaignDto;
import es.udc.OpenHope.dto.DonationDto;
import es.udc.OpenHope.dto.SearchParamsDto;
import es.udc.OpenHope.exception.DuplicatedCampaignException;
import es.udc.OpenHope.exception.MaxTopicsExceededException;
import es.udc.OpenHope.model.BankAccount;
import es.udc.OpenHope.model.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

public interface CampaignService {
  CampaignDto create(Long organizationId, String owner, String name, String description, LocalDate startAt,
                     LocalDate dateLimit, Long economicTarget, Float minimumDonation, List<String> categoryNames, List<String> topics, MultipartFile image) throws DuplicatedCampaignException, MaxTopicsExceededException;

  Page<CampaignDto> getByOrganization(Long organizationId, int page, int size);

  CampaignDto get(Long id);

  CampaignDto updateBankAccount(Long id, BankAccountParams bankAccountParams, String owner);

  Page<CampaignDto> search(SearchParamsDto searchParamsDto, int page, int size);
  Page<DonationDto> getDonations(Long id, int page, int size);
  DonationDto addDonation(Campaign campaign, BankAccount bankAccount, Float amount, Timestamp datetime);
}
