package pl.lodz.p.it.zzpj.exception.game;

import pl.lodz.p.it.zzpj.common.Messages;
import pl.lodz.p.it.zzpj.exception.AppBaseException;

public class UserNotFoundInGameException extends AppBaseException {

    public UserNotFoundInGameException() {
        super(Messages.USER_NOT_FOUND_IN_GAME);
    }
}
