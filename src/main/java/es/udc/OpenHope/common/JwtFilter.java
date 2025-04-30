package es.udc.OpenHope.common;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

  @Value("${jwt.secret}")
  private String SECRET;

  @Value("${jwt.expiration}")
  private Long EXPIRATION;

  @Value("${jwt.header}")
  private String AUTH_HEADER;

  @Value("${jwt.prefix}")
  private String PREFIX;

  private final MessageSource messageSource;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    try {
      String authHeader = request.getHeader(AUTH_HEADER);
      boolean isValidToken = authHeader != null && authHeader.startsWith(String.format("%s ", PREFIX));

      if(isValidToken) {
        String tokenClear = authHeader.replace(String.format("%s ", PREFIX), "");
        String email = extractsubject(tokenClear);

        if (email != null && !email.trim().isEmpty()) {
          UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(email, null, null);
          SecurityContextHolder.getContext().setAuthentication(auth);
        } else {
          SecurityContextHolder.clearContext();
        }
      } else {
        SecurityContextHolder.clearContext();
      }

      filterChain.doFilter(request, response);
    } catch(Exception e) {
      SecurityContextHolder.clearContext();
      filterChain.doFilter(request, response);
    }
  }

  private String extractsubject(String jwt) {
    SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(jwt)
        .getPayload()
        .getSubject();
  }
}
