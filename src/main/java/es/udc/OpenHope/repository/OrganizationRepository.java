package es.udc.OpenHope.repository;

import es.udc.OpenHope.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
  //TODO Add existByName
}
