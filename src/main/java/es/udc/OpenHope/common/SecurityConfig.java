package es.udc.OpenHope.common;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig implements WebMvcConfigurer {

  private final JwtFilter jwtFilter;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .addFilterAfter(jwtFilter, BasicAuthenticationFilter.class)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.POST,  "/api/users").permitAll()
            .requestMatchers(HttpMethod.POST,  "/api/organizations").permitAll()
            .requestMatchers(HttpMethod.GET,  "/api/organizations/{id}").permitAll()
            .requestMatchers(HttpMethod.GET,  "/api/organizations/{id}/campaigns").permitAll()
            .requestMatchers(HttpMethod.POST,  "/api/accounts/login").permitAll()
            .requestMatchers(HttpMethod.GET,  "/api/resources/{imageName}").permitAll()
            .requestMatchers(HttpMethod.POST,  "/api/resources").permitAll()
            .requestMatchers(HttpMethod.GET,  "/api/categories").permitAll()
            .requestMatchers(HttpMethod.GET,  "/api/campaigns/{id}").permitAll()
            .requestMatchers(HttpMethod.GET,  "/api/providers/oauth/callback").permitAll()
            .anyRequest().authenticated())
        .exceptionHandling(exceptionHandling ->
            exceptionHandling.authenticationEntryPoint(authenticationEntryPoint())
        )
        .build();
  }

  @Bean
  public AuthenticationEntryPoint authenticationEntryPoint() {
    return new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);
  }

  @Bean
  public BCryptPasswordEncoder bCryptPasswordEncoder() {
    return new BCryptPasswordEncoder(16);
  }

  @Override
  public void addCorsMappings(@NonNull CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedOrigins("http://localhost:5173")
        .allowedMethods("*")
        .allowedHeaders("*")
        .allowCredentials(true);
  }
}