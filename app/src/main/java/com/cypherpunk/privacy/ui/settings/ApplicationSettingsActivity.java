package com.cypherpunk.privacy.ui.settings;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.databinding.ActivityApplicationSettingsBinding;
import com.cypherpunk.privacy.model.AppData;
import com.cypherpunk.privacy.model.CypherpunkSetting;
import com.cypherpunk.privacy.utils.FontUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

public class ApplicationSettingsActivity extends AppCompatActivity {

    private List<AppData> items = new ArrayList<>();
    private ActivityApplicationSettingsBinding binding;
    private Subscription subscription = Subscriptions.empty();
    private ApplicationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_application_settings);

        if (!getResources().getBoolean(R.bool.is_tablet)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        // TODO: customize search view
        setSupportActionBar(binding.toolbar.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            binding.toolbar.title.setText(R.string.title_activity_application_settings);
            actionBar.setHomeAsUpIndicator(R.drawable.close_vector);
        }

        // TODO: scroll bar
        binding.list.setHasFixedSize(true);
        binding.list.addItemDecoration(new DividerDecoration(this));
        adapter = new ApplicationAdapter();
        binding.list.setAdapter(adapter);

        getApplicationList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.application_settings, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setQueryHint(getString(R.string.search_application_title));
        TextView textView = (TextView) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        textView.setTypeface(FontUtil.getDosisMedium(this));
        textView.setHintTextColor(ContextCompat.getColor(this, R.color.search_view_hint_text));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)) {
                    adapter.addAll(items);
                } else {
                    String query = newText.toLowerCase();
                    ArrayList<AppData> filteredList = new ArrayList<>();
                    for (AppData item : items) {
                        String name = item.name.toLowerCase();
                        if (name.contains(query)) {
                            filteredList.add(item);
                        }
                    }
                    adapter.addAll(filteredList);
                }
                return false;
            }
        });
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.toolbar.title.setText("");
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                binding.toolbar.title.setText(R.string.title_activity_application_settings);
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
        subscription.unsubscribe();
        ArrayList<String> offList = new ArrayList<>();
        for (AppData item : items) {
            if (!item.check) {
                offList.add(item.packageName);
            }
        }
        CypherpunkSetting cypherpunkSetting = new CypherpunkSetting();
        cypherpunkSetting.disableAppPackageName = TextUtils.join(",", offList);
        cypherpunkSetting.save();
        super.onDestroy();
    }

    private void getApplicationList() {
        CypherpunkSetting cypherpunkSetting = new CypherpunkSetting();
        final List<String> offList = Arrays.asList(cypherpunkSetting.disableAppPackageName.split(","));
        subscription = Observable
                .create(new Observable.OnSubscribe<AppData>() {
                    @Override
                    public void call(Subscriber<? super AppData> subscriber) {
                        int androidSystemId = 0;
                        ApplicationInfo system = null;
                        final PackageManager pm = getPackageManager();

                        try {
                            system = pm.getApplicationInfo("android", PackageManager.GET_META_DATA);
                            androidSystemId = system.uid;
                        } catch (PackageManager.NameNotFoundException e) {
                        }

                        List<ApplicationInfo> installedAppList = pm.getInstalledApplications(PackageManager.GET_META_DATA);
                        for (ApplicationInfo app : installedAppList) {
                            if (pm.checkPermission(Manifest.permission.INTERNET, app.packageName) == PackageManager.PERMISSION_GRANTED &&
                                    app.uid != androidSystemId)
                                subscriber.onNext(new AppData(app.loadLabel(pm).toString(), app.loadIcon(pm), app.packageName));
                        }
                        subscriber.onCompleted();
                    }
                })
                .map(new Func1<AppData, AppData>() {
                    @Override
                    public AppData call(AppData appData) {
                        if (offList.contains(appData.packageName)) {
                            appData.check = false;
                        }
                        return appData;
                    }
                })
                .toSortedList(new Func2<AppData, AppData, Integer>() {
                    @Override
                    public Integer call(AppData appData, AppData appData2) {
                        return appData.name.compareTo(appData2.name);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<AppData>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(List<AppData> list) {
                        adapter.addAll(list);
                        items = list;
                        binding.progress.setVisibility(View.GONE);
                        binding.list.setVisibility(View.VISIBLE);
                    }
                });
    }
}
