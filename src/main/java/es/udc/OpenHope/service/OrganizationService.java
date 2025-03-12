package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.OrganizationDto;
import es.udc.OpenHope.exception.DuplicateEmailException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface OrganizationService {
  OrganizationDto create(String email, String password, String name, String description, MultipartFile image) throws DuplicateEmailException;
}
