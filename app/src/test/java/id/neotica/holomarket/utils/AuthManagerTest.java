package id.neotica.holomarket.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuthManagerTest {

    @Mock Context mockContext;
    @Mock SharedPreferences mockPrefs;
    @Mock SharedPreferences.Editor mockEditor;

    private AuthManager auth;

    @Before
    public void setUp() {
        when(mockContext.getSharedPreferences(anyString(), anyInt()))
                .thenReturn(mockPrefs);
        when(mockPrefs.edit()).thenReturn(mockEditor);
        when(mockEditor.putString(anyString(), anyString()))
                .thenReturn(mockEditor);
        when(mockEditor.putBoolean(anyString(), anyBoolean()))
                .thenReturn(mockEditor);
        when(mockEditor.putLong(anyString(), anyLong()))
                .thenReturn(mockEditor);
        when(mockEditor.remove(anyString()))
                .thenReturn(mockEditor);
        auth = new AuthManager(mockContext);
    }

    @Test
    public void saveToken_storesToPrefs() {
        auth.saveToken("my.jwt.token");
        verify(mockEditor).putString("jwt_token", "my.jwt.token");
        verify(mockEditor).commit();
    }

    @Test
    public void getToken_returnsSavedToken() {
        when(mockPrefs.getString("jwt_token", null)).thenReturn("my.jwt");
        assertEquals("my.jwt", auth.getToken());
    }

    @Test
    public void getToken_returnsNullWhenEmpty() {
        when(mockPrefs.getString("jwt_token", null)).thenReturn(null);
        assertNull(auth.getToken());
    }

    @Test
    public void isLoggedIn_trueWithToken() {
        when(mockPrefs.getString("jwt_token", null)).thenReturn("some.token");
        assertTrue(auth.isLoggedIn());
    }

    @Test
    public void isLoggedIn_falseWithoutToken() {
        when(mockPrefs.getString("jwt_token", null)).thenReturn(null);
        assertFalse(auth.isLoggedIn());
    }

    @Test
    public void saveUsername_storesToPrefs() {
        auth.saveUsername("john");
        verify(mockEditor).putString("username", "john");
        verify(mockEditor).commit();
    }

    @Test
    public void getUsername_returnsSavedName() {
        when(mockPrefs.getString("username", null)).thenReturn("john");
        assertEquals("john", auth.getUsername());
    }

    @Test
    public void getUsername_returnsNullWhenEmpty() {
        when(mockPrefs.getString("username", null)).thenReturn(null);
        assertNull(auth.getUsername());
    }

    @Test
    public void adultContent_disabledByDefault() {
        when(mockPrefs.getBoolean("adult_content_enabled", false)).thenReturn(false);
        assertFalse(auth.isAdultContentEnabled());
    }

    @Test
    public void setAdultContentEnabled_true_persists() {
        auth.saveAdultContentEnabled(true);
        verify(mockEditor).putBoolean("adult_content_enabled", true);
        verify(mockEditor).commit();
    }

    @Test
    public void adultContent_enabled_returnsTrue() {
        when(mockPrefs.getBoolean("adult_content_enabled", false)).thenReturn(true);
        assertTrue(auth.isAdultContentEnabled());
    }

    @Test
    public void setAdultContentEnabled_false_persists() {
        auth.saveAdultContentEnabled(false);
        verify(mockEditor).putBoolean("adult_content_enabled", false);
        verify(mockEditor).commit();
    }

    @Test
    public void getAuthHeaders_includesBearerToken() {
        when(mockPrefs.getString("jwt_token", null)).thenReturn("jwt.val");
        Map<String, String> headers = auth.getAuthHeaders();
        assertNotNull(headers);
        assertEquals("Bearer jwt.val", headers.get("Authorization"));
    }

    @Test
    public void getAuthHeaders_returnsNullWhenNoToken() {
        when(mockPrefs.getString("jwt_token", null)).thenReturn(null);
        assertNull(auth.getAuthHeaders());
    }

    @Test
    public void clear_removesAllKeys() {
        auth.clear();
        verify(mockEditor).remove("jwt_token");
        verify(mockEditor).remove("refresh_token");
        verify(mockEditor).remove("expiration_time");
        verify(mockEditor).remove("username");
        verify(mockEditor).commit();
    }

    @Test
    public void saveRefreshToken_thenGetRefreshToken_roundtrip() {
        auth.saveRefreshToken("my.refresh.token");
        verify(mockEditor).putString("refresh_token", "my.refresh.token");
        when(mockPrefs.getString("refresh_token", null)).thenReturn("my.refresh.token");
        assertEquals("my.refresh.token", auth.getRefreshToken());
    }

    @Test
    public void saveExpirationTime_thenGetExpirationTime_roundtrip() {
        auth.saveExpirationTime(123456789L);
        verify(mockEditor).putLong("expiration_time", 123456789L);
        when(mockPrefs.getLong("expiration_time", 0)).thenReturn(123456789L);
        assertEquals(123456789L, auth.getExpirationTime());
    }

    @Test
    public void getRefreshToken_returnsNullWhenEmpty() {
        when(mockPrefs.getString("refresh_token", null)).thenReturn(null);
        assertNull(auth.getRefreshToken());
    }

    @Test
    public void getExpirationTime_returnsZeroWhenEmpty() {
        when(mockPrefs.getLong("expiration_time", 0)).thenReturn(0L);
        assertEquals(0L, auth.getExpirationTime());
    }
}