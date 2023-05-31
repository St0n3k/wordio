package pl.lodz.p.it.zzpj.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckedWord implements Serializable {
    private String word;
    private boolean valid;
}
