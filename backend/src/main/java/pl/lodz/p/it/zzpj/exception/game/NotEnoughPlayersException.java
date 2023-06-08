package pl.lodz.p.it.zzpj.exception.game;

import pl.lodz.p.it.zzpj.common.Messages;
import pl.lodz.p.it.zzpj.exception.AppBaseException;

public class NotEnoughPlayersException extends AppBaseException {

    public NotEnoughPlayersException() {
        super(Messages.NOT_ENOUGH_PLAYERS);
    }
}
