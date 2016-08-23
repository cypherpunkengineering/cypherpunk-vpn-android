package com.cypherpunk.android.vpn.ui.status;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.databinding.ActivityStatusBinding;
import com.cypherpunk.android.vpn.vpn.CypherpunkVpnStatus;

import de.blinkt.openvpn.core.VpnStatus;


public class StatusActivity extends AppCompatActivity implements VpnStatus.StateListener {

    private ActivityStatusBinding binding;
    private CypherpunkVpnStatus status;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_status);
        status = CypherpunkVpnStatus.getInstance();

        // TODO:
        String originalIp = getIntent().getStringExtra("original_ip");
        if (!TextUtils.isEmpty(originalIp)) {
            binding.originalIp.setText(originalIp);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        binding.map.setOriginalPosition(305, 56);

        VpnStatus.addStateListener(this);
        refresh();
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
    public void updateState(String state, String logmessage, int localizedResId, VpnStatus.ConnectionStatus level) {
        refresh();
    }

    private void refresh() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                boolean connected = status.isConnected();
                binding.newContainer.setVisibility(connected ? View.VISIBLE : View.GONE);
                binding.changeIpButton.setVisibility(connected ? View.VISIBLE : View.GONE);
                binding.time.setVisibility(connected ? View.VISIBLE : View.GONE);
                binding.state.setText(connected ? "Connected" : "Disconnected");
                binding.state.setTextColor(ContextCompat.getColor(StatusActivity.this,
                        connected ? R.color.status_connected : R.color.status_disconnected));

                if (connected) {
                    binding.map.setNewPosition(20, 59);
                }
                binding.map.setNewPositionVisibility(connected);
            }
        });
    }
}
