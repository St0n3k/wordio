package pl.lodz.p.it.zzpj.exception.game;

import pl.lodz.p.it.zzpj.common.Messages;
import pl.lodz.p.it.zzpj.exception.AppBaseException;

public class PlayerAlreadyVotedException extends AppBaseException {
    public PlayerAlreadyVotedException() {
        super(Messages.PLAYER_ALREADY_VOTED);
    }
}
