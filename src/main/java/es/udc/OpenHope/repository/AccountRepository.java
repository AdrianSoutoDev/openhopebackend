package es.udc.OpenHope.repository;

import es.udc.OpenHope.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
  boolean existsByEmailIgnoreCase(String email);
  Account getUserByEmailIgnoreCase(String email);
}
