package vallegrande.edu.pe.controller;

import org.junit.jupiter.api.Test;
import vallegrande.edu.pe.model.LoginRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AuthControllerTest {

    private final AuthController authController = new AuthController();

    private LoginRequest request(String username, String password) {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);
        return loginRequest;
    }

    @Test
    void loginConCredencialesCorrectas() {
        Map<String, Object> response = authController.login(request("admin", "123456"));
        assertEquals(true, response.get("success"));
        assertEquals("Login correcto", response.get("message"));
        assertNotNull(response.get("token"));
    }

    @Test
    void loginConPasswordIncorrecta() {
        Map<String, Object> response = authController.login(request("admin", "000000"));
        assertEquals(false, response.get("success"));
        assertEquals("Credenciales incorrectas", response.get("message"));
        assertNull(response.get("token"));
    }

    @Test
    void loginConUsuarioInexistente() {
        Map<String, Object> response = authController.login(request("otro", "123456"));
        assertEquals(false, response.get("success"));
    }
}
