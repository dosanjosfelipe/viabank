package br.com.viabank.service;

import br.com.viabank.entity.User;
import br.com.viabank.exception.NotFoundException;
import br.com.viabank.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    private final UserRepository users;
    public CurrentUserService(UserRepository users) { this.users = users; }

    public User get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null)
            throw new NotFoundException("Usuário autenticado não encontrado.");
        return users.findByEmailIgnoreCase(auth.getName())
            .orElseThrow(() -> new NotFoundException("Usuário autenticado não encontrado."));
    }
}
