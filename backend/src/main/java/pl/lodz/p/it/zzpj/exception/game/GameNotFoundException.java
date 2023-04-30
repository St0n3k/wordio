package pl.lodz.p.it.zzpj.exception.game;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Game not found")
public class GameNotFoundException extends Exception {
}
