package com.cypherpunk.android.vpn.ui;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.VpnService;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;

import com.cypherpunk.android.vpn.CypherpunkVPN;
import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.data.api.UserManager;
import com.cypherpunk.android.vpn.databinding.ActivityMainBinding;
import com.cypherpunk.android.vpn.ui.region.SelectRegionActivity;
import com.cypherpunk.android.vpn.ui.settings.SettingsActivity;

import de.blinkt.openvpn.core.VpnStatus;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, VpnStatus.StateListener {

    private static final int REQUEST_VPN_START = 0;
    private static final int REQUEST_VPN_STOP = 1;
    private static final int REQUEST_SELECT_REGION = 2;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        if (!UserManager.getInstance(this).isSignedIn()) {
            Intent intent = new Intent(this, TutorialActivity.class);
            TaskStackBuilder builder = TaskStackBuilder.create(this);
            builder.addNextIntent(intent);
            builder.startActivities();
            finish();
        }
        setSupportActionBar(binding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowTitleEnabled(false);

        binding.connectionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Intent intent = VpnService.prepare(MainActivity.this);
                    if (intent != null) {
                        startActivityForResult(intent, REQUEST_VPN_START);
                    } else {
                        onActivityResult(REQUEST_VPN_START, RESULT_OK, null);
                    }
                } else {
                    onActivityResult(REQUEST_VPN_STOP, RESULT_OK, null);
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        binding.pin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, SelectRegionActivity.class), REQUEST_SELECT_REGION);
            }
        });

        VpnStatus.addStateListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_VPN_START:
                    CypherpunkVPN.start(getApplicationContext(), getBaseContext());
                    binding.connectionStatus.setText(R.string.status_connecting);
                    break;
                case REQUEST_VPN_STOP:
                    CypherpunkVPN.stop(getApplicationContext(), getBaseContext());
                    break;
                case REQUEST_SELECT_REGION:
                    String city = data.getStringExtra("city");
                    binding.region.setText(city);
                    break;
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setting:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.action_sign_out:
                signOut();
                break;
        }
        return false;
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

    private void onVpnConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.keyTexture.startAnimation();
                binding.connectionStatus.setText(R.string.status_connected);
            }
        });
    }

    private void onVpnDisconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.keyTexture.stopAnimation();
                binding.connectionStatus.setText(R.string.status_disconnected);
                binding.connectionSwitch.setChecked(false);
            }
        });
    }

    private void signOut() {
        CypherpunkVPN.stop(getApplicationContext(), getBaseContext());
        UserManager manager = UserManager.getInstance(this);
        manager.clearUser();
        Intent intent = new Intent(this, TutorialActivity.class);
        TaskStackBuilder builder = TaskStackBuilder.create(this);
        builder.addNextIntent(intent);
        builder.startActivities();
        finish();
    }
}
