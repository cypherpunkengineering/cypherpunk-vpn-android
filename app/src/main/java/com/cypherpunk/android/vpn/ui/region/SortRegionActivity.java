package com.cypherpunk.android.vpn.ui.region;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.cypherpunk.android.vpn.R;

public class SortRegionActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener,
        ConnectConfirmationDialogFragment.ConnectDialogListener {
    private static String[] CITY = {"London, UK", "Paris, France", "Zurich, Swiss", "Amsterdam, Netherlands", "Frankfurt, Germany"};

    private ArrayAdapter<String> adapter;
    private String selectedItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        ListView listView = new ListView(this);
        adapter = new ArrayAdapter<>(this, R.layout.list_item_city);
        adapter.addAll(CITY);
        listView.setDivider(null);
        listView.setAdapter(adapter);
        setContentView(listView);
        listView.setOnItemClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, final int position, long id) {
        selectedItem = adapter.getItem(position);
        ConnectConfirmationDialogFragment dialogFragment = new ConnectConfirmationDialogFragment();
        dialogFragment.show(getSupportFragmentManager());
    }

    @Override
    public void onDialogPositiveButtonClick() {
        Intent intent = new Intent();
        intent.putExtra(SelectCityActivity.EXTRA_CITY, selectedItem);
        intent.putExtra(SelectCityActivity.EXTRA_CONNECT, true);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onDialogNegativeButtonClick() {
        Intent intent = new Intent();
        intent.putExtra(SelectCityActivity.EXTRA_CITY, selectedItem);
        setResult(RESULT_OK, intent);
        finish();
    }
}
