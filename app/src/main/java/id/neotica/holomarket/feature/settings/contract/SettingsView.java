package id.neotica.holomarket.feature.settings.contract;

/**
 * View contract for the settings screen, implemented by the settings Activity.
 * {@code SettingsPresenter} drives all rendering through this interface.
 */
public interface SettingsView {

    void renderProfile(String username);

    void renderAdultContent(boolean enabled);

    void renderRootAutoInstall(boolean enabled);

    void renderVersion(String versionName, int versionCode);

    void showUpToDate();

    void showUpdateAvailable(String versionName);

    void showMessage(String message);

    void navigateToMain();
}