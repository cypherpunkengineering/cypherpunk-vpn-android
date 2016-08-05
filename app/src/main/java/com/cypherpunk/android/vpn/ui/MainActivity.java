package com.cypherpunk.android.vpn.ui;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Paint;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionMenuView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.data.api.UserManager;
import com.cypherpunk.android.vpn.databinding.ActivityMainBinding;
import com.cypherpunk.android.vpn.ui.account.AccountActivity;
import com.cypherpunk.android.vpn.ui.region.SelectCityActivity;
import com.cypherpunk.android.vpn.ui.region.SelectRegionActivity;
import com.cypherpunk.android.vpn.ui.settings.SettingsActivity;
import com.cypherpunk.android.vpn.vpn.CypherpunkVPN;
import com.cypherpunk.android.vpn.widget.ConnectionStatusView;
import com.cypherpunk.android.vpn.widget.VpnButton;

import de.blinkt.openvpn.core.VpnStatus;

public class MainActivity extends AppCompatActivity implements VpnStatus.StateListener {

    private static final int REQUEST_VPN_START = 0;
    private static final int REQUEST_SELECT_REGION = 1;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!UserManager.getInstance(this).isSignedIn()) {
            Intent intent = new Intent(this, IntroductionActivity.class);
            TaskStackBuilder builder = TaskStackBuilder.create(this);
            builder.addNextIntent(intent);
            builder.startActivities();
            finish();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        setSupportActionBar(binding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowTitleEnabled(false);

        binding.connectionButton.setOnCheckedChangeListener(new VpnButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startVpn();
                } else {
                    stopVpn();
                }
            }
        });

        // TODO: set progress
        binding.connectingProgress.setProgress(70);
        binding.connectingCancelButton.setPaintFlags(
                binding.connectingCancelButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        binding.regionContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, SelectRegionActivity.class), REQUEST_SELECT_REGION);
            }
        });

        binding.actionAccount.setOnMenuItemClickListener(new ActionMenuView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelected(item);
            }
        });

        binding.connectingCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // cancel
                stopVpn();
            }
        });

        VpnStatus.addStateListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_left, menu);
        getMenuInflater().inflate(R.menu.main_right, binding.actionAccount.getMenu());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setting:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.action_account:
                startActivity(new Intent(this, AccountActivity.class));
                break;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_VPN_START:
                    startVpn();
                    break;
                case REQUEST_SELECT_REGION:
                    String city = data.getStringExtra(SelectCityActivity.EXTRA_CITY);
                    binding.region.setText(city);
                    if (data.getBooleanExtra(SelectCityActivity.EXTRA_CONNECT, false)) {
                        startVpn();
                    }
                    break;
            }
        }
    }

    @Override
    public void updateState(String state, String logmessage,
                            int localizedResId, VpnStatus.ConnectionStatus level) {
        if (level == VpnStatus.ConnectionStatus.LEVEL_CONNECTED) {
            onVpnConnected();
        } else if (level == VpnStatus.ConnectionStatus.LEVEL_NOTCONNECTED
                || level == VpnStatus.ConnectionStatus.LEVEL_NONETWORK) {
            onVpnDisconnected();
        }
    }

    private void startVpn() {
        Intent intent = VpnService.prepare(MainActivity.this);
        if (intent != null) {
            startActivityForResult(intent, REQUEST_VPN_START);
        } else {
            CypherpunkVPN.start(getApplicationContext(), getBaseContext());
            binding.connectionStatus.setStatus(ConnectionStatusView.CONNECTING);
            binding.connectionButton.setStatus(VpnButton.CONNECTING);
            binding.connectingProgressContainer.setVisibility(View.VISIBLE);
        }
    }

    private void stopVpn() {
        CypherpunkVPN.stop(getApplicationContext(), getBaseContext());
    }

    private void onVpnConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.keyTexture.startAnimation();
                binding.connectionStatus.setStatus(ConnectionStatusView.CONNECTED);
                binding.connectionButton.setStatus(VpnButton.CONNECTED);
                binding.connectingProgressContainer.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void onVpnDisconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.keyTexture.stopAnimation();
                binding.connectionStatus.setStatus(ConnectionStatusView.DISCONNECTED);
                binding.connectionButton.setStatus(VpnButton.DISCONNECTED);
                binding.connectingProgressContainer.setVisibility(View.INVISIBLE);
            }
        });
    }
}
