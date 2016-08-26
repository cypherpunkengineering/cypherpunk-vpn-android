package com.cypherpunk.android.vpn.ui.settings;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.databinding.ActivityApplicationSettingsBinding;

import java.util.ArrayList;
import java.util.List;

public class ApplicationSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityApplicationSettingsBinding binding
                = DataBindingUtil.setContentView(this, R.layout.activity_application_settings);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        final PackageManager pm = getPackageManager();
        List<ApplicationInfo> installedAppList = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        final List<AppData> dataList = new ArrayList<AppData>();
        for (ApplicationInfo app : installedAppList) {
            AppData data = new AppData();
            data.name = app.loadLabel(pm).toString();
            data.icon = app.loadIcon(pm);
            dataList.add(data);
        }

        binding.list.setHasFixedSize(true);
        ApplicationAdapter adapter = new ApplicationAdapter();
        adapter.addAll(dataList);
        binding.list.setAdapter(adapter);
    }

    public static class AppData {
        String name;
        Drawable icon;
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
}
