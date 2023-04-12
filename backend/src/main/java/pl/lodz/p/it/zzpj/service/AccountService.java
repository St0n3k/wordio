package pl.lodz.p.it.zzpj.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.zzpj.entity.Account;
import pl.lodz.p.it.zzpj.repository.AccountRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return accountRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public Account getAccountByUsername(String username) throws AccountNotFoundException {
        return accountRepository.findByUsername(username).orElseThrow(() -> new AccountNotFoundException());
    }
}
