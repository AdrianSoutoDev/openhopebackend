package es.udc.OpenHope.service;

import es.udc.OpenHope.exception.InvalidCredentialsException;
import es.udc.OpenHope.model.Account;
import es.udc.OpenHope.repository.AccountRepository;
import es.udc.OpenHope.utils.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Primary
public class AccountServiceImpl implements AccountService {

  @Value("${jwt.secret}")
  private String SECRET;

  protected final BCryptPasswordEncoder bCryptPasswordEncoder;
  protected final AccountRepository accountRepository;
  protected final TokenService tokenService;

  @Override
  public String authenticate(String email, String password) throws InvalidCredentialsException {

    validateParamsAuthenticate(email, password);

    Account account = accountRepository.getUserByEmailIgnoreCase(email);
    if(account == null) {
      throw new InvalidCredentialsException( Messages.get("validation.credentials.invalid") );
    }

    boolean matches = bCryptPasswordEncoder.matches(password, account.getEncryptedPassword());
    if(!matches) {
      throw new InvalidCredentialsException( Messages.get("validation.credentials.invalid") );
    }

    return tokenService.generateToken(email);
  }

  protected boolean accountExists(String email) {
    return accountRepository.existsByEmailIgnoreCase(email);
  }

  private void validateParamsAuthenticate(String email, String password) {
    if(email == null || email.isBlank()) throw new IllegalArgumentException( Messages.get("validation.email.null") );
    if(password == null || password.isBlank()) throw new IllegalArgumentException( Messages.get("validation.password.null") );
  }
}
