package pl.lodz.p.it.zzpj.exception.game;

public class GameAlreadyStartedException extends Exception {
    public GameAlreadyStartedException() {
        super("GameAlreadyStarted");
    }
}
