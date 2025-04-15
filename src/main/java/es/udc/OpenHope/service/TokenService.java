package es.udc.OpenHope.service;

public interface TokenService {
  String generateToken(String identifier);
  String extractsubject(String jwt);
}
