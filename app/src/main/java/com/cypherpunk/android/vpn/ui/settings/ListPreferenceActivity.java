package com.cypherpunk.android.vpn.ui.settings;

import android.app.Activity;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.databinding.ActivityListPreferenceBinding;
import com.cypherpunk.android.vpn.databinding.ListItemListPreferenceBinding;
import com.cypherpunk.android.vpn.model.CypherpunkSetting;
import com.cypherpunk.android.vpn.model.SettingItem;

import java.util.ArrayList;

public class ListPreferenceActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener {

    public static String EXTRA_TITLE = "title";
    public static String EXTRA_VALUE = "value";
    public static String EXTRA_LIST = "list";

    public static final String EXTRA_KEY = "key";
    public static final String EXTRA_SELECTED_NAME = "name";
    public static final String EXTRA_SELECTED_VALUE = "value";

    private ArrayList<SettingItem> items;

    @NonNull
    public static Intent createIntent(@NonNull Context context, @NonNull CharSequence key,
                                      @NonNull CharSequence title, @NonNull String value, @NonNull ArrayList<SettingItem> list) {
        Intent intent = new Intent(context, ListPreferenceActivity.class);
        intent.putExtra(EXTRA_KEY, key);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_VALUE, value);
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
            binding.toolbar.title.setText(getIntent().getCharSequenceExtra(EXTRA_TITLE));
        }

        SettingItemAdapter adapter = new SettingItemAdapter(this);
        items = (ArrayList<SettingItem>) getIntent().getSerializableExtra(EXTRA_LIST);
        adapter.addAll(items);
        binding.list.setAdapter(adapter);
        binding.list.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        binding.list.setOnItemClickListener(this);

        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).value.equals(getIntent().getStringExtra(EXTRA_VALUE))) {
                binding.list.setItemChecked(i, true);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_KEY, getIntent().getStringExtra(EXTRA_KEY));
        intent.putExtra(EXTRA_SELECTED_NAME, items.get(position).title);
        intent.putExtra(EXTRA_SELECTED_VALUE, items.get(position).value);
        setResult(Activity.RESULT_OK, intent);
        finish();
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
