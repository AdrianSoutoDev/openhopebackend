package es.udc.OpenHope.repository;

import es.udc.OpenHope.model.Account;
import es.udc.OpenHope.model.Campaign;
import es.udc.OpenHope.model.Donation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {
  Page<Donation> findByBankAccount_AccountAndConfirmedTrueOrderByDateDesc(Account account, Pageable pageable);
  List<Donation> findByCampaignAndConfirmedTrue(Campaign campaign);
  Page<Donation> findByCampaignAndConfirmedTrueOrderByDateDesc(Campaign campaign, Pageable pageable);
}
