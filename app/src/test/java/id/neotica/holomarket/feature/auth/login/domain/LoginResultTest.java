package id.neotica.holomarket.feature.auth.login.domain;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LoginResultTest {

    @Test
    public void fromJson_parsesAllFields() throws Exception {
        LoginResult r = LoginResult.fromJson(new JSONObject(
                "{\"token\":\"jwt\",\"refreshToken\":\"ref\",\"expirationTime\":123}"));

        assertEquals("jwt", r.token);
        assertEquals("ref", r.refreshToken);
        assertEquals(123L, r.expirationTime);
    }

    @Test
    public void fromJson_missingFields_useDefaults() throws Exception {
        LoginResult r = LoginResult.fromJson(new JSONObject("{}"));

        assertNull(r.token);
        assertNull(r.refreshToken);
        assertEquals(0L, r.expirationTime);
    }

    @Test
    public void fromJson_nullObject_returnsNull() {
        assertNull(LoginResult.fromJson(null));
    }
}