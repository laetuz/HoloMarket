package id.neotica.holomarket.feature.downloads.domain;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class InstalledAppTest {

    @Test
    public void fromJson_parsesFields() throws Exception {
        JSONObject o = new JSONObject(
                "{\"package_name\":\"p1\",\"title\":\"App\",\"icon_url\":\"/i.png\",\"version_code\":9}");

        InstalledApp app = InstalledApp.fromJson(o, 5);

        assertEquals("p1", app.packageName);
        assertEquals("App", app.title);
        assertEquals("/i.png", app.iconUrl);
        assertEquals(9, app.latestVersionCode);
        assertEquals(5, app.installedVersionCode);
    }

    @Test
    public void fromJson_nullIcon_becomesEmpty() throws Exception {
        JSONObject o = new JSONObject(
                "{\"package_name\":\"p1\",\"title\":\"App\",\"icon_url\":null,\"version_code\":9}");

        assertEquals("", InstalledApp.fromJson(o, 5).iconUrl);
    }

    @Test
    public void fromJson_missingPackageOrNull_returnsNull() throws Exception {
        assertNull(InstalledApp.fromJson(new JSONObject("{\"title\":\"App\"}"), 1));
        assertNull(InstalledApp.fromJson(null, 1));
    }

    @Test
    public void hasUpdate_newerLatest_true() {
        assertTrue(new InstalledApp("p", "App", "", 9, 5).hasUpdate());
    }

    @Test
    public void hasUpdate_sameOrOlder_false() {
        assertFalse(new InstalledApp("p", "App", "", 5, 5).hasUpdate());
        assertFalse(new InstalledApp("p", "App", "", 4, 5).hasUpdate());
    }

    @Test
    public void hasUpdate_zeroVersions_false() {
        assertFalse(new InstalledApp("p", "App", "", 0, 5).hasUpdate());
        assertFalse(new InstalledApp("p", "App", "", 9, 0).hasUpdate());
    }
}