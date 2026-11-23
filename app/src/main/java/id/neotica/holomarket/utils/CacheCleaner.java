package id.neotica.holomarket.utils;

import android.content.Context;
import android.os.Build;
import android.text.format.Formatter;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;

/**
 * Clears the app cache, including the UIL image/icon memory and disk caches.
 * Never touches SharedPreferences, downloaded APKs, or crash logs.
 */
public final class CacheCleaner {

    private CacheCleaner() { }

    public static void clear(Context context) {
        try {
            ImageLoader.getInstance().clearMemoryCache();
            ImageLoader.getInstance().clearDiskCache();
        } catch (Throwable t) {
            // UIL not initialized — nothing else to clear there.
        }

        deleteChildren(context.getCacheDir());

        if (Build.VERSION.SDK_INT >= 8) {
            try {
                deleteChildren(context.getExternalCacheDir());
            } catch (Throwable t) {
                // External cache unavailable.
            }
        }
    }

    public static long size(Context context) {
        long total = sizeOf(context.getCacheDir());
        if (Build.VERSION.SDK_INT >= 8) {
            try {
                total += sizeOf(context.getExternalCacheDir());
            } catch (Throwable t) {
                // External cache unavailable.
            }
        }
        return total;
    }

    public static String formatSize(Context context, long bytes) {
        return Formatter.formatShortFileSize(context, bytes);
    }

    private static long sizeOf(File file) {
        if (file == null || !file.exists()) {
            return 0;
        }
        if (file.isFile()) {
            return file.length();
        }
        File[] children = file.listFiles();
        if (children == null) {
            return 0;
        }
        long total = 0;
        for (int i = 0; i < children.length; i++) {
            total += sizeOf(children[i]);
        }
        return total;
    }

    private static void deleteChildren(File dir) {
        if (dir == null || !dir.exists()) {
            return;
        }
        File[] children = dir.listFiles();
        if (children == null) {
            return;
        }
        for (int i = 0; i < children.length; i++) {
            deleteRecursively(children[i]);
        }
    }

    private static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    deleteRecursively(children[i]);
                }
            }
        }
        file.delete();
    }
}