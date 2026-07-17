package id.neotica.holomarket.network;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import id.neotica.holomarket.BuildConfig;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AnalyticsTrackerTest {

    @Mock Context mockContext;
    @Mock SharedPreferences mockPrefs;

    @Before
    public void setUp() {
        when(mockContext.getSharedPreferences(anyString(), anyInt()))
                .thenReturn(mockPrefs);
        when(mockPrefs.getString(anyString(), anyString())).thenReturn(null);
    }

    @Test
    public void track_skipsWhenNotLoggedIn() {
        AnalyticsTracker.track(mockContext, "feature_use", "test_event");
    }

    @Test
    public void track_skipsWhenTokenIsNull() {
        AnalyticsTracker.track(mockContext, "download", "test_download");
    }

    @Test
    public void eventsUrl_usesNeometricsBaseUrl() {
        String expected = BuildConfig.NEOMETRICS_BASE_URL + "/analytics/events";
        assertEquals(expected, AnalyticsTracker.EVENTS_URL);
    }
}