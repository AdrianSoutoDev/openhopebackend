package es.udc.OpenHope.repository;

import es.udc.OpenHope.model.Campaign;
import es.udc.OpenHope.model.Organization;
import es.udc.OpenHope.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
  List<Topic> findByOrganization(Organization organization);
  List<Topic> findByCampaign(Campaign campaign);
  Topic findByNameAndOrganization(String name, Organization organization);
}
