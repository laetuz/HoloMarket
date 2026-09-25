package id.neotica.holomarket.feature.auth.login.presenter;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import id.neotica.holomarket.feature.auth.login.contract.LoginView;
import id.neotica.holomarket.network.ApiCallback;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LoginPresenterTest {

    @Mock Context mockContext;
    @Mock SharedPreferences mockPrefs;
    @Mock SharedPreferences.Editor mockEditor;
    @Mock LoginView mockView;

    private TestPresenter presenter;

    private static class TestPresenter extends LoginPresenter {
        ApiCallback loginCallback;
        ApiCallback usernameCallback;
        int loginCalls;
        int usernameCalls;
        String lastUsername;
        String lastPassword;

        TestPresenter(Context context) {
            super(context);
        }

        @Override
        void requestLogin(String username, String password, ApiCallback callback) {
            loginCalls++;
            lastUsername = username;
            lastPassword = password;
            loginCallback = callback;
        }

        @Override
        void requestUsername(ApiCallback callback) {
            usernameCalls++;
            usernameCallback = callback;
        }
    }

    @Before
    public void setUp() {
        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs);
        when(mockPrefs.edit()).thenReturn(mockEditor);
        when(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor);
        when(mockEditor.putLong(anyString(), anyLong())).thenReturn(mockEditor);
        when(mockEditor.putBoolean(anyString(), anyBoolean())).thenReturn(mockEditor);
        when(mockEditor.remove(anyString())).thenReturn(mockEditor);
        when(mockEditor.commit()).thenReturn(true);
        when(mockPrefs.getString("jwt_token", null)).thenReturn(null);

        presenter = new TestPresenter(mockContext);
        presenter.attach(mockView);
    }

    @Test
    public void login_emptyUsername_showsUsernameError() {
        presenter.login("", "pw");

        verify(mockView).showUsernameError();
        assertEquals(0, presenter.loginCalls);
    }

    @Test
    public void login_emptyPassword_showsPasswordError() {
        presenter.login("user", "");

        verify(mockView).showPasswordError();
        assertEquals(0, presenter.loginCalls);
    }

    @Test
    public void login_success_savesTokenAndNavigates() {
        presenter.login("user", "pw");
        assertEquals(1, presenter.loginCalls);
        assertEquals("user", presenter.lastUsername);

        presenter.loginCallback.onSuccess(
                "{\"token\":\"jwt\",\"refreshToken\":\"ref\",\"expirationTime\":123}");

        verify(mockEditor).putString("jwt_token", "jwt");
        verify(mockEditor).putString("refresh_token", "ref");
        verify(mockEditor).putLong("expiration_time", 123L);

        assertEquals(1, presenter.usernameCalls);
        presenter.usernameCallback.onSuccess("john");
        verify(mockEditor).putString("username", "john");
        verify(mockView).navigateToMain();
    }

    @Test
    public void login_noToken_showsMessage() {
        presenter.login("user", "pw");
        presenter.loginCallback.onSuccess("{}");

        verify(mockView).showMessage("Login failed: no token in response");
    }

    @Test
    public void login_requestError_showsMessage() {
        presenter.login("user", "pw");
        presenter.loginCallback.onError("Invalid credentials");

        verify(mockView).showMessage("Login failed: Invalid credentials");
    }

    @Test
    public void login_usernameFetchError_stillNavigates() {
        presenter.login("user", "pw");
        presenter.loginCallback.onSuccess("{\"token\":\"jwt\"}");
        presenter.usernameCallback.onError("timeout");

        verify(mockView).navigateToMain();
    }

    @Test
    public void detach_thenCallback_doesNotTouchView() {
        presenter.login("user", "pw");
        presenter.detach();
        reset(mockView);

        presenter.loginCallback.onSuccess("{\"token\":\"jwt\"}");

        verifyNoMoreInteractions(mockView);
    }
}