package id.neotica.holomarket.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class ItemModelTest {

    @Test
    public void constructor_assignsAllFields() {
        ItemModel item = new ItemModel("item1", "Widget", 9.99, 50,
                "A useful widget", "/w.png");
        assertEquals("item1", item.getId());
        assertEquals("Widget", item.getName());
        assertEquals(9.99, item.getPrice(), 0.001);
        assertEquals(50.0, item.getStock(), 0.001);
        assertEquals("A useful widget", item.getDescription());
        assertEquals("/w.png", item.getImageUrl());
    }

    @Test
    public void zeroPrice_isAllowed() {
        ItemModel item = new ItemModel("i", "N", 0.0, 0, "d", "/i.png");
        assertEquals(0.0, item.getPrice(), 0.001);
        assertEquals(0.0, item.getStock(), 0.001);
    }

    @Test
    public void negativeStock_isAllowed() {
        ItemModel item = new ItemModel("i", "N", 1.99, -5, "d", "/i.png");
        assertEquals(-5.0, item.getStock(), 0.001);
    }

    @Test
    public void nullFields_doNotThrow() {
        ItemModel item = new ItemModel(null, null, 0.0, 0, null, null);
        assertNull(item.getId());
        assertNull(item.getName());
        assertNull(item.getDescription());
        assertNull(item.getImageUrl());
    }
}