package pl.lodz.p.it.zzpj.exception.account;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import pl.lodz.p.it.zzpj.common.Messages;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = Messages.ACCOUNT_NOT_FOUND)
public class AccountNotFoundException extends Exception {
}
