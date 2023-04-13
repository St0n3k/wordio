package pl.lodz.p.it.zzpj.controller.dto;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.lodz.p.it.zzpj.entity.Account;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDTO {

    @NotNull
    private String username;

    @NotNull
    private String email;

    @NotNull
    private String password;

    public Account mapToAccount() {
        return new Account(username, email, password);
    }
}
