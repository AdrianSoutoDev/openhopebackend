package es.udc.OpenHope.repository;

import es.udc.OpenHope.model.Account;
import es.udc.OpenHope.model.BankAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
  Optional<BankAccount> findByIbanAndAccount(String iban, Account account);
  Page<BankAccount> findByAccount(Account account, Pageable pageable);
}
