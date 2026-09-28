package br.com.viabank.repository;

import br.com.viabank.entity.Investment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InvestmentRepository extends JpaRepository<Investment, Long> {
    List<Investment> findAllByAccountIdOrderByIdAsc(Long accountId);
    Optional<Investment> findByIdAndAccountId(Long id, Long accountId);
}
