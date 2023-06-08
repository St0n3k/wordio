package pl.lodz.p.it.zzpj.exception.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import pl.lodz.p.it.zzpj.common.Messages;
import pl.lodz.p.it.zzpj.exception.AppBaseException;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = Messages.USERNAME_ALREADY_EXIST_OR_EMAIL_ALREADY_USED)
public class CreateAccountException extends AppBaseException {
}
