package es.udc.OpenHope.common;

import es.udc.OpenHope.service.TokenService;
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

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

  @Value("${jwt.header}")
  private String AUTH_HEADER;

  @Value("${jwt.prefix}")
  private String PREFIX;

  private final MessageSource messageSource;
  private final TokenService tokenService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    try {
      String authHeader = request.getHeader(AUTH_HEADER);
      boolean isValidToken = authHeader != null && authHeader.startsWith(String.format("%s ", PREFIX));

      if(isValidToken) {
        String email = tokenService.extractsubject(authHeader);

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
}
