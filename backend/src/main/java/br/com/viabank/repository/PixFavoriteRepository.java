package br.com.viabank.repository;

import br.com.viabank.entity.PixFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PixFavoriteRepository extends JpaRepository<PixFavorite, Long> {
    List<PixFavorite> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
