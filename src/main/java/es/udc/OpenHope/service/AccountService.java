package es.udc.OpenHope.service;

import es.udc.OpenHope.exception.InvalidCredentialsException;

public interface AccountService {
  String authenticate(String email, String password) throws InvalidCredentialsException;
}
