package com.cypherpunk.android.vpn.ui.main;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Paint;
import android.net.Uri;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionMenuView;
import android.telephony.TelephonyManager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
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
import com.cypherpunk.android.vpn.model.CypherpunkSetting;
import com.cypherpunk.android.vpn.model.Location;
import com.cypherpunk.android.vpn.ui.region.LocationsActivity;
import com.cypherpunk.android.vpn.ui.settings.RateDialogFragment;
import com.cypherpunk.android.vpn.ui.settings.SettingsActivity;
import com.cypherpunk.android.vpn.ui.setup.IntroductionActivity;
import com.cypherpunk.android.vpn.ui.status.StatusActivity;
import com.cypherpunk.android.vpn.vpn.CypherpunkVPN;
import com.cypherpunk.android.vpn.vpn.CypherpunkVpnStatus;
import com.cypherpunk.android.vpn.widget.BinaryTextureView;
import com.cypherpunk.android.vpn.widget.ConnectionStatusView;
import com.cypherpunk.android.vpn.widget.VpnFlatButton;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.blinkt.openvpn.core.VpnStatus;
import io.realm.Realm;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

public class MainActivity extends AppCompatActivity
        implements VpnStatus.StateListener, RateDialogFragment.RateDialogListener {

    public static final String AUTO_START = "com.cypherpunk.android.vpn.AUTO_START";

    public static final int REQUEST_VPN_START = 0;
    public static final int REQUEST_SELECT_REGION = 1;
    public static final int REQUEST_SETTINGS = 3;

    private ActivityMainBinding binding;
    private CypherpunkVpnStatus status;
    private Subscription subscription = Subscriptions.empty();
    private String locationId;
    private Realm realm;

    @Inject
    JsonipService webService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!UserManager.isSignedIn()) {
            Intent intent = new Intent(this, IntroductionActivity.class);
            TaskStackBuilder builder = TaskStackBuilder.create(this);
            builder.addNextIntent(intent);
            builder.startActivities();
            finish();
        }

        ((CypherpunkApplication) getApplication()).getAppComponent().inject(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        status = CypherpunkVpnStatus.getInstance();
        setSupportActionBar(binding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowTitleEnabled(false);

        // background
        String operatorName = getSimOperatorName();
        if (TextUtils.isEmpty(operatorName)) {
            String[] text = {Build.BRAND.toUpperCase(), Build.MODEL.toUpperCase()};
            binding.binaryTextureView.setText(text);
        } else {
            String[] text = {Build.BRAND.toUpperCase(), Build.MODEL.toUpperCase(), operatorName};
            binding.binaryTextureView.setText(text);
        }

        // showSignUpButton();

        // TODO;
        realm = Realm.getDefaultInstance();
        Location location = realm.where(Location.class).equalTo("selected", true).findFirst();
        if (location == null) {
            getServerList();
            location = realm.where(Location.class).equalTo("selected", true).findFirst();
        }

        locationId = location.getId();
        binding.region.setText(location.getCity());
        Picasso.with(this).load(location.getNationalFlagUrl()).into(binding.nationalFlag);

        binding.connectionButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                CypherpunkVPN.getInstance().toggle(getApplicationContext(), getBaseContext());
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
                startActivityForResult(new Intent(this, SettingsActivity.class), REQUEST_SETTINGS);
                break;
            case R.id.action_status:
                startActivity(StatusActivity.createIntent(this, locationId));
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
                    locationId = data.getStringExtra(LocationsActivity.EXTRA_LOCATION_ID);
                    Location location = realm.where(Location.class).equalTo("id", locationId).findFirst();
                    binding.region.setText(location.getCity());
                    Picasso.with(this).load(location.getNationalFlagUrl()).into(binding.nationalFlag);
                    if (CypherpunkVPN.getInstance().getLocation() != location) {
                        CypherpunkVPN.getInstance().setLocation(location);

                        if (data.getBooleanExtra(LocationsActivity.EXTRA_CONNECT, false)) {
                            startVpn();
                        } else {
                            stopVpn();
                        }
                    }
                    break;
                case REQUEST_SETTINGS:
                    if (data.getBooleanExtra(LocationsActivity.EXTRA_CONNECT, false)) {
                        startVpn();
                    } else {
                        stopVpn();
                    }
            }
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        binding.binaryTextureView.startAnimation();

        /*
        Intent intent = getIntent();
        checkIfAutoStart(intent);
        checkIfTileClick(intent);
        setIntent(null);
        */
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 画面が黒くなってしまうから、アニメーションを止める
        binding.binaryTextureView.stopAnimation();
    }

    @Override
    protected void onDestroy() {
        subscription.unsubscribe();
        realm.close();
        super.onDestroy();
    }

    @Override
    public void onRateNowButtonClick() {
        startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=com.cypherpunk.android.vpn.debug")));
    }

    @Override
    public void updateState(String state, String logmessage,
                            int localizedResId, VpnStatus.ConnectionStatus level) {
        switch (level) {
            case LEVEL_CONNECTED:
                onVpnConnected();
                break;
            case LEVEL_NOTCONNECTED:
                onVpnDisconnected();
                break;
        }
    }

    private static void log(String str) {
        Log.w("CypherpunkVPN", str);
    }

    private void startVpn() {
        Intent intent = VpnService.prepare(MainActivity.this);
        if (intent != null) {
            startActivityForResult(intent, REQUEST_VPN_START);
        } else {
            binding.binaryTextureView.setState(BinaryTextureView.CONNECTING);
            binding.connectionStatus.setStatus(ConnectionStatusView.CONNECTING);
            binding.connectionButton.setStatus(VpnFlatButton.CONNECTING);
            binding.connectingCancelButton.setVisibility(View.VISIBLE);
            CypherpunkVPN.getInstance().start(getApplicationContext(), getBaseContext());
        }
    }

    private void stopVpn() {
        if (status.isDisconnected()) {
            return;
        }
        CypherpunkVPN.getInstance().stop(getApplicationContext(), getBaseContext());
    }

    private void onVpnConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.binaryTextureView.setState(BinaryTextureView.CONNECTED);
                binding.connectionStatus.setStatus(ConnectionStatusView.CONNECTED);
                binding.connectionButton.setStatus(VpnFlatButton.CONNECTED);
                binding.connectingCancelButton.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void onVpnDisconnected() {
        getIpAddress();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.binaryTextureView.setState(BinaryTextureView.DISCONNECTED);
                binding.connectionStatus.setStatus(ConnectionStatusView.DISCONNECTED);
                binding.connectionButton.setStatus(VpnFlatButton.DISCONNECTED);
                binding.connectingCancelButton.setVisibility(View.INVISIBLE);
            }
        });
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
                            status.setOriginalIp(jsonipResult.getIp());
                        } else if (!TextUtils.isEmpty(status.getNewIp())
                                && !status.getNewIp().equals(jsonipResult.getIp())) {
                            status.setNewIp(jsonipResult.getIp());
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        error.printStackTrace();
                    }
                });
    }

    private void getServerList() {
        realm.beginTransaction();
        List<Location> locations = new ArrayList<>();
        // TODO: serverList api
        locations.add(new Location("Tokyo Dev", "JP", "freebsd-test.tokyo.vpn.cypherpunk.network", "208.111.52.34", "208.111.52.35", "208.111.52.36", "208.111.52.37", "http://flags.fmcdn.net/data/flags/normal/jp.png", 305, 56));
        locations.add(new Location("Tokyo", "JP", "freebsd2.tokyo.vpn.cypherpunk.network", "208.111.52.2", "208.111.52.12", "208.111.52.22", "208.111.52.32", "http://flags.fmcdn.net/data/flags/normal/jp.png", 305, 56));
        locations.add(new Location("Honolulu", "US", "honolulu.vpn.cypherpunk.network", "199.68.252.203", "199.68.252.203", "199.68.252.203", "199.68.252.203", "http://flags.fmcdn.net/data/flags/normal/us.png", 355, 66));
        realm.copyToRealm(locations);
        Location first = realm.where(Location.class).findFirst();
        first.setSelected(true);
        realm.commitTransaction();
    }

    private String getSimOperatorName() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        return telephonyManager.getSimOperatorName();
    }

    private void showSignUpButton() {
        SpannableStringBuilder sb = new SpannableStringBuilder(getString(R.string.main_sign_up));
        sb.setSpan(new TextAppearanceSpan(this, R.style.TextAppearance_Cypherpunk_Yellow), 0, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.signUpButton.setText(sb);
        binding.signUpButton.setVisibility(View.VISIBLE);
    }
}
