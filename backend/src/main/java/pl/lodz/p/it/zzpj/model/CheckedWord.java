package pl.lodz.p.it.zzpj.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CheckedWord {
    private String word;
    private boolean valid;
}
