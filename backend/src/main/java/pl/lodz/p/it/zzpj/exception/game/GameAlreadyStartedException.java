package pl.lodz.p.it.zzpj.exception.game;

import pl.lodz.p.it.zzpj.common.Messages;
import pl.lodz.p.it.zzpj.exception.AppBaseException;

public class GameAlreadyStartedException extends AppBaseException {
    public GameAlreadyStartedException() {
        super(Messages.GAME_ALREADY_STARTED);
    }
}
