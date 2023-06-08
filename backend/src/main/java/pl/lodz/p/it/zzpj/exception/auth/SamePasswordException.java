package pl.lodz.p.it.zzpj.exception.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import pl.lodz.p.it.zzpj.common.Messages;
import pl.lodz.p.it.zzpj.exception.AppBaseException;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = Messages.CANNOT_CHANGE_PASSWORD_TO_OLD)
public class SamePasswordException extends AppBaseException {
}
