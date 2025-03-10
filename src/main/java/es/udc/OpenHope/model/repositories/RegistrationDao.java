package es.udc.OpenHope.model.repositories;

import es.udc.OpenHope.model.entities.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistrationDao extends JpaRepository<Registration, Long> {
}
