package id.neotica.holomarket.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class VersionModelTest {

    @Test
    public void constructor_assignsAllFields() {
        VersionModel v = new VersionModel("v1", "app1", "1.0", 10,
                "/dl.apk", "Bug fixes", 7, 21, 1000L);
        assertEquals("v1", v.id);
        assertEquals("app1", v.appId);
        assertEquals("1.0", v.versionName);
        assertEquals(10, v.versionCode);
        assertEquals("/dl.apk", v.fileUrl);
        assertEquals("Bug fixes", v.changelog);
        assertEquals(7, v.minSdk);
        assertEquals(21, v.maxSdk);
        assertEquals(1000L, v.createdAt);
    }

    @Test
    public void nullStrings_doNotThrow() {
        VersionModel v = new VersionModel(null, null, null, 0, null, null, 0, 0, 0);
        assertNull(v.id);
        assertNull(v.appId);
        assertNull(v.versionName);
        assertNull(v.fileUrl);
        assertNull(v.changelog);
    }

    @Test
    public void zeroVersionCode_isAllowed() {
        VersionModel v = new VersionModel("id", "a", "0.1", 0, null, "", 0, 0, 0);
        assertEquals(0, v.versionCode);
    }

    @Test
    public void negativeVersionCode_isAllowed() {
        VersionModel v = new VersionModel("id", "a", "0.1", -1, null, "", 0, 0, 0);
        assertEquals(-1, v.versionCode);
    }

    @Test
    public void emptyChangelog_isAllowed() {
        VersionModel v = new VersionModel("id", "a", "1.0", 1, "/x.apk", "", 7, 0, 0);
        assertEquals("", v.changelog);
    }
}