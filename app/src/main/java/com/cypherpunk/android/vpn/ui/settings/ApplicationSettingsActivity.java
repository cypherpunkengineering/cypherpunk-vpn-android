package com.cypherpunk.android.vpn.ui.settings;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

public class ApplicationSettingsActivity extends AppCompatActivity {

    private Subscription subscription = Subscriptions.empty();
    private ApplicationAdapter adapter;
    private ActivityApplicationSettingsBinding binding;

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
        // TODO: sort
        subscription = Observable
                .create(new Observable.OnSubscribe<List<AppData>>() {
                    @Override
                    public void call(Subscriber<? super List<AppData>> subscriber) {
                        final PackageManager pm = getPackageManager();
                        List<ApplicationInfo> installedAppList = pm.getInstalledApplications(PackageManager.GET_META_DATA);
                        final List<AppData> list = new ArrayList<>();
                        for (ApplicationInfo app : installedAppList) {
                            list.add(new AppData(app.loadLabel(pm).toString(), app.loadIcon(pm), app.packageName));
                        }
                        subscriber.onNext(list);
                        subscriber.onCompleted();

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
                        binding.progress.setVisibility(View.GONE);
                        binding.list.setVisibility(View.VISIBLE);
                    }
                });
    }
}
