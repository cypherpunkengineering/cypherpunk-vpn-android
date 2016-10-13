package com.cypherpunk.android.vpn.ui.settings;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.databinding.ActivityNetworkBinding;
import com.cypherpunk.android.vpn.databinding.ListItemWifiBinding;
import com.cypherpunk.android.vpn.model.CypherpunkSetting;
import com.cypherpunk.android.vpn.model.Network;

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
        }
        cypherpunkSetting = new CypherpunkSetting();

        binding.list.setDivider(new ColorDrawable(ContextCompat.getColor(this, R.color.background)));
        binding.list.setDividerHeight(getResources().getDimensionPixelSize(R.dimen.divider));
        binding.list.addHeaderView(buildListHeader());

        realm = Realm.getDefaultInstance();

        ArrayAdapter<Network> adapter = new WifiAdapter(this);
        adapter.addAll(getNetworks());
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
        List<Network> list = new ArrayList<>();
        for (WifiConfiguration configuredNetwork : configuredNetworks) {
            String ssid = configuredNetwork.SSID.replace("\"", "");
            Network network = realm.where(Network.class)
                    .equalTo("ssid", ssid).findFirst();
            if (network == null) {
                list.add(new Network(ssid));
            }
        }
        realm.beginTransaction();
        realm.copyToRealm(list);
        realm.commitTransaction();

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

    private class WifiAdapter extends ArrayAdapter<Network> {

        private LayoutInflater inflater;

        WifiAdapter(Context context) {
            super(context, 0);
            this.inflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
            ListItemWifiBinding binding;
            if (convertView == null) {
                binding = DataBindingUtil.inflate(inflater, R.layout.list_item_wifi, parent, false);
                convertView = binding.getRoot();
                convertView.setTag(binding);
            } else {
                binding = (ListItemWifiBinding) convertView.getTag();
            }
            final Network item = getItem(position);
            binding.networkItem.setText(item.getSsid());
            binding.networkItem.setChecked(item.isTrusted());
            binding.networkItem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            getItem(position).setTrusted(isChecked);
                        }
                    });
                }
            });

            return convertView;
        }
    }
}
