package pl.lodz.p.it.zzpj.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.zzpj.entity.Account;
import pl.lodz.p.it.zzpj.exception.account.AccountNotFoundException;
import pl.lodz.p.it.zzpj.exception.auth.CreateAccountException;
import pl.lodz.p.it.zzpj.repository.AccountRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;

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
            //Ignored, because there are no duplicates in initial data
        }
    }

    private void clearAccounts() {
        accountRepository.deleteAll();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return accountRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public Account getAccountByUsername(String username) throws AccountNotFoundException {
        return accountRepository.findByUsername(username).orElseThrow(AccountNotFoundException::new);
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

    public void deleteAccount() {
        Account account = (Account) SecurityContextHolder
            .getContext().getAuthentication().getPrincipal();
        accountRepository.delete(account);
    }
}
