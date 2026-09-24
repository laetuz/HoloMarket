package id.neotica.holomarket.feature.detail.presenter;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import id.neotica.holomarket.feature.detail.contract.RatingsView;
import id.neotica.holomarket.network.ApiCallback;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RatingsPresenterTest {

    @Mock Context mockContext;
    @Mock SharedPreferences mockPrefs;
    @Mock SharedPreferences.Editor mockEditor;
    @Mock
    RatingsView mockView;

    private TestPresenter presenter;

    /**
     * Overrides the I/O seams so the presenter logic runs synchronously without ApiTask.
     */
    private static class TestPresenter extends RatingsPresenter {
        ApiCallback reviewsCallback;
        ApiCallback submitCallback;
        ApiCallback deleteCallback;
        int reviewsCalls;
        int submitCalls;
        int deleteCalls;
        int lastSubmitRating = -1;
        String lastSubmitPkg;
        String lastDeletePkg;

        TestPresenter(Context context) {
            super(context);
        }

        @Override
        void requestReviews(String pkg, ApiCallback callback) {
            reviewsCalls++;
            reviewsCallback = callback;
        }

        @Override
        void requestSubmit(String pkg, int rating, ApiCallback callback) {
            submitCalls++;
            lastSubmitPkg = pkg;
            lastSubmitRating = rating;
            submitCallback = callback;
        }

        @Override
        void requestDelete(String pkg, ApiCallback callback) {
            deleteCalls++;
            lastDeletePkg = pkg;
            deleteCallback = callback;
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

    private void loggedInAs(String username) {
        when(mockPrefs.getString("jwt_token", null)).thenReturn("jwt.token");
        when(mockPrefs.getString("username", null)).thenReturn(username);
    }

    private void loggedOut() {
        when(mockPrefs.getString("jwt_token", null)).thenReturn(null);
    }

    // --- load ---

    @Test
    public void load_loggedOut_rendersWithoutFetching() {
        loggedOut();

        presenter.load("pkg", 4.5, 10);

        verify(mockView).renderRating(false, 0, 4.5, 10);
        assertEquals(0, presenter.reviewsCalls);
    }

    @Test
    public void load_loggedIn_withOwnReview_prefillsRating() {
        loggedInAs("john");

        presenter.load("pkg", 4.0, 3);
        assertEquals(1, presenter.reviewsCalls);

        presenter.reviewsCallback.onSuccess(
                "{\"average_rating\":4.2,\"total_reviews\":4,\"reviews\":["
                        + "{\"username\":\"jane\",\"rating\":2},"
                        + "{\"username\":\"john\",\"rating\":5}]}");

        verify(mockView).renderRating(true, 5, 4.2, 4);
    }

    @Test
    public void load_loggedIn_withoutOwnReview_myRatingZero() {
        loggedInAs("john");

        presenter.load("pkg", 4.0, 3);
        reset(mockView);
        presenter.reviewsCallback.onSuccess(
                "{\"average_rating\":4.0,\"total_reviews\":3,\"reviews\":[{\"username\":\"jane\",\"rating\":2}]}");

        verify(mockView).renderRating(true, 0, 4.0, 3);
    }

    // --- submit ---

    @Test
    public void submitRating_success_savesAndRefreshes() {
        loggedInAs("john");
        presenter.load("pkg", 0, 0);
        presenter.reviewsCallback.onSuccess("{\"reviews\":[]}");

        presenter.submitRating(4);

        assertEquals(1, presenter.submitCalls);
        assertEquals("pkg", presenter.lastSubmitPkg);
        assertEquals(4, presenter.lastSubmitRating);

        presenter.submitCallback.onSuccess("{}");
        verify(mockView).showRatingSaved();

        presenter.reviewsCallback.onSuccess(
                "{\"average_rating\":4.0,\"total_reviews\":1,\"reviews\":[{\"username\":\"john\",\"rating\":4}]}");
        verify(mockView).renderRating(true, 4, 4.0, 1);
    }

    @Test
    public void submitRating_ignoresInvalidRating() {
        loggedInAs("john");
        presenter.load("pkg", 0, 0);

        presenter.submitRating(0);

        assertEquals(0, presenter.submitCalls);
    }

    @Test
    public void submitRating_ignoresWhileInFlight() {
        loggedInAs("john");
        presenter.load("pkg", 0, 0);

        presenter.submitRating(3);
        presenter.submitRating(5);

        assertEquals(1, presenter.submitCalls);
        assertEquals(3, presenter.lastSubmitRating);
    }

    @Test
    public void submitRating_unauthorized_promptsLogin() {
        loggedInAs("john");
        presenter.load("pkg", 0, 0);

        presenter.submitRating(3);
        presenter.submitCallback.onError("HTTP_ERROR_401|unauthorized");

        verify(mockView).onUnauthorized();
    }

    @Test
    public void submitRating_error_showsError() {
        loggedInAs("john");
        presenter.load("pkg", 0, 0);

        presenter.submitRating(3);
        presenter.submitCallback.onError("HTTP_ERROR_500|boom");

        verify(mockView).showRatingError("Failed to save rating.");
    }

    // --- delete ---

    @Test
    public void deleteReview_success_clearsAndRefreshes() {
        loggedInAs("john");
        presenter.load("pkg", 0, 0);
        presenter.reviewsCallback.onSuccess("{\"reviews\":[{\"username\":\"john\",\"rating\":5}]}");
        verify(mockView).renderRating(true, 5, 0, 0);

        presenter.deleteReview();

        assertEquals(1, presenter.deleteCalls);
        assertEquals("pkg", presenter.lastDeletePkg);

        presenter.deleteCallback.onSuccess("{}");
        verify(mockView).showReviewDeleted();

        reset(mockView);
        presenter.reviewsCallback.onSuccess("{\"reviews\":[]}");
        verify(mockView).renderRating(true, 0, 0, 0);
    }

    @Test
    public void deleteReview_unauthorized_promptsLogin() {
        loggedInAs("john");
        presenter.load("pkg", 0, 0);

        presenter.deleteReview();
        presenter.deleteCallback.onError("HTTP_ERROR_401|x");

        verify(mockView).onUnauthorized();
    }

    // --- lifecycle ---

    @Test
    public void detach_thenCallback_doesNotTouchView() {
        loggedInAs("john");
        presenter.load("pkg", 0, 0);

        presenter.detach();
        reset(mockView);

        presenter.reviewsCallback.onSuccess(
                "{\"reviews\":[{\"username\":\"john\",\"rating\":5}]}");

        verifyNoMoreInteractions(mockView);
    }
}