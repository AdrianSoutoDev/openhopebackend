package es.udc.OpenHope.repository;

import es.udc.OpenHope.model.Category;
import es.udc.OpenHope.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository  extends JpaRepository<Category, Long> {
  List<Category> findByOrganization(Organization organization);
}
