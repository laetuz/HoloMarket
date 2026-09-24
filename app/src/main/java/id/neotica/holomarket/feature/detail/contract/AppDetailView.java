package id.neotica.holomarket.feature.detail.contract;

import id.neotica.holomarket.feature.detail.domain.AppDetailModel;
import id.neotica.holomarket.feature.detail.domain.InstallState;

/**
 * View contract for the app-detail screen, implemented by the detail Activity.
 * {@code AppDetailPresenter} drives all rendering through this interface.
 */
public interface AppDetailView {

    void showLoading();

    void renderAppDetail(AppDetailModel detail);

    void renderInstallState(InstallState state);

    void showLoadError(String message);

    void openApp(String packageName);

    void showNoVersions();

    void showNoDownloadLink();
}