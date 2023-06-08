package pl.lodz.p.it.zzpj.exception.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import pl.lodz.p.it.zzpj.common.Messages;
import pl.lodz.p.it.zzpj.exception.AppBaseException;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = Messages.WRONG_PASSWORD)
public class PasswordNotMatchesException extends AppBaseException {
}
