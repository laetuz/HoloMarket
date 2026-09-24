package id.neotica.holomarket.feature.detail.domain;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AppDetailModelTest {

    @Test
    public void fromJson_parsesAllFields() throws Exception {
        String json = "{\"title\":\"My App\",\"description\":\"Desc\",\"icon_url\":\"/i.png\","
                + "\"developer\":\"Dev\",\"categories\":[\"game\",\"tools\"],"
                + "\"screenshots\":[\"/s1.png\",\"/s2.png\"],"
                + "\"versions\":[{\"version_name\":\"1.0\",\"version_code\":5,\"file_url\":\"/a.apk\"},"
                + "{\"version_name\":\"2.0\",\"version_code\":9,\"file_url\":\"/b.apk\"}],"
                + "\"average_rating\":4.5,\"total_reviews\":10}";

        AppDetailModel m = AppDetailModel.fromJson("pkg", new JSONObject(json));

        assertEquals("pkg", m.packageName);
        assertEquals("My App", m.title);
        assertEquals("Desc", m.description);
        assertEquals("/i.png", m.iconUrl);
        assertEquals("Dev", m.developer);
        assertEquals(2, m.categories.size());
        assertEquals("game", m.categories.get(0));
        assertEquals(2, m.screenshots.size());
        assertEquals(2, m.versions.size());
        assertEquals(9, m.latestVersionCode());
        assertEquals(4.5, m.averageRating, 0.001);
        assertEquals(10, m.totalReviews);
    }

    @Test
    public void fromJson_fallsBackToSingleCategory() throws Exception {
        AppDetailModel m = AppDetailModel.fromJson("pkg", new JSONObject("{\"category\":\"utilities\"}"));

        assertEquals(1, m.categories.size());
        assertEquals("utilities", m.categories.get(0));
    }

    @Test
    public void fromJson_filtersEmptyScreenshots() throws Exception {
        AppDetailModel m = AppDetailModel.fromJson("pkg",
                new JSONObject("{\"screenshots\":[\"/a.png\",\"\",\"/b.png\"]}"));

        assertEquals(2, m.screenshots.size());
    }

    @Test
    public void fromJson_missingFields_useDefaults() throws Exception {
        AppDetailModel m = AppDetailModel.fromJson("pkg", new JSONObject("{}"));

        assertEquals("Unknown App", m.title);
        assertEquals("No description available.", m.description);
        assertEquals("", m.iconUrl);
        assertEquals("", m.developer);
        assertTrue(m.categories.isEmpty());
        assertTrue(m.screenshots.isEmpty());
        assertTrue(m.versions.isEmpty());
        assertEquals(0.0, m.averageRating, 0.001);
        assertEquals(0, m.totalReviews);
        assertEquals(-1, m.latestVersionCode());
    }

    @Test
    public void latestVersionCode_noVersions_isMinusOne() throws Exception {
        AppDetailModel m = AppDetailModel.fromJson("pkg", new JSONObject("{\"versions\":[]}"));

        assertEquals(-1, m.latestVersionCode());
    }
}