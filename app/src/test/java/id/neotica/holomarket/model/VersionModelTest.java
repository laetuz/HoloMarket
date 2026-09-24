package id.neotica.holomarket.model;

import org.json.JSONObject;
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

    @Test
    public void fromJson_parsesAllFields() throws Exception {
        String json = "{\"id\":\"v1\",\"app_id\":\"app1\",\"version_name\":\"1.0\",\"version_code\":10,"
                + "\"file_url\":\"/dl.apk\",\"changelog\":\"Bug fixes\",\"min_sdk\":7,\"max_sdk\":21,"
                + "\"created_at\":1000}";
        VersionModel v = VersionModel.fromJson(new JSONObject(json));
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
    public void fromJson_missingFields_useDefaults() throws Exception {
        VersionModel v = VersionModel.fromJson(new JSONObject("{}"));
        assertEquals("", v.id);
        assertEquals("", v.versionName);
        assertEquals(0, v.versionCode);
        assertEquals(0L, v.createdAt);
    }

    @Test
    public void fromJson_nullObject_returnsNull() {
        assertNull(VersionModel.fromJson(null));
    }
}