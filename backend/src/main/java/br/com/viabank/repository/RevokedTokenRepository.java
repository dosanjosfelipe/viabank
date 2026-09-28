package br.com.viabank.repository;
import br.com.viabank.entity.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {
    boolean existsByJti(String jti);
}
