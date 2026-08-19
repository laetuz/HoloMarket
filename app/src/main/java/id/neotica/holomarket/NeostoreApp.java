package id.neotica.holomarket;

import android.app.Application;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import id.neotica.holomarket.utils.CrashCatcher;

/**
 * Created by ryomartin on 08/04/26.
 */

public class NeostoreApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        CrashCatcher.init(this);

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .showImageOnLoading(android.R.drawable.ic_menu_gallery)
                .showImageForEmptyUri(android.R.drawable.sym_def_app_icon)
                .showImageOnFail(android.R.drawable.ic_delete)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .threadPoolSize(3)
                .build();

        ImageLoader.getInstance().init(config);
    }
}