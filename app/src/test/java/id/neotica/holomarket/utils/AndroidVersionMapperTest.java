package id.neotica.holomarket.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AndroidVersionMapperTest {

    @Test
    public void cupcake() {
        AndroidVersion v = AndroidVersionMapper.fromSdkLevel(3);

        assertEquals("1.5", v.version);
        assertEquals("Cupcake", v.codename);
    }

    @Test
    public void froyo() {
        AndroidVersion v = AndroidVersionMapper.fromSdkLevel(8);

        assertEquals("2.2", v.version);
        assertEquals("Froyo", v.codename);
        assertEquals("Froyo (2.2)", v.displayName());
    }

    @Test
    public void eclairSplit() {
        assertEquals("2.0", AndroidVersionMapper.fromSdkLevel(5).version);
        assertEquals("2.0.1", AndroidVersionMapper.fromSdkLevel(6).version);
        assertEquals("2.1", AndroidVersionMapper.fromSdkLevel(7).version);
    }

    @Test
    public void modernLevel_hasNoCodename() {
        AndroidVersion v = AndroidVersionMapper.fromSdkLevel(30);

        assertEquals("11", v.version);
        assertNull(v.codename);
        assertEquals("11", v.displayName());
    }

    @Test
    public void unknownLevel_isUnknown() {
        assertEquals("Unknown", AndroidVersionMapper.fromSdkLevel(999).displayName());
        assertEquals("Unknown", AndroidVersionMapper.fromSdkLevel(-1).displayName());
    }
}