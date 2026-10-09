package id.neotica.holomarket.feature.downloads.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import id.neotica.holomarket.R;
import id.neotica.holomarket.feature.detail.ui.AppDetailActivity;
import id.neotica.holomarket.feature.downloads.contract.DownloadsView;
import id.neotica.holomarket.feature.downloads.domain.DownloadInfo;
import id.neotica.holomarket.feature.downloads.domain.InstalledApp;
import id.neotica.holomarket.feature.downloads.presenter.DownloadsPresenter;
import id.neotica.holomarket.feature.downloads.service.DownloadService;
import id.neotica.holomarket.utils.CrashCatcher;
import id.neotica.holomarket.utils.ImageUrlHelper;
import id.neotica.holomarket.utils.TopBarHelper;

public class DownloadsActivity extends Activity implements DownloadsView {

    private static final String INTENT_PACKAGE_NAME = "PACKAGE_NAME";

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_TASK = 1;
    private static final int TYPE_INSTALLED = 2;

    private ListView listView;
    private TextView tvEmpty;
    private DownloadAdapter adapter;
    private List<DownloadInfo> tasks = new ArrayList<DownloadInfo>();
    private List<InstalledApp> updates = new ArrayList<InstalledApp>();
    private List<InstalledApp> installedApps = new ArrayList<InstalledApp>();
    private List<Row> rows = new ArrayList<Row>();
    private DownloadsPresenter presenter;

    private final BroadcastReceiver progressReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            presenter.refresh();
            if (intent.getBooleanExtra(DownloadService.EXTRA_DONE, false)
                    || intent.getBooleanExtra(DownloadService.EXTRA_ERROR, false)
                    || intent.getBooleanExtra(DownloadService.EXTRA_CANCELLED, false)) {
                presenter.loadInstalledApps();
            }
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        openRequestedDetail(intent);
    }

    private void openRequestedDetail(Intent intent) {
        if (intent == null) {
            return;
        }
        openDetail(intent.getStringExtra(DownloadService.EXTRA_OPEN_PACKAGE));
    }

    private void openDetail(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return;
        }
        Intent intent = new Intent(this, AppDetailActivity.class);
        intent.putExtra(INTENT_PACKAGE_NAME, packageName);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashCatcher.init(this.getApplicationContext());
        setContentView(R.layout.activity_downloads);
        CrashCatcher.showCrashLogIfAny(this);

        presenter = new DownloadsPresenter(this);
        presenter.attach(this);

        TopBarHelper.setup(this, getString(R.string.downloads_title), true);

        listView = (ListView) findViewById(R.id.lv_downloads);
        tvEmpty = (TextView) findViewById(R.id.tv_downloads_empty);

        listView.setItemsCanFocus(true);
        adapter = new DownloadAdapter();
        listView.setAdapter(adapter);

        presenter.refresh();
        presenter.loadInstalledApps();

        if (savedInstanceState == null) {
            openRequestedDetail(getIntent());
        }
    }

    private View.OnClickListener rowClickListener(final String packageName) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDetail(packageName);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(progressReceiver, new IntentFilter(DownloadService.ACTION_PROGRESS));
        presenter.refresh();
        presenter.loadInstalledAppsIfStale();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(progressReceiver);
        } catch (Exception e) {
        }
    }

    @Override
    public void renderTasks(List<DownloadInfo> list) {
        tasks.clear();
        if (list != null) {
            tasks.addAll(list);
        }
        rebuildRows();
    }

    @Override
    public void renderInstalledApps(List<InstalledApp> updateList, List<InstalledApp> installedList) {
        updates.clear();
        if (updateList != null) {
            updates.addAll(updateList);
        }
        installedApps.clear();
        if (installedList != null) {
            installedApps.addAll(installedList);
        }
        rebuildRows();
    }

    private void rebuildRows() {
        rows.clear();

        if (!tasks.isEmpty()) {
            rows.add(headerRow(getString(R.string.downloads_section_active)));
            for (int i = 0; i < tasks.size(); i++) {
                Row row = new Row();
                row.type = TYPE_TASK;
                row.task = tasks.get(i);
                rows.add(row);
            }
        }

        if (!updates.isEmpty()) {
            rows.add(headerRow(getString(R.string.downloads_section_updates)));
            for (int i = 0; i < updates.size(); i++) {
                Row row = new Row();
                row.type = TYPE_INSTALLED;
                row.app = updates.get(i);
                rows.add(row);
            }
        }

        if (!installedApps.isEmpty()) {
            rows.add(headerRow(getString(R.string.downloads_section_installed)));
            for (int i = 0; i < installedApps.size(); i++) {
                Row row = new Row();
                row.type = TYPE_INSTALLED;
                row.app = installedApps.get(i);
                rows.add(row);
            }
        }

        adapter.notifyDataSetChanged();

        boolean empty = rows.isEmpty();
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        listView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private Row headerRow(String title) {
        Row row = new Row();
        row.type = TYPE_HEADER;
        row.title = title;
        return row;
    }

    @Override
    protected void onDestroy() {
        if (presenter != null) {
            presenter.detach();
        }
        super.onDestroy();
    }

    private static class Row {
        int type;
        String title;
        DownloadInfo task;
        InstalledApp app;
    }

    private static class TaskHolder {
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvStatus;
        ProgressBar progress;
        Button btnCancel;
    }

    private static class InstalledHolder {
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvStatus;
    }

    private class DownloadAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return rows.size();
        }

        @Override
        public Object getItem(int position) {
            return rows.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }

        @Override
        public int getItemViewType(int position) {
            return rows.get(position).type;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Row row = rows.get(position);
            LayoutInflater inflater = LayoutInflater.from(DownloadsActivity.this);

            if (row.type == TYPE_HEADER) {
                View view = convertView;
                if (view == null) {
                    view = inflater.inflate(R.layout.item_download_section, parent, false);
                }
                view.setOnClickListener(null);
                ((TextView) view.findViewById(R.id.tv_section_title)).setText(row.title);
                return view;
            }

            if (row.type == TYPE_TASK) {
                TaskHolder holder;
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.item_download, parent, false);
                    holder = new TaskHolder();
                    holder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_download_icon);
                    holder.tvTitle = (TextView) convertView.findViewById(R.id.tv_download_title);
                    holder.tvStatus = (TextView) convertView.findViewById(R.id.tv_download_status);
                    holder.progress = (ProgressBar) convertView.findViewById(R.id.pb_download);
                    holder.btnCancel = (Button) convertView.findViewById(R.id.btn_download_cancel);
                    convertView.setTag(holder);
                } else {
                    holder = (TaskHolder) convertView.getTag();
                }
                bindTask(holder, row.task);
                convertView.setOnClickListener(rowClickListener(
                        row.task == null ? null : row.task.packageName));
                return convertView;
            }

            InstalledHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_installed_app, parent, false);
                holder = new InstalledHolder();
                holder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_installed_icon);
                holder.tvTitle = (TextView) convertView.findViewById(R.id.tv_installed_title);
                holder.tvStatus = (TextView) convertView.findViewById(R.id.tv_installed_status);
                convertView.setTag(holder);
            } else {
                holder = (InstalledHolder) convertView.getTag();
            }
            bindInstalled(holder, row.app);
            convertView.setOnClickListener(rowClickListener(
                    row.app == null ? null : row.app.packageName));
            return convertView;
        }

        private void bindTask(TaskHolder holder, final DownloadInfo info) {
            if (info == null) {
                return;
            }

            holder.tvTitle.setText(!TextUtils.isEmpty(info.appName) ? info.appName : info.packageName);
            holder.tvStatus.setText(info.statusText);

            holder.progress.setIndeterminate(info.installing);
            if (!info.installing) {
                holder.progress.setProgress(Math.max(0, Math.min(100, info.percent)));
            }

            if (!TextUtils.isEmpty(info.icon)) {
                ImageLoader.getInstance().displayImage(ImageUrlHelper.build(info.icon), holder.ivIcon);
            } else {
                ImageLoader.getInstance().cancelDisplayTask(holder.ivIcon);
                holder.ivIcon.setImageResource(android.R.drawable.sym_def_app_icon);
            }

            holder.btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    presenter.cancel(info.packageName);
                }
            });
        }

        private void bindInstalled(InstalledHolder holder, InstalledApp app) {
            if (app == null) {
                return;
            }

            holder.tvTitle.setText(!TextUtils.isEmpty(app.title) ? app.title : app.packageName);
            holder.tvStatus.setText(app.hasUpdate()
                    ? getString(R.string.downloads_status_update_available)
                    : getString(R.string.downloads_status_installed));

            if (!TextUtils.isEmpty(app.iconUrl)) {
                ImageLoader.getInstance().displayImage(ImageUrlHelper.build(app.iconUrl), holder.ivIcon);
            } else {
                ImageLoader.getInstance().cancelDisplayTask(holder.ivIcon);
                holder.ivIcon.setImageResource(android.R.drawable.sym_def_app_icon);
            }
        }
    }
}