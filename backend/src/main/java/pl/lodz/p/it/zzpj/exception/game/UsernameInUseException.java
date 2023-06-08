package pl.lodz.p.it.zzpj.exception.game;

import pl.lodz.p.it.zzpj.common.Messages;
import pl.lodz.p.it.zzpj.exception.AppBaseException;

public class UsernameInUseException extends AppBaseException {

    public UsernameInUseException() {
        super(Messages.USERNAME_IN_USE);
    }
}
