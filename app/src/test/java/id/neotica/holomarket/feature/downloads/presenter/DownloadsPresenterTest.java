package id.neotica.holomarket.feature.downloads.presenter;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.neotica.holomarket.feature.downloads.contract.DownloadsView;
import id.neotica.holomarket.feature.downloads.domain.DownloadInfo;
import id.neotica.holomarket.feature.downloads.domain.InstalledApp;
import id.neotica.holomarket.feature.downloads.service.DownloadService;
import id.neotica.holomarket.network.ApiCallback;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DownloadsPresenterTest {

    @Mock Context mockContext;
    @Mock SharedPreferences mockPrefs;
    @Mock DownloadsView mockView;

    private TestPresenter presenter;

    private static final String TASKS_JSON =
            "[{\"package\":\"p1\",\"app_name\":\"A\",\"percent\":42,\"status_text\":\"42%\"},"
                    + "{\"package\":\"p2\",\"app_name\":\"B\",\"percent\":100,\"installing\":true}]";

    private static class TestPresenter extends DownloadsPresenter {
        String lastCancelled;
        int cancelCalls;
        Map<String, Integer> installedVersions = new HashMap<String, Integer>();
        String lookupResponse = "[]";
        String lookupError;
        String lastLookupBody;
        int lookupCalls;

        TestPresenter(Context context) {
            super(context);
        }

        @Override
        void sendCancel(String packageName) {
            cancelCalls++;
            lastCancelled = packageName;
        }

        @Override
        Map<String, Integer> getInstalledVersions() {
            return installedVersions;
        }

        @Override
        void requestLookup(String jsonBody, ApiCallback callback) {
            lookupCalls++;
            lastLookupBody = jsonBody;
            if (lookupError != null) {
                callback.onError(lookupError);
            } else {
                callback.onSuccess(lookupResponse);
            }
        }
    }

    @Before
    public void setUp() {
        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs);
        when(mockPrefs.getString(eq(DownloadService.KEY_TASKS), anyString())).thenReturn(TASKS_JSON);

        presenter = new TestPresenter(mockContext);
        presenter.attach(mockView);
    }

    // --- persisted tasks ---

    @Test
    public void refresh_parsesTasks() {
        presenter.refresh();

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(mockView).renderTasks(captor.capture());
        List<DownloadInfo> tasks = captor.getValue();

        assertEquals(2, tasks.size());
        assertEquals("p1", tasks.get(0).packageName);
        assertEquals("A", tasks.get(0).appName);
        assertEquals(42, tasks.get(0).percent);
        assertEquals("p2", tasks.get(1).packageName);
        assertEquals(true, tasks.get(1).installing);
    }

    @Test
    public void refresh_empty_rendersEmptyList() {
        when(mockPrefs.getString(eq(DownloadService.KEY_TASKS), anyString())).thenReturn("[]");

        presenter.refresh();

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(mockView).renderTasks(captor.capture());
        assertEquals(0, captor.getValue().size());
    }

    @Test
    public void refresh_malformed_rendersEmptyList() {
        when(mockPrefs.getString(eq(DownloadService.KEY_TASKS), anyString())).thenReturn("not json");

        presenter.refresh();

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(mockView).renderTasks(captor.capture());
        assertEquals(0, captor.getValue().size());
    }

    @Test
    public void cancel_sendsCancelForPackage() {
        presenter.cancel("p1");

        assertEquals(1, presenter.cancelCalls);
        assertEquals("p1", presenter.lastCancelled);
    }

    @Test
    public void detach_thenRefresh_doesNotTouchView() {
        presenter.detach();
        reset(mockView);

        presenter.refresh();

        verifyNoMoreInteractions(mockView);
    }

    // --- installed apps lookup ---

    @Test
    public void loadInstalledApps_noInstalledPackages_rendersEmptyWithoutLookup() {
        presenter.loadInstalledApps();

        assertEquals(0, presenter.lookupCalls);

        ArgumentCaptor<List> updatesCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List> installedCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockView).renderInstalledApps(updatesCaptor.capture(), installedCaptor.capture());
        assertEquals(0, updatesCaptor.getValue().size());
        assertEquals(0, installedCaptor.getValue().size());
    }

    @Test
    public void loadInstalledApps_splitsUpdatesInstalledAndSkipsUnknown() {
        presenter.installedVersions.put("p1", 1);
        presenter.installedVersions.put("p2", 5);
        presenter.installedVersions.put("p3", 7);
        presenter.lookupResponse = "["
                + "{\"package_name\":\"p3\",\"title\":\"Zeta\",\"version_code\":6},"
                + "{\"package_name\":\"p1\",\"title\":\"Alpha\",\"version_code\":3},"
                + "{\"package_name\":\"p2\",\"title\":\"Beta\",\"version_code\":5},"
                + "{\"package_name\":\"p4\",\"title\":\"Ghost\",\"version_code\":9}]";

        presenter.loadInstalledApps();

        assertEquals(1, presenter.lookupCalls);

        ArgumentCaptor<List> updatesCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List> installedCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockView).renderInstalledApps(updatesCaptor.capture(), installedCaptor.capture());

        List<InstalledApp> updates = updatesCaptor.getValue();
        List<InstalledApp> installed = installedCaptor.getValue();

        assertEquals(1, updates.size());
        assertEquals("p1", updates.get(0).packageName);
        assertEquals(3, updates.get(0).latestVersionCode);
        assertEquals(1, updates.get(0).installedVersionCode);

        assertEquals(2, installed.size());
        assertEquals("Beta", installed.get(0).title);
        assertEquals("Zeta", installed.get(1).title);
    }

    @Test
    public void loadInstalledApps_lookupError_rendersEmptyLists() {
        presenter.installedVersions.put("p1", 1);
        presenter.lookupError = "NETWORK_ERROR_x";

        presenter.loadInstalledApps();

        assertEquals(1, presenter.lookupCalls);

        ArgumentCaptor<List> updatesCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List> installedCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockView).renderInstalledApps(updatesCaptor.capture(), installedCaptor.capture());
        assertEquals(0, updatesCaptor.getValue().size());
        assertEquals(0, installedCaptor.getValue().size());
    }

    @Test
    public void loadInstalledApps_bodyContainsInstalledPackageNames() throws Exception {
        presenter.installedVersions.put("p1", 1);
        presenter.installedVersions.put("p2", 2);

        presenter.loadInstalledApps();

        JSONObject body = new JSONObject(presenter.lastLookupBody);
        JSONArray names = body.getJSONArray("package_names");
        List<String> sent = new ArrayList<String>();
        for (int i = 0; i < names.length(); i++) {
            sent.add(names.getString(i));
        }
        assertEquals(2, sent.size());
        assertTrue(sent.contains("p1"));
        assertTrue(sent.contains("p2"));
    }

    @Test
    public void loadInstalledAppsIfStale_justScanned_doesNotRescan() {
        presenter.installedVersions.put("p1", 1);
        presenter.loadInstalledApps();

        presenter.loadInstalledAppsIfStale();

        assertEquals(1, presenter.lookupCalls);
    }
}