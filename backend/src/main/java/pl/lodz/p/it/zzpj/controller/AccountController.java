package pl.lodz.p.it.zzpj.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.lodz.p.it.zzpj.service.AccountService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class AccountController {

    private final AccountService accountService;

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping
    @Secured("ROLE_PLAYER")
    public void deleteAccount() {
        accountService.deleteAccount();
    }
}
