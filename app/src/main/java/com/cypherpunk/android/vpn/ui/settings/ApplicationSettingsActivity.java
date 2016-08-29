package com.cypherpunk.android.vpn.ui.settings;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.databinding.ActivityApplicationSettingsBinding;
import com.cypherpunk.android.vpn.model.AppData;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
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

        ActionBar actionBar = getSupportActionBar();

        // TODO: customize search view
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // TODO: scroll bar
        binding.list.setHasFixedSize(true);
        binding.list.addItemDecoration(new DividerItemDecoration(this));
        adapter = new ApplicationAdapter();
        binding.list.setAdapter(adapter);

        getApplicationList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.application_settings, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setQueryHint(getString(R.string.search_application_title));
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
        super.onDestroy();
    }

    private void getApplicationList() {
        subscription = Observable
                .create(new Observable.OnSubscribe<AppData>() {
                    @Override
                    public void call(Subscriber<? super AppData> subscriber) {
                        final PackageManager pm = getPackageManager();
                        List<ApplicationInfo> installedAppList = pm.getInstalledApplications(PackageManager.GET_META_DATA);
                        for (ApplicationInfo app : installedAppList) {
                            subscriber.onNext(new AppData(app.loadLabel(pm).toString(), app.loadIcon(pm), app.packageName));
                        }
                        subscriber.onCompleted();
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
