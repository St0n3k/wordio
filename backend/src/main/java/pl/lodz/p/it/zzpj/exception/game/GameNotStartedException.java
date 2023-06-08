package pl.lodz.p.it.zzpj.exception.game;

import pl.lodz.p.it.zzpj.common.Messages;
import pl.lodz.p.it.zzpj.exception.AppBaseException;

public class GameNotStartedException extends AppBaseException {

    public GameNotStartedException() {
        super(Messages.GAME_NOT_STARTED);
    }
}
