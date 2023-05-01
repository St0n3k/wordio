package pl.lodz.p.it.zzpj.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.lodz.p.it.zzpj.controller.dto.UuidDTO;
import pl.lodz.p.it.zzpj.controller.dto.account.ChangePasswordDTO;
import pl.lodz.p.it.zzpj.controller.dto.account.LoginDTO;
import pl.lodz.p.it.zzpj.controller.dto.account.RegisterDTO;
import pl.lodz.p.it.zzpj.controller.dto.account.SuccessfulLoginDTO;
import pl.lodz.p.it.zzpj.exception.auth.CreateAccountException;
import pl.lodz.p.it.zzpj.exception.auth.LoginException;
import pl.lodz.p.it.zzpj.exception.auth.PasswordNotMatchesException;
import pl.lodz.p.it.zzpj.exception.auth.SamePasswordException;
import pl.lodz.p.it.zzpj.service.AccountService;
import pl.lodz.p.it.zzpj.service.AuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    private final AccountService accountService;

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.OK)
    @Secured("ROLE_PLAYER")
    public void changePassword(@Valid @RequestBody ChangePasswordDTO dto)
        throws PasswordNotMatchesException, SamePasswordException {
        authService.changePassword(dto.getOldPassword(), dto.getNewPassword());
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/login")
    public SuccessfulLoginDTO login(@Valid @RequestBody LoginDTO loginDTO) throws LoginException {
        return new SuccessfulLoginDTO(authService.login(loginDTO.getUsername(), loginDTO.getPassword()));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/register")
    public UuidDTO registerAccount(@Valid @RequestBody RegisterDTO registerDTO)
        throws CreateAccountException {
        return new UuidDTO(accountService
            .registerAccount(registerDTO.mapToAccount())
            .getId()
        );
    }

}
