package id.neotica.holomarket.utils;

import android.content.Context;
import android.os.Environment;
import android.text.format.Formatter;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;

/**
 * Clears the app cache, including the UIL image/icon memory and disk caches.
 * Never touches SharedPreferences, downloaded APKs, or crash logs.
 *
 * <p>Uses the manual {@code Android/data/<pkg>/cache} path instead of
 * {@code Context.getExternalCacheDir()} (API 8) so the class verifies on Android 1.6.
 */
public final class CacheCleaner {

    private CacheCleaner() { }

    public static void clear(Context context) {
        try {
            ImageLoader.getInstance().clearMemoryCache();
        } catch (Throwable t) {
            // UIL not initialized — nothing else to clear there.
        }

        deleteChildren(context.getCacheDir());
        deleteChildren(externalCacheDir(context));
    }

    public static long size(Context context) {
        return sizeOf(context.getCacheDir()) + sizeOf(externalCacheDir(context));
    }

    public static String formatSize(Context context, long bytes) {
        return Formatter.formatFileSize(context, bytes);
    }

    private static File externalCacheDir(Context context) {
        try {
            if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                return null;
            }
            File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
            return new File(new File(dataDir, context.getPackageName()), "cache");
        } catch (Throwable t) {
            return null;
        }
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