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


/**
 * (Unused)
 */
public class SelectRegionActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener,
        ConnectConfirmationDialogFragment.ConnectDialogListener {

    public static String EXTRA_AREA = "area";
    private static String[] AREA = {"Auto select region", "North America", "South America", "Europe", "Asia", "Pacific"};
    private static String[] CONNECTED = {"Tokyo, Japan", "New York, USA"};
    private static final int REQUEST_SELECT_REGION = 1;

    private ListView listView;
    private MergeAdapter mergeAdapter;
    private String selectedItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        overridePendingTransition(R.anim.region_anim_in, R.anim.region_no_anim);

        listView = new ListView(this);
        mergeAdapter = new MergeAdapter();
        LayoutInflater inflater = LayoutInflater.from(this);

        // recently connected
        mergeAdapter.addView(buildHeader(R.string.location_recommended));
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this, R.layout.list_item_city);
        cityAdapter.addAll(CONNECTED);
        mergeAdapter.addAdapter(cityAdapter);
        mergeAdapter.addView(inflater.inflate(R.layout.divider, listView, false));

        // region
        mergeAdapter.addView(buildHeader(R.string.location_all_locations));
        ArrayAdapter<String> regionAdapter = new ArrayAdapter<>(this, R.layout.list_item_area);
        regionAdapter.addAll(AREA);
        mergeAdapter.addAdapter(regionAdapter);
        mergeAdapter.addView(inflater.inflate(R.layout.divider, listView, false));

        // sort by distance
        mergeAdapter.addView(buildSortByDistanceButton());

        listView.setAdapter(mergeAdapter);
        listView.setDivider(null);
        setContentView(listView);
        listView.setOnItemClickListener(this);
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

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        String item = (String) mergeAdapter.getItem(position);
        int itemViewId = mergeAdapter.getView(position, null, adapterView).getId();
        if (itemViewId == R.id.city) {
            selectedItem = item;
//            ConnectConfirmationDialogFragment dialogFragment = ConnectConfirmationDialogFragment.newInstance(selectedItem);
//            dialogFragment.show(getSupportFragmentManager());
        } else if (itemViewId == R.id.area) {
            Intent intent = new Intent(this, SelectCityActivity.class);
            intent.putExtra(EXTRA_AREA, item);
            startActivityForResult(intent, REQUEST_SELECT_REGION);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.region_anim_out);
    }

    private View buildHeader(@StringRes int text) {
        TextView textView = (TextView) LayoutInflater.from(this)
                .inflate(R.layout.list_item_header_region_select, listView, false);
        textView.setText(text);
        return textView;
    }

    private View buildSortByDistanceButton() {
        View view = LayoutInflater.from(this)
                .inflate(R.layout.list_item_footer_region, listView, false);
        View button = view.findViewById(R.id.sort_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SelectRegionActivity.this, SortRegionActivity.class);
                startActivityForResult(intent, REQUEST_SELECT_REGION);
            }
        });
        return view;
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
