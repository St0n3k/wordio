package pl.lodz.p.it.zzpj.exception;

import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import pl.lodz.p.it.zzpj.common.Messages;

@NoArgsConstructor
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = Messages.UNEXPECTED_ERROR)
public class AppBaseException extends Exception {
    public AppBaseException(String message) {
        super(message);
    }
}
