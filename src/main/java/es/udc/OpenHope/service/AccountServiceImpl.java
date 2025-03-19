package es.udc.OpenHope.service;

import es.udc.OpenHope.exception.InvalidCredentialsException;
import es.udc.OpenHope.model.Account;
import es.udc.OpenHope.repository.AccountRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Primary
public class AccountServiceImpl implements AccountService {

  @Value("${jwt.secret}")
  private String SECRET;

  @Value("${jwt.expiration}")
  private Long EXPIRATION;

  protected final BCryptPasswordEncoder bCryptPasswordEncoder;
  protected final AccountRepository accountRepository;

  @Override
  public String authenticate(String email, String password) throws InvalidCredentialsException {

    if(email == null) throw new IllegalArgumentException("email cannot be null");
    if(password == null) throw new IllegalArgumentException("password cannot be null");

    Account account = accountRepository.getUserByEmailIgnoreCase(email);
    if(account == null) {
      throw new InvalidCredentialsException("Invalid credentials");
    }

    boolean matches = bCryptPasswordEncoder.matches(password, account.getEncryptedPassword());
    if(!matches) {
      throw new InvalidCredentialsException("Invalid credentials");
    }

    return generateToken(email);
  }

  protected boolean accountExists(String email) {
    return accountRepository.existsByEmailIgnoreCase(email);
  }

  private String generateToken(String identifier) {
    Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    return Jwts.builder()
        .claims()
        .subject(identifier)
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
        .and()
        .signWith(key)
        .compact();
  }
}
