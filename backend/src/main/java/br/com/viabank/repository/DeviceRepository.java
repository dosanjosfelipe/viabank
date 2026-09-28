package br.com.viabank.repository;
import br.com.viabank.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
public interface DeviceRepository extends JpaRepository<Device, Long> {}
