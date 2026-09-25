package id.neotica.holomarket.feature.auth.register.presenter;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import id.neotica.holomarket.feature.auth.register.contract.RegisterView;
import id.neotica.holomarket.network.ApiCallback;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RegisterPresenterTest {

    @Mock Context mockContext;
    @Mock SharedPreferences mockPrefs;
    @Mock RegisterView mockView;

    private TestPresenter presenter;

    private static class TestPresenter extends RegisterPresenter {
        ApiCallback registerCallback;
        int registerCalls;
        String lastUsername;
        String lastEmail;
        String lastPassword;

        TestPresenter(Context context) {
            super(context);
        }

        @Override
        void requestRegister(String username, String password, String email, ApiCallback callback) {
            registerCalls++;
            lastUsername = username;
            lastPassword = password;
            lastEmail = email;
            registerCallback = callback;
        }
    }

    @Before
    public void setUp() {
        // AnalyticsTracker.track() builds an AuthManager(context) — provide a (logged-out) prefs mock.
        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs);
        when(mockPrefs.getString("jwt_token", null)).thenReturn(null);

        presenter = new TestPresenter(mockContext);
        presenter.attach(mockView);
    }

    @Test
    public void register_emptyUsername_showsUsernameError() {
        presenter.register("", "e@x.com", "pw");

        verify(mockView).showUsernameError();
        assertEquals(0, presenter.registerCalls);
    }

    @Test
    public void register_emptyEmail_showsEmailError() {
        presenter.register("user", "", "pw");

        verify(mockView).showEmailError();
        assertEquals(0, presenter.registerCalls);
    }

    @Test
    public void register_emptyPassword_showsPasswordError() {
        presenter.register("user", "e@x.com", "");

        verify(mockView).showPasswordError();
        assertEquals(0, presenter.registerCalls);
    }

    @Test
    public void register_success_showsMessageAndFinishes() {
        presenter.register("user", "e@x.com", "pw");
        assertEquals(1, presenter.registerCalls);
        assertEquals("user", presenter.lastUsername);
        assertEquals("e@x.com", presenter.lastEmail);

        presenter.registerCallback.onSuccess("{\"message\":\"Welcome\"}");

        verify(mockView).showMessage("Welcome");
        verify(mockView).finishScreen();
    }

    @Test
    public void register_success_missingMessage_usesDefault() {
        presenter.register("user", "e@x.com", "pw");
        presenter.registerCallback.onSuccess("{}");

        verify(mockView).showMessage("Account created.");
    }

    @Test
    public void register_error_showsMessage() {
        presenter.register("user", "e@x.com", "pw");
        presenter.registerCallback.onError("Conflict");

        verify(mockView).showMessage("Registration failed: Conflict");
    }

    @Test
    public void detach_thenCallback_doesNotTouchView() {
        presenter.register("user", "e@x.com", "pw");
        presenter.detach();
        reset(mockView);

        presenter.registerCallback.onSuccess("{}");

        verifyNoMoreInteractions(mockView);
    }
}