package br.com.viabank.controller;
import br.com.viabank.dto.profile.ProfileDtos.*;
import br.com.viabank.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final ProfileService service;
    public ProfileController(ProfileService service) { this.service = service; }
    @GetMapping ProfileResponse get() { return service.get(); }
    @PutMapping ProfileResponse update(@Valid @RequestBody UpdateProfileRequest request) { return service.update(request); }
}
