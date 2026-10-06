package id.neotica.holomarket.feature.home.presenter;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import id.neotica.holomarket.feature.home.contract.HomeView;
import id.neotica.holomarket.feature.home.domain.AppTopic;
import id.neotica.holomarket.network.ApiCallback;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HomePresenterTest {

    @Mock Context mockContext;
    @Mock SharedPreferences mockPrefs;
    @Mock HomeView mockView;

    private TestPresenter presenter;

    private static class TestPresenter extends HomePresenter {
        ApiCallback featuredCallback;
        ApiCallback organizerCallback;
        ApiCallback collectionCallback;
        String lastCollectionSlug;
        int featuredCalls;
        int organizerCalls;
        int collectionCalls;

        TestPresenter(Context context) {
            super(context);
        }

        @Override
        void requestFeatured(ApiCallback callback) {
            featuredCalls++;
            featuredCallback = callback;
        }

        @Override
        void requestOrganizer(ApiCallback callback) {
            organizerCalls++;
            organizerCallback = callback;
        }

        @Override
        void requestCollection(String slug, ApiCallback callback) {
            collectionCalls++;
            lastCollectionSlug = slug;
            collectionCallback = callback;
        }
    }

    @Before
    public void setUp() {
        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs);
        presenter = new TestPresenter(mockContext);
        presenter.attach(mockView);
    }

    private void loggedIn(boolean loggedIn) {
        when(mockPrefs.getString("jwt_token", null)).thenReturn(loggedIn ? "jwt" : null);
    }

    private void adultEnabled(boolean enabled) {
        when(mockPrefs.getBoolean("adult_content_enabled", false)).thenReturn(enabled);
    }

    @Test
    public void load_loggedOut_showsLogin_andSectionsWithoutAdult() {
        loggedIn(false);
        adultEnabled(false);

        presenter.load();

        verify(mockView).showLogin();

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(mockView).renderSections(captor.capture());
        List topics = captor.getValue();
        assertEquals(2, topics.size());

        assertEquals(1, presenter.featuredCalls);
        assertEquals(0, presenter.organizerCalls);
    }

    @Test
    public void load_loggedIn_showsSettings_andLoadsOrganizer() {
        loggedIn(true);

        presenter.load();

        verify(mockView).showSettings();
        assertEquals(1, presenter.featuredCalls);
        assertEquals(1, presenter.organizerCalls);
    }

    @Test
    public void load_adultEnabled_includesAdultTopic() {
        loggedIn(false);
        adultEnabled(true);

        presenter.load();

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(mockView).renderSections(captor.capture());
        List<AppTopic> topics = captor.getValue();
        assertEquals(3, topics.size());
        assertEquals("Adult", topics.get(2).displayName);
    }

    @Test
    public void loadFeatured_success_renders() {
        loggedIn(false);
        presenter.load();

        presenter.featuredCallback.onSuccess("{\"data\":[{\"package_name\":\"p\"}]}");

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(mockView).renderFeatured(captor.capture());
        assertEquals(1, captor.getValue().size());
    }

    @Test
    public void loadFeatured_empty_doesNotRender() {
        loggedIn(false);
        presenter.load();

        presenter.featuredCallback.onSuccess("{\"data\":[]}");

        verify(mockView, never()).renderFeatured(anyList());
    }

    @Test
    public void loadFeatured_error_showsExtractedMessage() {
        loggedIn(false);
        presenter.load();

        presenter.featuredCallback.onError("HTTP_ERROR_500|Server exploded");

        verify(mockView).showError("Server exploded");
    }

    @Test
    public void loadOrganizer_rendersSection() {
        loggedIn(true);
        presenter.load();

        presenter.organizerCallback.onSuccess("[{\"slug\":\"s\",\"title\":\"Top Picks\"}]");
        assertEquals(1, presenter.collectionCalls);
        assertEquals("s", presenter.lastCollectionSlug);

        presenter.collectionCallback.onSuccess("{\"data\":[{\"package_name\":\"p\"}]}");

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(mockView).renderOrganizerSection(eq("Top Picks"), captor.capture());
        assertEquals(1, captor.getValue().size());
    }

    @Test
    public void loadOrganizer_emptyCollection_doesNotRender() {
        loggedIn(true);
        presenter.load();

        presenter.organizerCallback.onSuccess("[{\"slug\":\"s\",\"title\":\"Empty\"}]");
        presenter.collectionCallback.onSuccess("{\"data\":[]}");

        verify(mockView, never()).renderOrganizerSection(anyString(), anyList());
    }

    @Test
    public void detach_thenCallback_doesNotTouchView() {
        loggedIn(false);
        presenter.load();

        presenter.detach();
        reset(mockView);

        presenter.featuredCallback.onSuccess("{\"data\":[{\"package_name\":\"p\"}]}");

        verifyNoMoreInteractions(mockView);
    }
}