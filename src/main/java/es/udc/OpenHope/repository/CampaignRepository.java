package es.udc.OpenHope.repository;

import es.udc.OpenHope.model.Campaign;
import es.udc.OpenHope.model.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
  Page<Campaign> findByOrganization(Organization organization, Pageable pageable);
}