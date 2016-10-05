package com.cypherpunk.android.vpn.ui.status;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import com.cypherpunk.android.vpn.CypherpunkApplication;
import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.data.api.JsonipService;
import com.cypherpunk.android.vpn.data.api.json.JsonipResult;
import com.cypherpunk.android.vpn.databinding.ActivityStatusBinding;
import com.cypherpunk.android.vpn.model.IpStatus;
import com.cypherpunk.android.vpn.model.Location;
import com.cypherpunk.android.vpn.vpn.CypherpunkVpnStatus;

import javax.inject.Inject;

import de.blinkt.openvpn.core.VpnStatus;
import io.realm.Realm;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;


public class StatusActivity extends AppCompatActivity implements VpnStatus.StateListener {

    public static String EXTRA_STATUS = "ip_status";
    public static String EXTRA_LOCATION_ID = "location_id";

    private ActivityStatusBinding binding;
    private CypherpunkVpnStatus status;
    private Subscription subscription = Subscriptions.empty();
    private IpStatus ipStatus = new IpStatus();
    private Location location;

    @Inject
    JsonipService webService;

    @NonNull
    public static Intent createIntent(@NonNull Context context, @NonNull String locationId, @NonNull IpStatus ipStatus) {
        Intent intent = new Intent(context, StatusActivity.class);
        intent.putExtra(EXTRA_LOCATION_ID, locationId);
        intent.putExtra(EXTRA_STATUS, ipStatus);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((CypherpunkApplication) getApplication()).getAppComponent().inject(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_status);

        setSupportActionBar(binding.toolbar.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            binding.toolbar.title.setText(R.string.title_activity_status);
        }

        status = CypherpunkVpnStatus.getInstance();
        Intent intent = getIntent();
        ipStatus = intent.getParcelableExtra(EXTRA_STATUS);

        binding.time.setBaseTime(status.getConnectedTime());
        if (!TextUtils.isEmpty(ipStatus.getOriginalIp())) {
            binding.originalIp.setText(ipStatus.getOriginalIp());
        }
        if (!TextUtils.isEmpty(ipStatus.getNewIp())) {
            binding.newIp.setText(ipStatus.getNewIp());
        } else if (status.isConnected()) {
            getIpAddress();
        }

        Realm realm = Realm.getDefaultInstance();
        location = realm.where(Location.class).equalTo("id", intent.getStringExtra(EXTRA_LOCATION_ID)).findFirst();
        realm.close();

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
                binding.state.setText(connected ? R.string.status_connected : R.string.status_disconnected);
                binding.state.setTextColor(ContextCompat.getColor(StatusActivity.this,
                        connected ? R.color.status_connected : R.color.status_disconnected));

                if (connected) {
                    binding.map.setNewPosition(location.getMapX(), location.getMapY());
                    binding.newLocation.setText(location.getCity());
                }
                binding.map.setNewPositionVisibility(connected);
            }
        });
    }

    private void getIpAddress() {
        subscription = webService.getIpAddress()
                .subscribeOn(Schedulers.newThread())
                .retry(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<JsonipResult>() {
                    @Override
                    public void onSuccess(JsonipResult jsonipResult) {
                        if (status.isConnected()) {
                            ipStatus.setNewIp(jsonipResult.getIp());
                            binding.newIp.setText(ipStatus.getNewIp());

                            Intent intent = new Intent();
                            intent.putExtra(EXTRA_STATUS, ipStatus);
                            setResult(RESULT_OK, intent);
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        error.printStackTrace();
                        if (status.isConnected()) {
                            ipStatus.setNewIp("");
                            binding.newIp.setText("-");

                            Intent intent = new Intent();
                            intent.putExtra(EXTRA_STATUS, ipStatus);
                            setResult(RESULT_OK, intent);
                        }
                    }
                });
    }
}
