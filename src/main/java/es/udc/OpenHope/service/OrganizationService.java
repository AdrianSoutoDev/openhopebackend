package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.OrganizationDto;
import es.udc.OpenHope.exception.DuplicateEmailException;
import org.springframework.stereotype.Service;

@Service
public interface OrganizationService {
  OrganizationDto create(String email, String password, String name, String description, String image) throws DuplicateEmailException;
}
