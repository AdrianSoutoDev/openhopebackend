package es.udc.OpenHope.repository;

import es.udc.OpenHope.model.Aspsp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AspspRepository extends JpaRepository<Aspsp, Long> {
  Optional<Aspsp> findByProviderAndCode(String provider, String code);
}
