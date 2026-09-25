package id.neotica.holomarket.feature.auth.forgotpassword.presenter;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import id.neotica.holomarket.feature.auth.forgotpassword.contract.ForgotPasswordView;
import id.neotica.holomarket.network.ApiCallback;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class ForgotPasswordPresenterTest {

    @Mock Context mockContext;
    @Mock ForgotPasswordView mockView;

    private TestPresenter presenter;

    private static class TestPresenter extends ForgotPasswordPresenter {
        ApiCallback forgotCallback;
        int forgotCalls;
        String lastEmail;

        TestPresenter(Context context) {
            super(context);
        }

        @Override
        void requestForgot(String email, ApiCallback callback) {
            forgotCalls++;
            lastEmail = email;
            forgotCallback = callback;
        }
    }

    @Before
    public void setUp() {
        presenter = new TestPresenter(mockContext);
        presenter.attach(mockView);
    }

    @Test
    public void sendResetLink_emptyEmail_showsEmailError() {
        presenter.sendResetLink("");

        verify(mockView).showEmailError();
        assertEquals(0, presenter.forgotCalls);
    }

    @Test
    public void sendResetLink_success_showsMessageAndFinishes() {
        presenter.sendResetLink("a@b.com");
        assertEquals(1, presenter.forgotCalls);
        assertEquals("a@b.com", presenter.lastEmail);

        presenter.forgotCallback.onSuccess("{\"message\":\"Check your inbox\"}");

        verify(mockView).showMessage("Check your inbox");
        verify(mockView).finishScreen();
    }

    @Test
    public void sendResetLink_success_missingMessage_usesDefault() {
        presenter.sendResetLink("a@b.com");
        presenter.forgotCallback.onSuccess("{}");

        verify(mockView).showMessage("If the email exists, a reset link has been sent.");
    }

    @Test
    public void sendResetLink_error_showsMessage() {
        presenter.sendResetLink("a@b.com");
        presenter.forgotCallback.onError("timeout");

        verify(mockView).showMessage("Request failed: timeout");
    }

    @Test
    public void detach_thenCallback_doesNotTouchView() {
        presenter.sendResetLink("a@b.com");
        presenter.detach();
        reset(mockView);

        presenter.forgotCallback.onSuccess("{}");

        verifyNoMoreInteractions(mockView);
    }
}