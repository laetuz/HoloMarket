package id.neotica.holomarket.utils;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class Base64UrlTest {

    @Test
    public void decode_jwtPayload() throws Exception {
        byte[] decoded = Base64Url.decode("eyJzdWIiOiJqb2huIn0");

        assertEquals("{\"sub\":\"john\"}", new String(decoded, "UTF-8"));
    }

    @Test
    public void decode_paddingIsIgnored() throws Exception {
        byte[] decoded = Base64Url.decode("eyJzdWIiOiJqb2huIn0=");

        assertEquals("{\"sub\":\"john\"}", new String(decoded, "UTF-8"));
    }

    @Test
    public void decode_urlSafeAlphabet() {
        assertArrayEquals(new byte[]{(byte) 0xFB, (byte) 0xFF}, Base64Url.decode("-_8"));
    }

    @Test
    public void decode_standardAlphabetAccepted() {
        assertArrayEquals(new byte[]{(byte) 0xFB, (byte) 0xFF}, Base64Url.decode("+/8="));
    }

    @Test
    public void decode_invalidLength_returnsNull() {
        assertNull(Base64Url.decode("A"));
    }

    @Test
    public void decode_invalidCharacter_returnsNull() {
        assertNull(Base64Url.decode("A*"));
    }

    @Test
    public void decode_nullOrEmpty() {
        assertNull(Base64Url.decode(null));
        assertEquals(0, Base64Url.decode("").length);
    }
}