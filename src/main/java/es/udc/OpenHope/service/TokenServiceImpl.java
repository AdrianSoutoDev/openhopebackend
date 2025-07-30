package es.udc.OpenHope.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Service
public class TokenServiceImpl implements TokenService {

  @Value("${jwt.prefix}")
  private String PREFIX;

  @Value("${jwt.secret}")
  private String SECRET;

  @Value("${jwt.expiration}")
  private Long EXPIRATION;

  @Override
  public String generateToken(String identifier) {
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

  @Override
  public String extractsubject(String jwt) {
    String token = jwt.replace(String.format("%s ", PREFIX), "");
    SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .getSubject();
  }
}
