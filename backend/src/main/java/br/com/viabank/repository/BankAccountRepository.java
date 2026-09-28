package br.com.viabank.repository;

import br.com.viabank.entity.BankAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByUserId(Long userId);
    boolean existsByAccountNumber(String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from BankAccount a join fetch a.user where a.id = :id")
    Optional<BankAccount> findByIdForUpdate(@Param("id") Long id);
}
