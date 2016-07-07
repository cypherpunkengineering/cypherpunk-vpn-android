package com.cypherpunk.android.vpn.ui.region;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.cypherpunk.android.vpn.R;

public class SelectCityActivity extends AppCompatActivity {

    private static String[] CITY = {"London", "Turkey", "Switzerland", "Sweden", "Singapore"};
    public static String EXTRA_CITY = "city";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            String area = getIntent().getStringExtra(SelectRegionActivity.EXTRA_AREA);
            actionBar.setTitle(area);
        }

        final ListView listView = new ListView(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item_city);
        adapter.addAll(CITY);
        listView.setAdapter(adapter);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        setContentView(listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent();
                intent.putExtra(EXTRA_CITY, CITY[listView.getCheckedItemPosition()]);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        listView.setItemChecked(0, true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
