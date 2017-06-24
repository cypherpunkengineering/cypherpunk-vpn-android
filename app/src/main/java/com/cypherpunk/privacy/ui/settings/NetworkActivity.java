package com.cypherpunk.privacy.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.domain.model.VpnSetting;
import com.cypherpunk.privacy.datasource.vpn.Network;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NetworkActivity extends AppCompatActivity {

    @NonNull
    public static Intent createIntent(@NonNull Context context) {
        return new Intent(context, NetworkActivity.class);
    }

    @Inject
    VpnSetting vpnSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);
        ButterKnife.bind(this);

        CypherpunkApplication.instance.getAppComponent().inject(this);

        if (!getResources().getBoolean(R.bool.is_tablet)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        final Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        final RecyclerView recyclerView = ButterKnife.findById(this, R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerDecoration(this, 2));

        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_vector);
        }

        // add wifi configuration to realm
        final WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        final List<WifiConfiguration> wifiNetworks = wifiManager.getConfiguredNetworks();
        if (wifiNetworks != null) {
            vpnSetting.addNetworks(wifiNetworks);
        }

        final View header = LayoutInflater.from(this).inflate(R.layout.list_header_network, recyclerView, false);
        {
            final View container = ButterKnife.findById(header, R.id.auto_secure_container);
            final TextView textView = ButterKnife.findById(container, R.id.name);
            textView.setText(R.string.newt_work_auto_secure_connections);
            final SwitchCompat switchView = ButterKnife.findById(container, R.id.switch_compat);
            switchView.setChecked(vpnSetting.isAutoSecureUntrusted());
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switchView.toggle();
                    vpnSetting.updateAutoSecureUntrusted(switchView.isChecked());
                }
            });
        }
        {
            final View container = ButterKnife.findById(header, R.id.other_auto_secure_container);
            final TextView textView = ButterKnife.findById(container, R.id.name);
            textView.setText(R.string.network_other_auto_secure_connections);
            final SwitchCompat switchView = ButterKnife.findById(container, R.id.switch_compat);
            switchView.setChecked(vpnSetting.isAutoSecureOther());
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switchView.toggle();
                    vpnSetting.updateAutoSecureOther(switchView.isChecked());
                }
            });
        }

        final NetworkAdapter adapter = new NetworkAdapter(vpnSetting.findAllNetwork(), header) {
            @Override
            protected void onNetworkCheckedChanged(@NonNull Network network, boolean isChecked) {
                vpnSetting.updateTrusted(network.ssid(), isChecked);
            }
        };
        recyclerView.setAdapter(adapter);
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

    static class NetworkAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int ITEM_VIEW_TYPE_HEADER = 0;
        private static final int ITEM_VIEW_TYPE_ITEM = 1;

        private List<Network> items = new ArrayList<>();
        private final View headerView;

        protected void onNetworkCheckedChanged(@NonNull Network network, boolean isChecked) {
        }

        NetworkAdapter(List<Network> items, @Nullable View headerView) {
            this.items = items;
            this.headerView = headerView;
            notifyDataSetChanged();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case ITEM_VIEW_TYPE_HEADER:
                    return new HeaderViewHolder(headerView);
                case ITEM_VIEW_TYPE_ITEM:
                    final ItemViewHolder holder = ItemViewHolder.create(LayoutInflater.from(parent.getContext()), parent);
                    holder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            final int position = holder.getAdapterPosition();
                            final Network network = items.get(position - 1);
                            onNetworkCheckedChanged(network, isChecked);
                        }
                    });
                    return holder;
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            final int viewType = holder.getItemViewType();
            switch (viewType) {
                case ITEM_VIEW_TYPE_HEADER:
                    break;
                case ITEM_VIEW_TYPE_ITEM:
                    final ItemViewHolder viewHolder = (ItemViewHolder) holder;
                    viewHolder.bind(items.get(position - 1));
            }
        }

        @Override
        public int getItemCount() {
            return items.size() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return ITEM_VIEW_TYPE_HEADER;
            } else {
                return ITEM_VIEW_TYPE_ITEM;
            }
        }

        static class ItemViewHolder extends RecyclerView.ViewHolder {

            private static final int LAYOUT_ID = R.layout.list_item_network;

            @NonNull
            public static ItemViewHolder create(@NonNull LayoutInflater inflater, ViewGroup parent) {
                return new ItemViewHolder(inflater.inflate(LAYOUT_ID, parent, false));
            }

            @BindView(R.id.name)
            TextView nameView;

            @BindView(R.id.switch_compat)
            SwitchCompat switchView;

            private ItemViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switchView.toggle();
                    }
                });
            }

            void bind(@NonNull Network network) {
                nameView.setText(network.ssid());
                switchView.setChecked(network.trusted());
            }

            void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
                switchView.setOnCheckedChangeListener(listener);
            }
        }

        static class HeaderViewHolder extends RecyclerView.ViewHolder {

            HeaderViewHolder(View view) {
                super(view);
            }
        }
    }
}
