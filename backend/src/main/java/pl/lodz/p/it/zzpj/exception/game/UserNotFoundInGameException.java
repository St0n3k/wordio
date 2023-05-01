package pl.lodz.p.it.zzpj.exception.game;

public class UserNotFoundInGameException extends Exception {

    public UserNotFoundInGameException() {
        super("user.not.found.in.game");
    }
}
