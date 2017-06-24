package com.cypherpunk.privacy.ui.settings;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.domain.model.VpnSetting;
import com.cypherpunk.privacy.ui.common.FontCache;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

import static android.Manifest.permission.INTERNET;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class SplitTunnelActivity extends AppCompatActivity {

    @NonNull
    private Disposable disposable = Disposables.empty();

    private AppAdapter adapter;

    @BindView(R.id.title)
    TextView titleView;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @Inject
    VpnSetting vpnSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_split_tunnel);
        ButterKnife.bind(this);

        CypherpunkApplication.instance.getAppComponent().inject(this);

        if (!getResources().getBoolean(R.bool.is_tablet)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        // TODO: customize search view
        final Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        final View progress = ButterKnife.findById(this, R.id.progress);

        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_vector);
        }

        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerDecoration(this));

        final List<String> exceptAppList = vpnSetting.exceptAppList();

        progress.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        final PackageManager pm = getPackageManager();

        disposable = getTargetAppList(this)
                .map(new Function<ApplicationInfo, AppInfo>() {
                    @Override
                    public AppInfo apply(ApplicationInfo info) throws Exception {
                        final String name = info.loadLabel(pm).toString();
                        final Drawable icon = info.loadIcon(pm);
                        final AppInfo appData = new AppInfo(name, icon, info.packageName);
                        if (exceptAppList.contains(info.packageName)) {
                            appData.setChecked(false);
                        }
                        return appData;
                    }
                })
                .toSortedList(new Comparator<AppInfo>() {
                    @Override
                    public int compare(AppInfo appInfo, AppInfo appInfo2) {
                        return appInfo.name.compareTo(appInfo2.name);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<AppInfo>>() {
                    @Override
                    public void accept(List<AppInfo> list) throws Exception {
                        adapter = new AppAdapter(list);
                        recyclerView.setAdapter(adapter);

                        progress.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setQueryHint(getString(R.string.split_tunnel_search_hint));

        final TextView textView = (TextView) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        textView.setTypeface(FontCache.getDosisMedium(this));
        textView.setHintTextColor(ContextCompat.getColor(this, R.color.search_view_hint_text));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)) {
                    adapter.clearTextFilter();
                } else {
                    adapter.setFilterText(newText);
                }
                return false;
            }
        });
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                titleView.setVisibility(View.GONE);
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                titleView.setVisibility(View.VISIBLE);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        disposable.dispose();
        vpnSetting.updateExceptAppList(adapter.getCheckedList());
        super.onDestroy();
    }

    private static Flowable<ApplicationInfo> getTargetAppList(@NonNull Context context) {
        final PackageManager pm = context.getPackageManager();

        return Single
                .create(new SingleOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(SingleEmitter<Integer> emitter) throws Exception {
                        try {
                            final ApplicationInfo system = pm.getApplicationInfo("android", PackageManager.GET_META_DATA);
                            emitter.onSuccess(system.uid);
                        } catch (PackageManager.NameNotFoundException e) {
                            emitter.onError(e);
                        }
                    }
                })
                .toFlowable()
                .flatMap(new Function<Integer, Flowable<ApplicationInfo>>() {
                    @Override
                    public Flowable<ApplicationInfo> apply(final Integer androidSystemId) throws Exception {
                        return Flowable
                                .fromIterable(pm.getInstalledApplications(PackageManager.GET_META_DATA))
                                .filter(new Predicate<ApplicationInfo>() {
                                    @Override
                                    public boolean test(ApplicationInfo info) throws Exception {
                                        return info.uid != androidSystemId;
                                    }
                                })
                                .filter(new Predicate<ApplicationInfo>() {
                                    @Override
                                    public boolean test(ApplicationInfo info) throws Exception {
                                        return pm.checkPermission(INTERNET, info.packageName) == PERMISSION_GRANTED;
                                    }
                                });
                    }
                });
    }

    private static class AppInfo implements Checkable {

        @NonNull
        final String name;
        final Drawable icon;
        final String packageName;

        private boolean checked = true;

        AppInfo(@NonNull String name, Drawable icon, String packageName) {
            this.name = name;
            this.icon = icon;
            this.packageName = packageName;
        }

        @Override
        public void setChecked(boolean checked) {
            this.checked = checked;
        }

        @Override
        public boolean isChecked() {
            return checked;
        }

        @Override
        public void toggle() {
            checked = !checked;
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {

        private static final int LAYOUT_ID = R.layout.list_item_split_tunnel;

        @NonNull
        public static ItemViewHolder create(@NonNull LayoutInflater inflater, ViewGroup parent) {
            return new ItemViewHolder(inflater.inflate(LAYOUT_ID, parent, false));
        }

        @BindView(R.id.icon)
        ImageView iconView;

        @BindView(R.id.name)
        TextView nameView;

        @BindView(R.id.switch_compat)
        SwitchCompat switchView;

        ItemViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switchView.toggle();
                }
            });
        }
    }

    private static class AppAdapter extends RecyclerView.Adapter<ItemViewHolder> {

        private List<AppInfo> originals;
        private List<AppInfo> items;

        AppAdapter(@NonNull List<AppInfo> data) {
            originals = new ArrayList<>(data);
            items = new ArrayList<>(data);
        }

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            final ItemViewHolder holder = ItemViewHolder.create(inflater, parent);
            holder.switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    final int position = holder.getAdapterPosition();
                    final AppInfo info = items.get(position);
                    info.setChecked(isChecked);
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(ItemViewHolder holder, int position) {
            final AppInfo appData = items.get(position);
            holder.iconView.setImageDrawable(appData.icon);
            holder.nameView.setText(appData.name);
            holder.switchView.setChecked(appData.isChecked());
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @NonNull
        private List<String> getCheckedList() {
            final ArrayList<String> exceptAppList = new ArrayList<>();
            for (int i = 0, count = items.size(); i < count; i++) {
                final AppInfo info = items.get(i);
                if (!info.isChecked()) {
                    exceptAppList.add(info.packageName);
                }
            }
            return exceptAppList;
        }

        void clearTextFilter() {
            items.clear();
            items.addAll(originals);
            notifyDataSetChanged();
        }

        void setFilterText(@NonNull String query) {
            items.clear();
            for (AppInfo info : originals) {
                if (info.name.toLowerCase().contains(query.toLowerCase())) {
                    items.add(info);
                }
            }
            notifyDataSetChanged();
        }
    }
}
