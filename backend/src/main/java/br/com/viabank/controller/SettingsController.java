package br.com.viabank.controller;
import br.com.viabank.dto.settings.SettingsDtos.*;
import br.com.viabank.service.SettingsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {
    private final SettingsService service;
    public SettingsController(SettingsService service) { this.service = service; }
    @GetMapping SettingsResponse get() { return service.get(); }
    @PutMapping SettingsResponse update(@RequestBody UpdateSettingsRequest request) { return service.update(request); }
}
