package br.com.viabank.repository;

import br.com.viabank.entity.InvestmentProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InvestmentProductRepository extends JpaRepository<InvestmentProduct, Long> {
    List<InvestmentProduct> findAllByActiveTrueOrderByIdAsc();
}
