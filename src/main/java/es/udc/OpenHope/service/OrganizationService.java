package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.OrganizationDto;
import es.udc.OpenHope.exception.DuplicateEmailException;
import es.udc.OpenHope.exception.DuplicateOrganizationException;
import es.udc.OpenHope.exception.MaxCategoriesExceededException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public interface OrganizationService extends AccountService {
  OrganizationDto create(String email, String password, String name, String description, List<String> categoryNames, MultipartFile image) throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException;
  OrganizationDto create(String email, String password, String name, String description, MultipartFile image) throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException;
  OrganizationDto getById(Long id);
  OrganizationDto update(Long id, String name, String description, List<String> categoryNames, MultipartFile image, String owner) throws DuplicateOrganizationException, MaxCategoriesExceededException, IOException;
}
