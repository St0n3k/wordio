package pl.lodz.p.it.zzpj.exception.game;

import pl.lodz.p.it.zzpj.common.Messages;
import pl.lodz.p.it.zzpj.exception.AppBaseException;

public class PlayerAlreadySentAnswersException extends AppBaseException {
    public PlayerAlreadySentAnswersException() {
        super(Messages.PLAYER_ALREADY_SENT_ANSWERS);
    }
}
