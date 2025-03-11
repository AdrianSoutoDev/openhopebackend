package es.udc.OpenHope.service;

import es.udc.OpenHope.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
  protected final BCryptPasswordEncoder bCryptPasswordEncoder;
  protected final AccountRepository accountRepository;

  protected boolean accountExists(String email) {
    return accountRepository.existsByEmailIgnoreCase(email);
  }
}
