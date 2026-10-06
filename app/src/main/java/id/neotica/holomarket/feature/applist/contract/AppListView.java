package id.neotica.holomarket.feature.applist.contract;

import java.util.List;

import id.neotica.holomarket.model.AppModel;

/**
 * View contract for the app-list screen, implemented by the app-list Activity.
 * {@code AppListPresenter} drives all rendering through this interface.
 */
public interface AppListView {

    void renderApps(List<AppModel> apps, boolean append);

    void showLoadMore(boolean hasMore);

    void showError(String message);
}