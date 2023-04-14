package pl.lodz.p.it.zzpj;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import net.minidev.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.junit.jupiter.Testcontainers;


@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class AccountAuthTest extends TestContainersSetup {

    @Autowired
    private MockMvc mockMvc;

    //region register
    @Test
    void shouldPassWhenRegisteringNewUser() throws Exception {
        JSONObject newAccount = new JSONObject();
        newAccount.put("username", "test1");
        newAccount.put("email", "test1@wp.pl");
        newAccount.put("password", "test1234");
        mockMvc.perform(post("/auth/register").content(newAccount.toString())
                .contentType(MediaType.parseMediaType("application/json")))
            .andExpect(status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNotEmpty());

        JSONObject credentials = new JSONObject();
        credentials.put("username", "test1");
        credentials.put("password", "test1234");
        mockMvc.perform(post("/auth/login").content(credentials.toString())
                .contentType(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.jwt").isNotEmpty());
    }

    @Test
    void shouldFailWhenRegisteringUserWithSameUsername() throws Exception {
        JSONObject correctUser = new JSONObject();
        correctUser.put("username", "test2");
        correctUser.put("email", "test2@wp.pl");
        correctUser.put("password", "test1234");
        mockMvc.perform(post("/auth/register").content(correctUser.toString())
                .contentType(MediaType.parseMediaType("application/json")))
            .andExpect(status().isCreated());

        JSONObject invalidUser = new JSONObject();
        invalidUser.put("username", "test2");
        invalidUser.put("email", "test3@wp.pl");
        invalidUser.put("password", "test1234");
        mockMvc.perform(post("/auth/register").content(invalidUser.toString())
                .contentType(MediaType.parseMediaType("application/json")))
            .andExpect(status().isConflict());
    }

    @Test
    void shouldFailWhenRegisteringUserWithSameEmail() throws Exception {
        JSONObject correctUser = new JSONObject();
        correctUser.put("username", "test3");
        correctUser.put("email", "test3@wp.pl");
        correctUser.put("password", "test1234");
        mockMvc.perform(post("/auth/register").content(correctUser.toString())
                .contentType(MediaType.parseMediaType("application/json")))
            .andExpect(status().isCreated());

        JSONObject invalidUser = new JSONObject();
        invalidUser.put("username", "test4");
        invalidUser.put("email", "test3@wp.pl");
        invalidUser.put("password", "test1234");
        mockMvc.perform(post("/auth/register").content(invalidUser.toString())
                .contentType(MediaType.parseMediaType("application/json")))
            .andExpect(status().isConflict());
    }

    @Test
    void shouldFailWhenRegisteringUserWithInvalidEmail() throws Exception {
        JSONObject invalidUser = new JSONObject();
        invalidUser.put("username", "test4");
        invalidUser.put("email", "test3");
        invalidUser.put("password", "test1234");
        mockMvc.perform(post("/auth/register").content(invalidUser.toString())
                .contentType(MediaType.parseMediaType("application/json")))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenRegisteringUserWithoutProvidingEmail() throws Exception {
        JSONObject invalidUser = new JSONObject();
        invalidUser.put("username", "test4");
        invalidUser.put("password", "test1234");
        mockMvc.perform(post("/auth/register").content(invalidUser.toString())
                .contentType(MediaType.parseMediaType("application/json")))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenRegisteringUserWithoutProvidingUsername() throws Exception {
        JSONObject invalidUser = new JSONObject();
        invalidUser.put("email", "test3@wp.pl");
        invalidUser.put("password", "test1234");
        mockMvc.perform(post("/auth/register").content(invalidUser.toString())
                .contentType(MediaType.parseMediaType("application/json")))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenRegisteringUserWithoutProvidingPassword() throws Exception {
        JSONObject invalidUser = new JSONObject();
        invalidUser.put("username", "test4");
        invalidUser.put("email", "test3@wp.pl");
        mockMvc.perform(post("/auth/register").content(invalidUser.toString())
                .contentType(MediaType.parseMediaType("application/json")))
            .andExpect(status().isBadRequest());
    }
    //endregion

    //region logging in
    @Test
    void shouldPassLoggingIn() throws Exception {
        JSONObject credentials = new JSONObject();
        credentials.put("username", "kamillo");
        credentials.put("password", "test1234");
        mockMvc.perform(post("/auth/login").content(credentials.toString())
                .contentType(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.jwt").isNotEmpty());
    }

    @Test
    void shouldFailLoggingInWithNonExistingUsername() throws Exception {
        JSONObject credentials = new JSONObject();
        credentials.put("username", "``````````");
        credentials.put("password", "test1234");
        mockMvc.perform(post("/auth/login").content(credentials.toString())
                .contentType(MediaType.parseMediaType("application/json")))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldFailLoggingInWithInvalidPassword() throws Exception {
        JSONObject credentials = new JSONObject();
        credentials.put("username", "kamillo");
        credentials.put("password", "``````````");
        mockMvc.perform(post("/auth/login").content(credentials.toString())
                .contentType(MediaType.parseMediaType("application/json")))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldFailLoggingInWithoutProvidingUsername() throws Exception {
        JSONObject credentials = new JSONObject();
        credentials.put("username", "kamillo");
        mockMvc.perform(post("/auth/login").content(credentials.toString())
                .contentType(MediaType.parseMediaType("application/json")))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailLoggingInWithoutProvidingPassword() throws Exception {
        JSONObject credentials = new JSONObject();
        credentials.put("password", "kamillo");
        mockMvc.perform(post("/auth/login").content(credentials.toString())
                .contentType(MediaType.parseMediaType("application/json")))
            .andExpect(status().isBadRequest());
    }
    //endregion

    //region change password
    
    //endregion

    //region delete account

    //endregion
}
