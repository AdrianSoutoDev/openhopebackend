package es.udc.OpenHope.repository;

import es.udc.OpenHope.model.Account;
import es.udc.OpenHope.model.BankAccount;
import es.udc.OpenHope.model.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
  Optional<BankAccount> findByIbanAndAccount(String iban, Account account);
  Page<BankAccount> findByAccount(Account account, Pageable pageable);
  List<BankAccount> findByAccount(Account account);
}
