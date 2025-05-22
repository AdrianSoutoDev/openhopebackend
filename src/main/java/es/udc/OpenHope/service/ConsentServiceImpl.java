package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.ConsentDto;
import es.udc.OpenHope.dto.mappers.ConsentMapper;
import es.udc.OpenHope.model.Account;
import es.udc.OpenHope.model.Consent;
import es.udc.OpenHope.repository.AccountRepository;
import es.udc.OpenHope.repository.ConsentRepository;
import es.udc.OpenHope.utils.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConsentServiceImpl implements ConsentService {

  private final ConsentRepository consentRepository;
  private final AccountRepository accountRepository;

  @Override
  @Transactional
  public ConsentDto get(String owner, String aspsp, String provider) {
    Account ownerAccount = accountRepository.getUserByEmailIgnoreCase(owner);
    Consent consent = consentRepository.findByAccountAndAspspAndProvider(ownerAccount, aspsp, provider);
    return consent != null ? ConsentMapper.toCategoryDto(consent) : null;
  }

  @Override
  @Transactional
  public void delete(String owner, String aspsp, String provider) {
    Account ownerAccount = accountRepository.getUserByEmailIgnoreCase(owner);
    Consent consent = consentRepository.findByAccountAndAspspAndProvider(ownerAccount, aspsp, provider);
    if(consent != null) {
      consentRepository.delete(consent);
    }
  }
}
