package id.neotica.holomarket.model;

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
}