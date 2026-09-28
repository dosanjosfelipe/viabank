package br.com.viabank.security;

import br.com.viabank.entity.User;
import br.com.viabank.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository users;
    public CustomUserDetailsService(UserRepository users) { this.users = users; }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return map(users.findByEmailIgnoreCase(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado")));
    }

    public UserDetails loadUserById(Long id) {
        return map(users.findById(id)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado")));
    }

    private UserDetails map(User user) {
        return org.springframework.security.core.userdetails.User
            .withUsername(user.getEmail())
            .password(user.getPasswordHash())
            .authorities("ROLE_USER")
            .disabled(!user.isEnabled())
            .build();
    }
}
