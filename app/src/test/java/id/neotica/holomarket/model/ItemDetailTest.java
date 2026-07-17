package id.neotica.holomarket.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class ItemDetailTest {

    @Test
    public void constructor_assignsAllFields() {
        ItemDetail d = new ItemDetail("det1", "Detail", "A description",
                "/d.png", 25000L, "2025-01-01");
        assertEquals("det1", d.getId());
        assertEquals("Detail", d.getName());
        assertEquals("A description", d.getDesc());
        assertEquals("/d.png", d.getImageUrl());
        assertEquals(Long.valueOf(25000L), d.getPrice());
        assertEquals("2025-01-01", d.getCreatedAt());
    }

    @Test
    public void nullPrice_isAllowed() {
        ItemDetail d = new ItemDetail("i", "N", "d", "/i.png", null, "2025-01-01");
        assertNull(d.getPrice());
    }

    @Test
    public void nullCreatedAt_isAllowed() {
        ItemDetail d = new ItemDetail("i", "N", "d", "/i.png", 1000L, null);
        assertNull(d.getCreatedAt());
    }

    @Test
    public void allNulls_doNotThrow() {
        ItemDetail d = new ItemDetail(null, null, null, null, null, null);
        assertNull(d.getId());
        assertNull(d.getName());
        assertNull(d.getDesc());
        assertNull(d.getImageUrl());
        assertNull(d.getPrice());
        assertNull(d.getCreatedAt());
    }
}