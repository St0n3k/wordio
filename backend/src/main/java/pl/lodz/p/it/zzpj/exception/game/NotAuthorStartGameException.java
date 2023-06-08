package pl.lodz.p.it.zzpj.exception.game;

import pl.lodz.p.it.zzpj.common.Messages;
import pl.lodz.p.it.zzpj.exception.AppBaseException;

public class NotAuthorStartGameException extends AppBaseException {

    public NotAuthorStartGameException() {
        super(Messages.NOT_AUTHOR_STARTED);
    }
}
