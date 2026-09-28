package br.com.viabank.security;

import br.com.viabank.repository.RevokedTokenRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final RevokedTokenRepository revokedTokens;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   CustomUserDetailsService userDetailsService,
                                   RevokedTokenRepository revokedTokens) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.revokedTokens = revokedTokens;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = auth.substring(7);
        try {
            var claims = jwtService.parse(token);
            String jti = claims.getId();
            Long userId = Long.valueOf(claims.getSubject());

            if (SecurityContextHolder.getContext().getAuthentication() == null
                && (jti == null || !revokedTokens.existsByJti(jti))) {
                UserDetails user = userDetailsService.loadUserById(userId);
                var authentication = new UsernamePasswordAuthenticationToken(
                    user, null, user.getAuthorities()
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (JwtException | IllegalArgumentException ignored) {
        }

        chain.doFilter(request, response);
    }
}
