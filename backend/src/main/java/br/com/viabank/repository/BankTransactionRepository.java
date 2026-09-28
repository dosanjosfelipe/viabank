package br.com.viabank.repository;

import br.com.viabank.entity.BankTransaction;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {
    @Query("""
        select t from BankTransaction t
        where t.account.id = :accountId
          and t.createdAt >= :fromDate
          and t.createdAt < :toDate
        order by t.createdAt desc
        """)
    Page<BankTransaction> findStatement(
        @Param("accountId") Long accountId,
        @Param("fromDate") Instant fromDate,
        @Param("toDate") Instant toDate,
        Pageable pageable
    );

    @Query(value = """
        select coalesce(category, 'Outros') as category,
               abs(sum(amount)) as total
        from transactions
        where account_id = :accountId
          and amount < 0
          and created_at >= :fromDate
          and created_at < :toDate
        group by coalesce(category, 'Outros')
        order by total desc
        """, nativeQuery = true)
    List<CategorySpendingProjection> sumExpensesByCategory(
        @Param("accountId") Long accountId,
        @Param("fromDate") Instant fromDate,
        @Param("toDate") Instant toDate
    );

    interface CategorySpendingProjection {
        String getCategory();
        BigDecimal getTotal();
    }
}
