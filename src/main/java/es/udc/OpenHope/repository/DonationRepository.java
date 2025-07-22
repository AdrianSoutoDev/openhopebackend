package es.udc.OpenHope.repository;

import es.udc.OpenHope.model.Account;
import es.udc.OpenHope.model.Donation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {
  Page<Donation> findByBankAccount_Account(Account account, Pageable pageable);
}
