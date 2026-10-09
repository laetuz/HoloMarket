package id.neotica.holomarket.feature.downloads.contract;

import java.util.List;

import id.neotica.holomarket.feature.downloads.domain.DownloadInfo;
import id.neotica.holomarket.feature.downloads.domain.InstalledApp;

/**
 * View contract for the downloads manager, implemented by the downloads Activity.
 * {@code DownloadsPresenter} drives all rendering through this interface.
 */
public interface DownloadsView {

    void renderTasks(List<DownloadInfo> tasks);

    void renderInstalledApps(List<InstalledApp> updates, List<InstalledApp> installed);
}