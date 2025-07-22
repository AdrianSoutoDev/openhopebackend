package es.udc.OpenHope.repository;

import es.udc.OpenHope.model.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface DonationRepository extends JpaRepository<Donation, Long>{
}
