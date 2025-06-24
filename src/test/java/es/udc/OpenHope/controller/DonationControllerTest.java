package es.udc.OpenHope.controller;


import es.udc.OpenHope.dto.*;
import es.udc.OpenHope.model.*;
import es.udc.OpenHope.repository.*;
import es.udc.OpenHope.service.CampaignService;
import es.udc.OpenHope.service.OrganizationService;
import es.udc.OpenHope.service.UserService;
import es.udc.OpenHope.utils.Utils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static es.udc.OpenHope.utils.Constants.*;
import static es.udc.OpenHope.utils.Constants.AMOUNT_DONATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class DonationControllerTest {

  private final MockMvc mockMvc;
  private final UserService userService;
  private final AspspRepository aspspRepository;
  private final BankAccountRepository bankAccountRepository;
  private final UserRepository userRepository;
  private final OrganizationService organizationService;
  private final CampaignService campaignService;
  private final DonationRepository donationRepository;
  private final CampaignRepository campaignRepository;

  @Autowired
  public DonationControllerTest(MockMvc mockMvc, UserService userService, AspspRepository aspspRepository, BankAccountRepository bankAccountRepository, UserRepository userRepository, OrganizationService organizationService, CampaignService campaignService, DonationRepository donationRepository, CampaignRepository campaignRepository) {
    this.mockMvc = mockMvc;
    this.userService = userService;
    this.aspspRepository = aspspRepository;
    this.bankAccountRepository = bankAccountRepository;
    this.userRepository = userRepository;
    this.organizationService = organizationService;
    this.campaignService = campaignService;
    this.donationRepository = donationRepository;
    this.campaignRepository = campaignRepository;
  }

  @Test
  void getDonationsTest() throws Exception {
    UserDto userDto = userService.create(USER_EMAIL, PASSWORD);
    Optional<User> user = userRepository.findById(userDto.getId());

    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, null, null, null);
    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, null, CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, null, null, null, null, null);

    AspspParamsDto aspspParamsDto = Utils.getAspspParams();
    BankAccountParams bankAccountParams = Utils.getBankAccountParams();
    bankAccountParams.setAspsp(aspspParamsDto);

    Aspsp aspsp = Utils.getAspsps();
    aspspRepository.save(aspsp);

    BankAccount bankAccount = Utils.getBankAccount(aspsp, user.get());
    bankAccountRepository.save(bankAccount);

    Optional<Campaign> campaign = campaignRepository.findById(campaignDto.getId());
    Donation donation = Utils.getDonation(bankAccount, campaign.get(), AMOUNT_DONATION);
    donationRepository.save(donation);

    LoginDto loginDto = userService.authenticate(USER_EMAIL, PASSWORD);
    ResultActions result = mockMvc.perform(get("/api/donations")
        .header("Authorization", "Bearer " + loginDto.getToken()));

    result.andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content").isNotEmpty())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.content[0].bankAccount.iban").value(bankAccount.getIban()))
        .andExpect(jsonPath("$.content[0].campaign.id").value(campaign.get().getId()))
        .andExpect(jsonPath("$.content[0].amount").value(donation.getAmount()));

  }
}
