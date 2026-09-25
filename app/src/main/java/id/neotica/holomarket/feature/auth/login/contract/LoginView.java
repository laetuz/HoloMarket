package id.neotica.holomarket.feature.auth.login.contract;

/**
 * View contract for the login screen, implemented by the login Activity.
 * {@code LoginPresenter} drives all rendering through this interface.
 */
public interface LoginView {

    void showUsernameError();

    void showPasswordError();

    void showMessage(String message);

    void navigateToMain();
}