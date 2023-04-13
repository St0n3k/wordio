package pl.lodz.p.it.zzpj.exception.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Wrong password")
public class PasswordNotMatchesException extends Exception {
}
