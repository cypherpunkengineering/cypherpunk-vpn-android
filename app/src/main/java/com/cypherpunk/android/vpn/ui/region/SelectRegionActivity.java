package com.cypherpunk.android.vpn.ui.region;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.commonsware.cwac.merge.MergeAdapter;
import com.cypherpunk.android.vpn.R;


public class SelectRegionActivity extends AppCompatActivity {

    public static String EXTRA_AREA = "area";
    private static String[] AREA = {"Auto select region", "North America", "South America", "Europe", "Asia", "Pacific"};
    private static String[] CONNECTED = {"Tokyo, Japan", "New York, USA"};
    private static final int REQUEST_SELECT_REGION = 1;
    private ListView listView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.close_vector);
        }

        listView = new ListView(this);
        MergeAdapter mergeAdapter = new MergeAdapter();
        LayoutInflater inflater = LayoutInflater.from(this);

        // recently connected
        mergeAdapter.addView(buildHeader(R.string.select_region_list_header_recent));
        ArrayAdapter<String> regionAdapter = new ArrayAdapter<>(this, R.layout.list_item_city);
        regionAdapter.addAll(CONNECTED);
        mergeAdapter.addAdapter(regionAdapter);
        mergeAdapter.addView(inflater.inflate(R.layout.divider, listView, false));

        // region
        mergeAdapter.addView(buildHeader(R.string.select_region_list_header));
        ArrayAdapter<String> regionAdapter1 = new ArrayAdapter<>(this, R.layout.list_item_area);
        regionAdapter1.addAll(AREA);
        mergeAdapter.addAdapter(regionAdapter1);
        mergeAdapter.addView(inflater.inflate(R.layout.divider, listView, false));

        // sort by distance
        mergeAdapter.addView(buildSortByDistanceButton());

        listView.setAdapter(mergeAdapter);
        listView.setDivider(null);
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

    private View buildHeader(@StringRes int text) {
        TextView textView
                = (TextView) LayoutInflater.from(this).inflate(R.layout.list_item_header_region_select, listView, false);
        textView.setText(text);
        return textView;
    }

    private View buildSortByDistanceButton() {
        View view = LayoutInflater.from(this).inflate(R.layout.list_item_footer_region, listView, false);
        View button = view.findViewById(R.id.sort_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Sort by distance screen
            }
        });
        return view;
    }
}
