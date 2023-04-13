package pl.lodz.p.it.zzpj.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.lodz.p.it.zzpj.controller.dto.ChangePasswordDTO;
import pl.lodz.p.it.zzpj.controller.dto.LoginDTO;
import pl.lodz.p.it.zzpj.controller.dto.RegisterDTO;
import pl.lodz.p.it.zzpj.controller.dto.SuccessfulLoginDTO;
import pl.lodz.p.it.zzpj.exception.auth.CreateAccountException;
import pl.lodz.p.it.zzpj.exception.auth.LoginException;
import pl.lodz.p.it.zzpj.exception.auth.PasswordNotMatchesException;
import pl.lodz.p.it.zzpj.exception.auth.SamePasswordException;
import pl.lodz.p.it.zzpj.service.AuthService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/register")
    public UUID registerAccount(@Valid @RequestBody RegisterDTO registerDTO)
        throws CreateAccountException {
        return authService.registerAccount(registerDTO.mapToAccount()).getId();
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.OK)
    public void changePassword(@Valid @RequestBody ChangePasswordDTO dto)
        throws PasswordNotMatchesException, SamePasswordException {
        authService.changePassword(dto.getOldPassword(), dto.getNewPassword());
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/login")
    public SuccessfulLoginDTO login(@Valid @RequestBody LoginDTO loginDTO) throws LoginException {
        return new SuccessfulLoginDTO(authService.login(loginDTO.getUsername(), loginDTO.getPassword()));
    }

}
