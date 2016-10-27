package com.cypherpunk.android.privacy.ui.settings;

import android.databinding.DataBindingUtil;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.cypherpunk.android.privacy.CypherpunkApplication;
import com.cypherpunk.android.privacy.R;
import com.cypherpunk.android.privacy.databinding.ActivityNetworkBinding;
import com.cypherpunk.android.privacy.databinding.ListItemWifiBinding;
import com.cypherpunk.android.privacy.model.CypherpunkSetting;
import com.cypherpunk.android.privacy.model.Network;
import com.cypherpunk.android.privacy.widget.NetworkItemView;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class NetworkActivity extends AppCompatActivity {

    private ActivityNetworkBinding binding;
    private Realm realm;
    private CypherpunkSetting cypherpunkSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_network);

        setSupportActionBar(binding.toolbar.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            binding.toolbar.title.setText(R.string.title_activity_network);
            actionBar.setHomeAsUpIndicator(R.drawable.close_vector);
        }
        cypherpunkSetting = new CypherpunkSetting();

        realm = CypherpunkApplication.instance.getAppComponent().getDefaultRealm();

        NetworkAdapter adapter = new NetworkAdapter(getNetworks(), buildListHeader()) {
            @Override
            protected void onNetworkCheckedChanged(Network network, boolean isChecked) {
                realm.beginTransaction();
                network.setTrusted(isChecked);
                realm.commitTransaction();
            }
        };
        binding.list.setAdapter(adapter);
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

    @Override
    protected void onDestroy() {
        realm.close();
        super.onDestroy();
    }

    @NonNull
    private List<Network> getNetworks() {
        List<WifiConfiguration> configuredNetworks = getConfiguredNetworks();
        for (WifiConfiguration configuredNetwork : configuredNetworks) {
            String ssid = configuredNetwork.SSID.replace("\"", "");
            Network network = realm.where(Network.class)
                    .equalTo("ssid", ssid).findFirst();
            if (network == null) {
                realm.beginTransaction();
                realm.copyToRealm(new Network(ssid));
                realm.commitTransaction();
            }
        }

        RealmResults<Network> networks = realm.where(Network.class).findAll();
        return new ArrayList<>(networks);
    }

    @NonNull
    private List<WifiConfiguration> getConfiguredNetworks() {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        return wifiManager.getConfiguredNetworks();
    }

    private View buildListHeader() {
        final View header = LayoutInflater.from(this)
                .inflate(R.layout.list_item_header_neteork_secure, binding.list, false);
        final SwitchCompat autoSecureSwitch = (SwitchCompat) header.findViewById(R.id.auto_secure_switch);
        autoSecureSwitch.setChecked(cypherpunkSetting.autoSecureUntrusted);
        header.findViewById(R.id.auto_secure_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoSecureSwitch.toggle();
                cypherpunkSetting.autoSecureUntrusted = autoSecureSwitch.isChecked();
                cypherpunkSetting.save();
            }
        });
        final SwitchCompat otherAutoSecureSwitch =
                (SwitchCompat) header.findViewById(R.id.other_auto_secure_switch);
        otherAutoSecureSwitch.setChecked(cypherpunkSetting.autoSecureOther);
        header.findViewById(R.id.other_auto_secure_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                otherAutoSecureSwitch.toggle();
                cypherpunkSetting.autoSecureOther = otherAutoSecureSwitch.isChecked();
                cypherpunkSetting.save();
            }
        });
        return header;
    }

    public static class NetworkAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int ITEM_VIEW_TYPE_HEADER = 0;
        private static final int ITEM_VIEW_TYPE_ITEM = 1;

        private List<Network> items = new ArrayList<>();
        private final View headerView;

        protected void onNetworkCheckedChanged(Network network, boolean isChecked) {
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
                    return new NetworkAdapter.ItemViewHolder(LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.list_item_wifi, parent, false));
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
                    ItemViewHolder viewHolder = (ItemViewHolder) holder;
                    NetworkItemView itemView = viewHolder.getBinding().networkItem;
                    final Network network = items.get(position - 1);
                    itemView.setNetwork(network);
                    itemView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            onNetworkCheckedChanged(network, isChecked);
                        }
                    });
                    viewHolder.getBinding().executePendingBindings();
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

            private ListItemWifiBinding binding;

            ItemViewHolder(View view) {
                super(view);
                binding = DataBindingUtil.bind(view);
            }

            public ListItemWifiBinding getBinding() {
                return binding;
            }
        }

        static class HeaderViewHolder extends RecyclerView.ViewHolder {

            HeaderViewHolder(View view) {
                super(view);
            }
        }
    }
}
