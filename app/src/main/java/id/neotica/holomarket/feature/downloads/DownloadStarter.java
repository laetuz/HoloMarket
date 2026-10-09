package id.neotica.holomarket.feature.downloads;

import android.content.Context;
import android.content.Intent;

import id.neotica.holomarket.feature.downloads.service.DownloadService;

/**
 * Enqueues a download with {@link DownloadService}.
 */
public final class DownloadStarter {

    private DownloadStarter() { }

    public static void start(Context context, String packageName, String fileName, String appTitle,
                             String icon, String url) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(DownloadService.ACTION_START);
        intent.putExtra(DownloadService.EXTRA_PACKAGE, packageName);
        intent.putExtra(DownloadService.EXTRA_URL, url);
        intent.putExtra(DownloadService.EXTRA_FILE_NAME, fileName);
        intent.putExtra(DownloadService.EXTRA_APP_NAME, appTitle);
        intent.putExtra(DownloadService.EXTRA_ICON, icon);
        context.startService(intent);
    }

    public static void cancel(Context context, String packageName) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(DownloadService.ACTION_CANCEL);
        intent.putExtra(DownloadService.EXTRA_PACKAGE, packageName);
        context.startService(intent);
    }
}