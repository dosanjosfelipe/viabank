package br.com.viabank.service;

import br.com.viabank.dto.settings.SettingsDtos.*;
import br.com.viabank.entity.*;
import br.com.viabank.repository.UserSettingsRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {
    private final CurrentUserService currentUser;
    private final UserSettingsRepository settings;
    public SettingsService(CurrentUserService currentUser, UserSettingsRepository settings) {
        this.currentUser = currentUser; this.settings = settings;
    }

    public SettingsResponse get() {
        User user = currentUser.get();
        UserSettings s = settings.findByUserId(user.getId()).orElseGet(() -> createDefault(user));
        return map(s);
    }

    @Transactional
    public SettingsResponse update(UpdateSettingsRequest request) {
        User user = currentUser.get();
        UserSettings s = settings.findByUserId(user.getId()).orElseGet(() -> createDefault(user));
        s.setCompactMode(request.compactMode());
        s.setNotifications(request.notifications());
        s.setHideBalance(request.hideBalance());
        return map(settings.save(s));
    }

    private UserSettings createDefault(User user) {
        UserSettings s = new UserSettings();
        s.setUser(user); s.setNotifications(true);
        return settings.save(s);
    }

    private SettingsResponse map(UserSettings s) {
        return new SettingsResponse(s.isCompactMode(), s.isNotifications(), s.isHideBalance());
    }
}
