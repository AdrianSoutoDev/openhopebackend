package es.udc.OpenHope.repository;

import es.udc.OpenHope.model.Account;
import es.udc.OpenHope.model.Consent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsentRepository extends JpaRepository<Consent, Long> {
  Consent findByAccountAndAspspAndProvider(Account account, String aspsp, String Provider);
}