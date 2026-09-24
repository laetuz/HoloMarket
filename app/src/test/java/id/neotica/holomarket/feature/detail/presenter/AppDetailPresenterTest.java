package id.neotica.holomarket.feature.detail.presenter;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import id.neotica.holomarket.BuildConfig;
import id.neotica.holomarket.feature.detail.contract.AppDetailView;
import id.neotica.holomarket.feature.detail.domain.AppDetailModel;
import id.neotica.holomarket.feature.detail.domain.InstallState;
import id.neotica.holomarket.model.VersionModel;
import id.neotica.holomarket.network.ApiCallback;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AppDetailPresenterTest {

    @Mock Context mockContext;
    @Mock SharedPreferences mockPrefs;
    @Mock
    AppDetailView mockView;

    private TestPresenter presenter;

    private static final String DETAIL_JSON =
            "{\"title\":\"My App\",\"description\":\"Desc\",\"developer\":\"Dev\","
                    + "\"categories\":[\"game\"],\"screenshots\":[\"/s1.png\"],"
                    + "\"versions\":[{\"version_name\":\"1.0\",\"version_code\":5,\"file_url\":\"/apps/a.apk\"}],"
                    + "\"average_rating\":4.5,\"total_reviews\":10}";

    /**
     * Overrides the I/O seams so the presenter logic runs synchronously without ApiTask/DownloadTask.
     */
    private static class TestPresenter extends AppDetailPresenter {
        ApiCallback appDetailCallback;
        int appDetailCalls;
        int installedVersionCode = -1;
        int downloadCalls;
        String lastDownloadFile;
        String lastDownloadTitle;
        String lastDownloadUrl;

        TestPresenter(Context context) {
            super(context);
        }

        @Override
        void requestAppDetail(String pkg, ApiCallback callback) {
            appDetailCalls++;
            appDetailCallback = callback;
        }

        @Override
        int getInstalledVersionCode(String pkg) {
            return installedVersionCode;
        }

        @Override
        void startDownload(String fileName, String appTitle, String downloadUrl) {
            downloadCalls++;
            lastDownloadFile = fileName;
            lastDownloadTitle = appTitle;
            lastDownloadUrl = downloadUrl;
        }
    }

    @Before
    public void setUp() {
        // AnalyticsTracker.track() builds an AuthManager(context) — give it a (logged-out) prefs mock.
        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs);
        when(mockPrefs.getString("jwt_token", null)).thenReturn(null);

        presenter = new TestPresenter(mockContext);
        presenter.attach(mockView);
    }

    // --- load ---

    @Test
    public void load_success_rendersDetailAndDownloadState() {
        presenter.installedVersionCode = -1;

        presenter.load("pkg");
        verify(mockView).showLoading();

        presenter.appDetailCallback.onSuccess(DETAIL_JSON);

        ArgumentCaptor<AppDetailModel> captor = ArgumentCaptor.forClass(AppDetailModel.class);
        verify(mockView).renderAppDetail(captor.capture());
        AppDetailModel model = captor.getValue();
        assertEquals("pkg", model.packageName);
        assertEquals("My App", model.title);
        assertEquals("Dev", model.developer);
        assertEquals(1, model.categories.size());
        assertEquals(1, model.screenshots.size());
        assertEquals(1, model.versions.size());
        assertEquals(5, model.latestVersionCode());
        assertEquals(4.5, model.averageRating, 0.001);
        assertEquals(10, model.totalReviews);

        verify(mockView).renderInstallState(InstallState.DOWNLOAD);
    }

    @Test
    public void load_installedUpToDate_rendersOpen() {
        presenter.installedVersionCode = 5;

        presenter.load("pkg");
        presenter.appDetailCallback.onSuccess(DETAIL_JSON);

        verify(mockView).renderInstallState(InstallState.OPEN);
    }

    @Test
    public void load_installedOlder_rendersUpdate() {
        presenter.installedVersionCode = 3;

        presenter.load("pkg");
        presenter.appDetailCallback.onSuccess(DETAIL_JSON);

        verify(mockView).renderInstallState(InstallState.UPDATE);
    }

    @Test
    public void load_error_extractsMessageAfterPipe() {
        presenter.load("pkg");

        presenter.appDetailCallback.onError("HTTP_ERROR_500|Server exploded");

        verify(mockView).showLoadError("Server exploded");
    }

    @Test
    public void load_error_withoutPipe_passesRawMessage() {
        presenter.load("pkg");

        presenter.appDetailCallback.onError("Failed to connect. Check internet.");

        verify(mockView).showLoadError("Failed to connect. Check internet.");
    }

    // --- download ---

    @Test
    public void onDownloadClicked_whenOpen_opensApp() {
        presenter.installedVersionCode = 5;
        presenter.load("pkg");
        presenter.appDetailCallback.onSuccess(DETAIL_JSON);

        presenter.onDownloadClicked();

        verify(mockView).openApp("pkg");
        assertEquals(0, presenter.downloadCalls);
    }

    @Test
    public void onDownloadClicked_noVersions_showsNoVersions() {
        presenter.installedVersionCode = -1;
        presenter.load("pkg");
        presenter.appDetailCallback.onSuccess("{\"versions\":[]}");

        presenter.onDownloadClicked();

        verify(mockView).showNoVersions();
        assertEquals(0, presenter.downloadCalls);
    }

    @Test
    public void onDownloadClicked_downloadsLatestVersion() {
        presenter.installedVersionCode = -1;
        presenter.load("pkg");
        presenter.appDetailCallback.onSuccess(DETAIL_JSON);

        presenter.onDownloadClicked();

        assertEquals(1, presenter.downloadCalls);
        assertEquals("a.apk", presenter.lastDownloadFile);
        assertEquals("My App", presenter.lastDownloadTitle);
        assertEquals(BuildConfig.FILE_BASE_URL + "/apps/a.apk", presenter.lastDownloadUrl);
    }

    @Test
    public void downloadVersion_emptyFileUrl_showsNoDownloadLink() {
        presenter.downloadVersion(new VersionModel("id", "app", "1.0", 1, "", "", 0, 0, 0));

        verify(mockView).showNoDownloadLink();
        assertEquals(0, presenter.downloadCalls);
    }

    @Test
    public void downloadVersion_nonApkFileName_usesFallbackName() {
        presenter.downloadVersion(new VersionModel("id", "app", "1.0", 7, "/apps/noext", "", 0, 0, 0));

        assertEquals(1, presenter.downloadCalls);
        assertEquals("update_v7.apk", presenter.lastDownloadFile);
    }

    // --- lifecycle ---

    @Test
    public void detach_thenCallback_doesNotTouchView() {
        presenter.load("pkg");

        presenter.detach();
        reset(mockView);

        presenter.appDetailCallback.onSuccess(DETAIL_JSON);

        verifyNoMoreInteractions(mockView);
    }
}