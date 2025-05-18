package es.udc.OpenHope.service;

import es.udc.OpenHope.exception.InvalidCredentialsException;
import es.udc.OpenHope.model.Account;

public interface AccountService {
  String authenticate(String email, String password) throws InvalidCredentialsException;
}
