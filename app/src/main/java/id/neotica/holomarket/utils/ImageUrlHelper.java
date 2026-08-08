package id.neotica.holomarket.utils;

import id.neotica.holomarket.BuildConfig;

public class ImageUrlHelper {
    private ImageUrlHelper() { }

    public static String build(String path) {
        if (path == null) {
            return null;
        }
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }
        if (path.startsWith("/buckets")) {
            return BuildConfig.FILE_BASE_URL + path;
        }
        return BuildConfig.FILE_BASE_URL + "/buckets" + path;
    }
}