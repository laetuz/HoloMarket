package id.neotica.holomarket.feature.settings.presenter;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import id.neotica.holomarket.BuildConfig;
import id.neotica.holomarket.feature.settings.contract.SettingsView;
import id.neotica.holomarket.network.ApiCallback;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SettingsPresenterTest {

    @Mock Context mockContext;
    @Mock SharedPreferences mockPrefs;
    @Mock SharedPreferences.Editor mockEditor;
    @Mock SettingsView mockView;

    private TestPresenter presenter;

    private static class TestPresenter extends SettingsPresenter {
        ApiCallback latestCallback;
        int latestCalls;
        int downloadCalls;
        String lastDownloadFile;
        String lastDownloadUrl;

        TestPresenter(Context context) {
            super(context);
        }

        @Override
        void requestLatest(ApiCallback callback) {
            latestCalls++;
            latestCallback = callback;
        }

        @Override
        void startDownload(String pkg, String fileName, String appTitle, String icon, String downloadUrl) {
            downloadCalls++;
            lastDownloadFile = fileName;
            lastDownloadUrl = downloadUrl;
        }
    }

    @Before
    public void setUp() {
        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs);
        when(mockPrefs.edit()).thenReturn(mockEditor);
        when(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor);
        when(mockEditor.putBoolean(anyString(), anyBoolean())).thenReturn(mockEditor);
        when(mockEditor.putLong(anyString(), anyLong())).thenReturn(mockEditor);
        when(mockEditor.remove(anyString())).thenReturn(mockEditor);
        when(mockEditor.commit()).thenReturn(true);

        presenter = new TestPresenter(mockContext);
        presenter.attach(mockView);
    }

    @Test
    public void load_rendersProfileAdultStateAndVersion() {
        when(mockPrefs.getString("username", null)).thenReturn("john");
        when(mockPrefs.getBoolean("adult_content_enabled", false)).thenReturn(false);

        presenter.load();

        verify(mockView).renderProfile("john");
        verify(mockView).renderAdultContent(false);
        verify(mockView).renderVersion(anyString(), anyInt());
    }

    @Test
    public void load_noUsername_rendersUser() {
        when(mockPrefs.getString("username", null)).thenReturn(null);
        when(mockPrefs.getString("jwt_token", null)).thenReturn(null);

        presenter.load();

        verify(mockView).renderProfile("User");
    }

    @Test
    public void load_adultEnabled_rendersChecked() {
        when(mockPrefs.getString("username", null)).thenReturn("john");
        when(mockPrefs.getBoolean("adult_content_enabled", false)).thenReturn(true);

        presenter.load();

        verify(mockView).renderAdultContent(true);
    }

    @Test
    public void logout_clearsSessionAndNavigates() {
        presenter.logout();

        verify(mockEditor).remove("jwt_token");
        verify(mockEditor).remove("refresh_token");
        verify(mockEditor).remove("expiration_time");
        verify(mockEditor).remove("username");
        verify(mockEditor).commit();
        verify(mockView).navigateToMain();
    }

    @Test
    public void setAdultContent_true_persists() {
        presenter.setAdultContent(true);

        verify(mockEditor).putBoolean("adult_content_enabled", true);
        verify(mockEditor).commit();
    }

    @Test
    public void setAdultContent_false_persists() {
        presenter.setAdultContent(false);

        verify(mockEditor).putBoolean("adult_content_enabled", false);
        verify(mockEditor).commit();
    }

    @Test
    public void verifyAdultPassword_acceptsCorrectPassword() {
        assertTrue(presenter.verifyAdultPassword("adult"));
    }

    @Test
    public void verifyAdultPassword_rejectsWrongPassword() {
        assertFalse(presenter.verifyAdultPassword("nope"));
    }

    @Test
    public void checkForUpdates_updateAvailable_andDownload() {
        presenter.checkForUpdates();
        assertTrue(presenter.latestCalls == 1);

        String newer = "{\"version_name\":\"9.9.9\",\"version_code\":"
                + (BuildConfig.VERSION_CODE + 1) + ",\"file_url\":\"/x/holomarket.apk\"}";
        presenter.latestCallback.onSuccess(newer);

        verify(mockView).showUpdateAvailable("9.9.9");

        presenter.downloadUpdate();

        assertTrue(presenter.downloadCalls == 1);
        org.junit.Assert.assertEquals("holomarket_v9.9.9.apk", presenter.lastDownloadFile);
        org.junit.Assert.assertEquals(BuildConfig.FILE_BASE_URL + "/x/holomarket.apk", presenter.lastDownloadUrl);
    }

    @Test
    public void checkForUpdates_sameVersion_showsUpToDate() {
        presenter.checkForUpdates();

        String current = "{\"version_name\":\"1.0\",\"version_code\":"
                + BuildConfig.VERSION_CODE + ",\"file_url\":\"/x.apk\"}";
        presenter.latestCallback.onSuccess(current);

        verify(mockView).showUpToDate();
    }

    @Test
    public void checkForUpdates_olderVersion_showsUpToDate() {
        presenter.checkForUpdates();

        presenter.latestCallback.onSuccess("{\"version_name\":\"0.1\",\"version_code\":1,\"file_url\":\"/x.apk\"}");

        verify(mockView).showUpToDate();
    }

    @Test
    public void checkForUpdates_parseError_showsMessage() {
        presenter.checkForUpdates();

        presenter.latestCallback.onSuccess("not json");

        verify(mockView).showMessage("Error checking for updates.");
    }

    @Test
    public void checkForUpdates_requestError_showsMessage() {
        presenter.checkForUpdates();

        presenter.latestCallback.onError("boom");

        verify(mockView).showMessage("Failed to check for updates: boom");
    }

    @Test
    public void detach_thenCallback_doesNotTouchView() {
        presenter.checkForUpdates();

        presenter.detach();
        reset(mockView);

        presenter.latestCallback.onSuccess("{\"version_code\":999}");

        verifyNoMoreInteractions(mockView);
    }
}