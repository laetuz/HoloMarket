package id.neotica.holomarket.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AndroidVersionTest {

    @Test
    public void displayName_withCodename() {
        AndroidVersion v = new AndroidVersion(7, "2.1", "Eclair");
        assertEquals("Eclair (2.1)", v.displayName());
    }

    @Test
    public void displayName_withoutCodename() {
        AndroidVersion v = new AndroidVersion(30, "11", null);
        assertEquals("11", v.displayName());
    }

    @Test
    public void displayName_emptyCodename() {
        AndroidVersion v = new AndroidVersion(30, "11", "");
        assertEquals("11", v.displayName());
    }

    @Test
    public void displayName_nullVersion_isUnknown() {
        AndroidVersion v = new AndroidVersion(999, null, null);
        assertEquals("Unknown", v.displayName());
    }

    @Test
    public void toString_returnsDisplayName() {
        AndroidVersion v = new AndroidVersion(8, "2.2", "Froyo");
        assertEquals(v.displayName(), v.toString());
    }

    @Test
    public void constructor_assignsFields() {
        AndroidVersion v = new AndroidVersion(21, "5.0", "Lollipop");
        assertEquals(21, v.sdkLevel);
        assertEquals("5.0", v.version);
        assertEquals("Lollipop", v.codename);
    }
}