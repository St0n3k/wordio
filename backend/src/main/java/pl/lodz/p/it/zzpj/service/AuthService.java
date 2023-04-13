package pl.lodz.p.it.zzpj.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.zzpj.entity.Account;
import pl.lodz.p.it.zzpj.exception.auth.CreateAccountException;
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

    @PostConstruct
    private void init() {
        clearAccounts();
        initialData();
    }

    private void initialData() {
        Account account = new Account("kamillo", "test@wp.pl", "test1234");
        try {
            this.registerAccount(account);
        } catch (Exception ignored) {
            //
        }
    }

    private void clearAccounts() {
        accountRepository.deleteAll();
    }

    public Account registerAccount(Account account) throws CreateAccountException {
        Account acc;
        try {
            account.setPassword(passwordEncoder.encode(account.getPassword()));
            acc = accountRepository.save(account);
        } catch (DuplicateKeyException e) {
            throw new CreateAccountException();
        }
        return acc;
    }

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
            authentication = authenticationManager.
                authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (AuthenticationException ae) {
            throw new LoginException();
        }

        Account account = (Account) authentication.getPrincipal();

        return jwtService.generateJWT(account.getUsername());
    }

}
