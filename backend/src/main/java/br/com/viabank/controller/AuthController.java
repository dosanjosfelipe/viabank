package br.com.viabank.controller;

import br.com.viabank.dto.auth.*;
import br.com.viabank.dto.common.MessageResponse;
import br.com.viabank.entity.User;
import br.com.viabank.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    private final CurrentUserService currentUser;
    public AuthController(AuthService auth, CurrentUserService currentUser) {
        this.auth = auth; this.currentUser = currentUser;
    }

    @PostMapping("/register")
    ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(auth.register(request));
    }

    @PostMapping("/login")
    AuthResponse login(@Valid @RequestBody LoginRequest request) { return auth.login(request); }

    @PostMapping("/logout")
    MessageResponse logout(HttpServletRequest request) {
        auth.logout(request.getHeader("Authorization"));
        return new MessageResponse("Sessão encerrada.");
    }

    @GetMapping("/me")
    AuthResponse.UserSummary me() {
        User user = currentUser.get();
        return new AuthResponse.UserSummary(user.getId(), user.getName(), user.getEmail());
    }
}
