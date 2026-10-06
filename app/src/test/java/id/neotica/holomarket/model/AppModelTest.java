package id.neotica.holomarket.model;

import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public class AppModelTest {

    @Test
    public void constructor_assignsAllFields() {
        AppModel app = new AppModel("id.neotica.neomart", "Neomart",
                "The official e-commerce store", "/icon.png", "utilities");
        assertEquals("id.neotica.neomart", app.packageName);
        assertEquals("Neomart", app.title);
        assertEquals("The official e-commerce store", app.description);
        assertEquals("/icon.png", app.iconUrl);
        assertEquals("utilities", app.category);
    }

    @Test
    public void nullIconUrl_isAllowed() {
        AppModel app = new AppModel("pkg", "App", "Desc", null, "game");
        assertNull(app.iconUrl);
    }

    @Test
    public void nullCategory_isAllowed() {
        AppModel app = new AppModel("pkg", "App", "Desc", "/i.png", null);
        assertNull(app.category);
    }

    @Test
    public void emptyStrings_doNotThrow() {
        AppModel app = new AppModel("", "", "", "", "");
        assertEquals("", app.packageName);
        assertEquals("", app.title);
        assertEquals("", app.description);
        assertEquals("", app.iconUrl);
        assertEquals("", app.category);
    }

    @Test
    public void fromJson_parsesAllFields() throws Exception {
        AppModel app = AppModel.fromJson(new JSONObject(
                "{\"package_name\":\"p\",\"title\":\"T\",\"description\":\"D\",\"icon_url\":\"/i.png\","
                        + "\"category\":\"game\",\"developer\":\"Dev\","
                        + "\"categories\":[\"a\",\"b\"],\"screenshots\":[\"/s.png\"]}"));

        assertEquals("p", app.packageName);
        assertEquals("T", app.title);
        assertEquals("D", app.description);
        assertEquals("/i.png", app.iconUrl);
        assertEquals("game", app.category);
        assertEquals("Dev", app.developer);
        assertEquals(2, app.categories.size());
        assertEquals(1, app.screenshots.size());
    }

    @Test
    public void fromJson_nullIconUrl_becomesEmpty() throws Exception {
        AppModel app = AppModel.fromJson(new JSONObject("{\"package_name\":\"p\",\"icon_url\":null}"));

        assertEquals("", app.iconUrl);
    }

    @Test
    public void fromJson_missingFields_useDefaults() throws Exception {
        AppModel app = AppModel.fromJson(new JSONObject("{}"));

        assertEquals("", app.packageName);
        assertEquals("", app.title);
        assertTrue(app.categories.isEmpty());
        assertTrue(app.screenshots.isEmpty());
    }

    @Test
    public void fromJson_nullObject_returnsNull() {
        assertNull(AppModel.fromJson(null));
    }
}