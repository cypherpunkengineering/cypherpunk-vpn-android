package com.cypherpunk.android.vpn.ui.main;

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
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;

import com.cypherpunk.android.vpn.CypherpunkApplication;
import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.data.api.JsonipService;
import com.cypherpunk.android.vpn.data.api.UserManager;
import com.cypherpunk.android.vpn.data.api.json.JsonipResult;
import com.cypherpunk.android.vpn.databinding.ActivityMainBinding;
import com.cypherpunk.android.vpn.model.IpStatus;
import com.cypherpunk.android.vpn.ui.region.LocationsActivity;
import com.cypherpunk.android.vpn.ui.region.SelectCityActivity;
import com.cypherpunk.android.vpn.ui.settings.SettingsActivity;
import com.cypherpunk.android.vpn.ui.setup.IntroductionActivity;
import com.cypherpunk.android.vpn.ui.status.StatusActivity;
import com.cypherpunk.android.vpn.vpn.CypherpunkVPN;
import com.cypherpunk.android.vpn.vpn.CypherpunkVpnStatus;
import com.cypherpunk.android.vpn.widget.BinaryTextureView;
import com.cypherpunk.android.vpn.widget.ConnectionStatusView;
import com.cypherpunk.android.vpn.widget.VpnButton;

import java.util.ArrayList;

import javax.inject.Inject;

import de.blinkt.openvpn.core.VpnStatus;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

public class MainActivity extends AppCompatActivity implements VpnStatus.StateListener {

    private static final int REQUEST_VPN_START = 0;
    private static final int REQUEST_SELECT_REGION = 1;

    private ActivityMainBinding binding;
    private CypherpunkVpnStatus status;
    private Subscription subscription = Subscriptions.empty();
    private IpStatus ipStatus = new IpStatus();

    @Inject
    JsonipService webService;

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

        ((CypherpunkApplication) getApplication()).getAppComponent().inject(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        status = CypherpunkVpnStatus.getInstance();
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

        binding.connectingCancelButton.setPaintFlags(
                binding.connectingCancelButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        binding.regionContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, LocationsActivity.class), REQUEST_SELECT_REGION);
            }
        });

        binding.actionMenuLeft.setOnMenuItemClickListener(new ActionMenuView.OnMenuItemClickListener() {
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
        setBinaryStrings();

//        getIpAddress();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_right, menu);
        getMenuInflater().inflate(R.menu.main_left, binding.actionMenuLeft.getMenu());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setting:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.action_status:
                startActivity(StatusActivity.createIntent(this, ipStatus));
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("ip_status", ipStatus);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ipStatus = savedInstanceState.getParcelable("ip_status");
    }

    @Override
    protected void onDestroy() {
        subscription.unsubscribe();
        super.onDestroy();
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
        if (status.isConnected()) {
            return;
        }
        Intent intent = VpnService.prepare(MainActivity.this);
        if (intent != null) {
            startActivityForResult(intent, REQUEST_VPN_START);
        } else {
            CypherpunkVPN.start(getApplicationContext(), getBaseContext());
            binding.keyTexture.setState(BinaryTextureView.CONNECTING);
            binding.connectionStatus.setStatus(ConnectionStatusView.CONNECTING);
            binding.connectionButton.setStatus(VpnButton.CONNECTING);
            binding.connectingCancelButton.setVisibility(View.VISIBLE);
        }
    }

    private void stopVpn() {
        if (status.isDisconnected()) {
            return;
        }
        CypherpunkVPN.stop(getApplicationContext(), getBaseContext());
    }

    private void onVpnConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.keyTexture.setState(BinaryTextureView.CONNECTED);
                binding.connectionStatus.setStatus(ConnectionStatusView.CONNECTED);
                binding.connectionButton.setStatus(VpnButton.CONNECTED);
                binding.connectingCancelButton.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void onVpnDisconnected() {
        getIpAddress();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.keyTexture.setState(BinaryTextureView.DISCONNECTED);
                binding.connectionStatus.setStatus(ConnectionStatusView.DISCONNECTED);
                binding.connectionButton.setStatus(VpnButton.DISCONNECTED);
                binding.connectingCancelButton.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void setBinaryStrings() {
        ArrayList<String> strings = new ArrayList<>();
        strings.add(Build.BRAND);
        strings.add(Build.MANUFACTURER);
        strings.add(Build.MODEL);
        binding.keyTexture.setStrings(strings);
    }

    private void getIpAddress() {
        subscription = webService
                .getIpAddress()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<JsonipResult>() {
                    @Override
                    public void onSuccess(JsonipResult jsonipResult) {
                        if (status.isDisconnected()) {
                            ipStatus.setOriginalIp(jsonipResult.getIp());
                        } else if (!TextUtils.isEmpty(ipStatus.getNewIp())
                                && !ipStatus.getNewIp().equals(jsonipResult.getIp())) {
                            ipStatus.setNewIp(jsonipResult.getIp());
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        error.printStackTrace();
                    }
                });
    }
}
