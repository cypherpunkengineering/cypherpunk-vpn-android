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


public class SelectRegionActivity extends AppCompatActivity {

    public static String EXTRA_AREA = "area";
    private static String[] AREA = {"US", "Europe", "Asia", "Australia", "South America", "USA"};
    private static final int REQUEST_SELECT_REGION = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.close);
        }

        final ListView listView = new ListView(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item_area);
        adapter.addAll(AREA);
        listView.setAdapter(adapter);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        setContentView(listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(SelectRegionActivity.this, SelectCityActivity.class);
                intent.putExtra(EXTRA_AREA, AREA[listView.getCheckedItemPosition()]);
                startActivityForResult(intent, REQUEST_SELECT_REGION);
            }
        });
        listView.setItemChecked(0, true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_SELECT_REGION:
                    setResult(RESULT_OK, data);
                    finish();
                    break;
            }
        }
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
