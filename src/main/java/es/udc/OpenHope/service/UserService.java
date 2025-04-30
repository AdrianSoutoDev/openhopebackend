package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.UserDto;
import es.udc.OpenHope.exception.DuplicateEmailException;

public interface UserService {
    UserDto create(String email, String password) throws DuplicateEmailException;
}
