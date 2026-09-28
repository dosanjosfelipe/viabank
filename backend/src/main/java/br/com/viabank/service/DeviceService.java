package br.com.viabank.service;

import br.com.viabank.dto.device.DeviceRequest;
import br.com.viabank.entity.*;
import br.com.viabank.repository.DeviceRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class DeviceService {
    private final CurrentUserService currentUser;
    private final DeviceRepository devices;
    public DeviceService(CurrentUserService currentUser, DeviceRepository devices) {
        this.currentUser = currentUser; this.devices = devices;
    }

    @Transactional
    public void register(DeviceRequest request) {
        User user = currentUser.get();
        Device d = new Device();
        d.setUser(user); d.setUserAgent(request.userAgent()); d.setLanguage(request.language());
        d.setPlatform(request.platform()); d.setTimezone(request.timezone());
        if (request.screen() != null) {
            d.setScreenWidth(request.screen().width()); d.setScreenHeight(request.screen().height());
        }
        devices.save(d);
    }
}
