package pl.lodz.p.it.zzpj.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.lodz.p.it.zzpj.entity.Account;
import pl.lodz.p.it.zzpj.exception.account.AccountNotFoundException;
import pl.lodz.p.it.zzpj.service.AccountService;
import pl.lodz.p.it.zzpj.service.JwtService;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtProvider;
    private final AccountService userDetailsService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String jwt = jwtProvider.getToken(request);

        try {
            if (jwt == null || !jwtProvider.validateToken(jwt)) {
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                        null,
                        null,
                        Collections.singleton(new SimpleGrantedAuthority("ANONYMOUS")));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
                filterChain.doFilter(request, response);
                return;
            }
        } catch (ExpiredJwtException exp) {
            response.setStatus(401);
            return;
        }

        Claims claims = jwtProvider.parseJWT(jwt).getBody();

        Account account;
        try {
            account = userDetailsService.getAccountByUsername(claims.getSubject());
        } catch (AccountNotFoundException enfe) {
            response.setStatus(401);
            return;
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            account,
            null,
            account.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}
