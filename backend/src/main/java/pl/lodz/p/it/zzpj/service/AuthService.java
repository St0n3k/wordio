package pl.lodz.p.it.zzpj.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.zzpj.entity.Account;
import pl.lodz.p.it.zzpj.exception.auth.LoginException;
import pl.lodz.p.it.zzpj.exception.auth.PasswordNotMatchesException;
import pl.lodz.p.it.zzpj.exception.auth.SamePasswordException;
import pl.lodz.p.it.zzpj.repository.AccountRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final AuthenticationManager authenticationManager;

    private final AccountRepository accountRepository;

    private final JwtService jwtService;

    private final PasswordEncoder passwordEncoder;



    public void changePassword(String oldPassword, String newPassword)
        throws PasswordNotMatchesException, SamePasswordException {
        if (oldPassword.equals(newPassword)) {
            throw new SamePasswordException();
        }

        Account account = (Account) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!passwordEncoder.matches(oldPassword, account.getPassword())) {
            throw new PasswordNotMatchesException();
        }

        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    public String login(String username, String password) throws LoginException {
        Authentication authentication;

        try {
            authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (AuthenticationException ae) {
            throw new LoginException();
        }

        Account account = (Account) authentication.getPrincipal();

        return jwtService.generateJWT(account);
    }

}
