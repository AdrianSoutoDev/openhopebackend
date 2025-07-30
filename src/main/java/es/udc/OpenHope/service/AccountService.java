package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.LoginDto;
import es.udc.OpenHope.dto.UserAccountDto;
import es.udc.OpenHope.exception.InvalidCredentialsException;

public interface AccountService {
  LoginDto authenticate(String email, String password) throws InvalidCredentialsException;
  UserAccountDto getByEmail(String email);
}
