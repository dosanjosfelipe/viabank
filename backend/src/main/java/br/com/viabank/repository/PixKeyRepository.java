package br.com.viabank.repository;

import br.com.viabank.domain.PixKeyType;
import br.com.viabank.entity.PixKey;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PixKeyRepository extends JpaRepository<PixKey, Long> {
    Optional<PixKey> findByValue(String value);
    boolean existsByValue(String value);
    List<PixKey> findAllByAccountIdOrderByIdAsc(Long accountId);
    Optional<PixKey> findByAccountIdAndType(Long accountId, PixKeyType type);
}
