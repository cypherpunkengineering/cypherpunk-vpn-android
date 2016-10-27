package com.cypherpunk.android.privacy.ui.settings;

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

import com.cypherpunk.android.privacy.R;
import com.cypherpunk.android.privacy.databinding.ActivityListPreferenceBinding;
import com.cypherpunk.android.privacy.databinding.ListItemListPreferenceBinding;
import com.cypherpunk.android.privacy.model.SettingItem;

import java.util.ArrayList;

public class ListPreferenceActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener {

    public static String EXTRA_TITLE = "title";
    public static String EXTRA_VALUE = "value";
    public static String EXTRA_LIST = "list";

    public static final String EXTRA_KEY = "key";
    public static final String EXTRA_SELECTED_VALUE = "selected_value";

    private ArrayList<SettingItem> items;
    private String selectedItem;

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
            actionBar.setHomeAsUpIndicator(R.drawable.close_vector);
        }

        SettingItemAdapter adapter = new SettingItemAdapter(this);
        items = (ArrayList<SettingItem>) getIntent().getSerializableExtra(EXTRA_LIST);
        adapter.addAll(items);
        binding.list.setAdapter(adapter);
        binding.list.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        binding.list.setOnItemClickListener(this);

        selectedItem = getIntent().getStringExtra(EXTRA_VALUE);
        boolean prefSelected = false;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).key.equals(selectedItem)) {
                binding.list.setItemChecked(i, true);
                prefSelected = true;
                break;
            }
        }
        // if saved preferences no longer exist due to app upgrade,
        // or if default pref not found, select 0th option
        if (!prefSelected)
            binding.list.setItemChecked(0, true);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        if (!items.get(position).key.equals(selectedItem)) {
            Intent intent = new Intent();
            intent.putExtra(EXTRA_KEY, getIntent().getStringExtra(EXTRA_KEY));
            intent.putExtra(EXTRA_SELECTED_VALUE, items.get(position).key);
            setResult(Activity.RESULT_OK, intent);
        }
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

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
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
