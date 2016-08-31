package com.cypherpunk.android.vpn.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.databinding.ActivityListPreferenceBinding;
import com.cypherpunk.android.vpn.databinding.ListItemListPreferenceBinding;
import com.cypherpunk.android.vpn.model.SettingItem;

import java.util.ArrayList;

public class ListPreferenceActivity extends AppCompatActivity {

    public static String EXTRA_TITLE = "title";
    public static String EXTRA_LIST = "list";

    @NonNull
    public static Intent createIntent(@NonNull Context context, @NonNull String title, @NonNull ArrayList<SettingItem> list) {
        Intent intent = new Intent(context, ListPreferenceActivity.class);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_LIST, list);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityListPreferenceBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_list_preference);

        setSupportActionBar(binding.toolbar.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            binding.toolbar.title.setText(getIntent().getStringExtra(EXTRA_TITLE));
        }

        SettingItemAdapter adapter = new SettingItemAdapter(this);
        adapter.addAll((ArrayList<SettingItem>) getIntent().getSerializableExtra(EXTRA_LIST));
        binding.list.setAdapter(adapter);
        binding.list.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        binding.list.setItemChecked(0, true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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

    private class SettingItemAdapter extends ArrayAdapter<SettingItem> {

        private LayoutInflater inflater;

        public SettingItemAdapter(Context context) {
            super(context, 0);
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ListItemListPreferenceBinding binding;
            if (convertView == null) {
                binding = DataBindingUtil.inflate(inflater, R.layout.list_item_list_preference, parent, false);
                convertView = binding.getRoot();
                convertView.setTag(binding);
            } else {
                binding = (ListItemListPreferenceBinding) convertView.getTag();
            }
            binding.settingItem.setSetting(getItem(position));

            return convertView;
        }
    }
}
