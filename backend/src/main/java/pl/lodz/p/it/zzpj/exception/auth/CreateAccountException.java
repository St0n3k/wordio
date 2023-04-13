package pl.lodz.p.it.zzpj.exception.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Username already exists or email is already used")
public class CreateAccountException extends Exception {
}
