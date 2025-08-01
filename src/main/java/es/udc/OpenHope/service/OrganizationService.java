package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.OrganizationDto;
import es.udc.OpenHope.dto.SearchParamsDto;
import es.udc.OpenHope.exception.DuplicateEmailException;
import es.udc.OpenHope.exception.DuplicateOrganizationException;
import es.udc.OpenHope.exception.MaxCategoriesExceededException;
import es.udc.OpenHope.exception.MaxTopicsExceededException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public interface OrganizationService extends AccountService {
  OrganizationDto create(String email, String password, String name, String description, List<String> categoryNames, List<String> topics, MultipartFile image) throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException, MaxTopicsExceededException;
  OrganizationDto get(Long id);
  OrganizationDto update(Long id, String name, String description, List<String> categoryNames, List<String> topics, MultipartFile image, String owner) throws DuplicateOrganizationException, MaxCategoriesExceededException, IOException, MaxTopicsExceededException;
  Page<OrganizationDto> search(SearchParamsDto searchParamsDto, int page, int size);
}
