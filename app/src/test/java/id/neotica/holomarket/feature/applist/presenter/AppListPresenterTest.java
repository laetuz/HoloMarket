package id.neotica.holomarket.feature.applist.presenter;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import id.neotica.holomarket.feature.applist.contract.AppListView;
import id.neotica.holomarket.model.AppModel;
import id.neotica.holomarket.network.ApiCallback;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AppListPresenterTest {

    @Mock Context mockContext;
    @Mock SharedPreferences mockPrefs;
    @Mock AppListView mockView;

    private TestPresenter presenter;

    private static final String FEED_JSON =
            "{\"page\":1,\"total_pages\":2,\"data\":["
                    + "{\"package_name\":\"p1\",\"title\":\"A\"},"
                    + "{\"package_name\":\"p2\",\"title\":\"B\"}]}";

    private static final String LAST_PAGE_JSON =
            "{\"page\":1,\"total_pages\":1,\"data\":[{\"package_name\":\"p1\",\"title\":\"A\"}]}";

    private static class TestPresenter extends AppListPresenter {
        ApiCallback callback;
        String lastUrl;
        int calls;

        TestPresenter(Context context) {
            super(context);
        }

        @Override
        void requestFeed(String url, ApiCallback callback) {
            calls++;
            lastUrl = url;
            this.callback = callback;
        }
    }

    @Before
    public void setUp() {
        // search() calls AnalyticsTracker.track() -> AuthManager(context) -> SharedPreferences.
        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs);
        when(mockPrefs.getString("jwt_token", null)).thenReturn(null);

        presenter = new TestPresenter(mockContext);
        presenter.attach(mockView);
    }

    @Test
    public void load_firstPage_rendersAndShowsLoadMore() {
        presenter.load("", "");

        assertTrue(presenter.lastUrl.contains("/apps/feed?page=1"));

        presenter.callback.onSuccess(FEED_JSON);

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(mockView).renderApps(captor.capture(), eq(false));
        assertEquals(2, captor.getValue().size());
        verify(mockView).showLoadMore(true);
    }

    @Test
    public void load_lastPage_hidesLoadMore() {
        presenter.load("", "");
        presenter.callback.onSuccess(LAST_PAGE_JSON);

        verify(mockView).showLoadMore(false);
    }

    @Test
    public void load_adultCategory_usesAdultFeed() {
        presenter.load("adult", "");

        assertTrue(presenter.lastUrl.contains("/apps/adult-feed"));
        assertFalse(presenter.lastUrl.contains("category="));
    }

    @Test
    public void load_category_addsCategoryParam() {
        presenter.load("game", "");

        assertTrue(presenter.lastUrl.contains("/apps/feed?page=1"));
        assertTrue(presenter.lastUrl.contains("&category=game"));
    }

    @Test
    public void load_searchQuery_addsSearchParam() {
        presenter.load("", "foo bar");

        assertTrue(presenter.lastUrl.contains("&search=foo+bar"));
    }

    @Test
    public void loadMore_incrementsPage_andAppends() {
        presenter.load("", "");
        presenter.callback.onSuccess(FEED_JSON);

        presenter.loadMore();
        assertTrue(presenter.lastUrl.contains("page=2"));

        presenter.callback.onSuccess(FEED_JSON);
        verify(mockView).renderApps(anyList(), eq(true));
    }

    @Test
    public void loadMore_atLastPage_doesNothing() {
        presenter.load("", "");
        presenter.callback.onSuccess(LAST_PAGE_JSON);

        int callsBefore = presenter.calls;
        presenter.loadMore();

        assertEquals(callsBefore, presenter.calls);
    }

    @Test
    public void loadMore_error_showsError() {
        presenter.load("", "");
        presenter.callback.onSuccess(FEED_JSON);

        presenter.loadMore();
        presenter.callback.onError("boom");

        verify(mockView).showError("boom");
    }

    @Test
    public void search_resetsToPageOne_andUsesQuery() {
        presenter.load("", "");
        presenter.callback.onSuccess(FEED_JSON);
        presenter.loadMore();

        presenter.search("hello");

        assertTrue(presenter.lastUrl.contains("page=1"));
        assertTrue(presenter.lastUrl.contains("&search=hello"));
    }

    @Test
    public void load_error_showsError() {
        presenter.load("", "");
        presenter.callback.onError("network down");

        verify(mockView).showError("network down");
    }

    @Test
    public void detach_thenCallback_doesNotTouchView() {
        presenter.load("", "");

        presenter.detach();
        reset(mockView);

        presenter.callback.onSuccess(FEED_JSON);

        verifyNoMoreInteractions(mockView);
    }
}