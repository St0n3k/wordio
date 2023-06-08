package pl.lodz.p.it.zzpj.exception.game;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import pl.lodz.p.it.zzpj.common.Messages;
import pl.lodz.p.it.zzpj.exception.AppBaseException;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Game not found")
public class GameNotFoundException extends AppBaseException {

    public GameNotFoundException() {
        super(Messages.GAME_NOT_FOUND);
    }
}
