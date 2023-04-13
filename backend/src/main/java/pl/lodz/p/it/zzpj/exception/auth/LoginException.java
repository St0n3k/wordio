package pl.lodz.p.it.zzpj.exception.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Wrong username or password")
public class LoginException extends Exception {
}
