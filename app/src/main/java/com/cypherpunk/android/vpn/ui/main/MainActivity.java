package com.cypherpunk.android.vpn.ui.main;

import android.content.Intent;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.net.VpnService;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionMenuView;
import android.telephony.TelephonyManager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.CompoundButton;

import com.cypherpunk.android.vpn.CypherpunkApplication;
import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.data.api.CypherpunkService;
import com.cypherpunk.android.vpn.data.api.UserManager;
import com.cypherpunk.android.vpn.data.api.json.LoginRequest;
import com.cypherpunk.android.vpn.data.api.json.LoginResult;
import com.cypherpunk.android.vpn.data.api.json.StatusResult;
import com.cypherpunk.android.vpn.databinding.ActivityMainBinding;
import com.cypherpunk.android.vpn.model.UserSettingPref;
import com.cypherpunk.android.vpn.ui.region.ConnectConfirmationDialogFragment;
import com.cypherpunk.android.vpn.ui.settings.AccountSettingsFragment;
import com.cypherpunk.android.vpn.ui.settings.RateDialogFragment;
import com.cypherpunk.android.vpn.ui.settings.SettingConnectDialogFragment;
import com.cypherpunk.android.vpn.ui.settings.SettingsFragment;
import com.cypherpunk.android.vpn.ui.setup.IntroductionActivity;
import com.cypherpunk.android.vpn.vpn.CypherpunkVPN;
import com.cypherpunk.android.vpn.vpn.CypherpunkVpnStatus;
import com.cypherpunk.android.vpn.widget.BinarySurfaceView;
import com.cypherpunk.android.vpn.widget.ConnectionStatusView;
import com.cypherpunk.android.vpn.widget.VpnFlatButton;

import javax.inject.Inject;

import de.blinkt.openvpn.core.VpnStatus;
import io.realm.Realm;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

public class MainActivity extends AppCompatActivity
        implements VpnStatus.StateListener, RateDialogFragment.RateDialogListener,
        ConnectConfirmationDialogFragment.ConnectDialogListener,
        SettingConnectDialogFragment.ConnectDialogListener,
        RegionFragment.RegionFragmentListener {

    public static final String AUTO_START = "com.cypherpunk.android.vpn.AUTO_START";

    public static final int REQUEST_SELECT_REGION = 1;
    public static final int REQUEST_SETTINGS = 3;
    private static final int REQUEST_VPN_START = 0;

    private ActivityMainBinding binding;
    private CypherpunkVpnStatus status;
    private Subscription subscription = Subscriptions.empty();
    private RegionFragment regionFragment;
    private ActionBarDrawerToggle drawerToggle;

    @Inject
    Realm realm;

    @Inject
    CypherpunkService webService;

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

        CypherpunkApplication.instance.getAppComponent().inject(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        status = CypherpunkVpnStatus.getInstance();
        setSupportActionBar(binding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // background
//        String operatorName = getSimOperatorName();
//        if (TextUtils.isEmpty(operatorName)) {
//            String[] text = {Build.BRAND.toUpperCase(), Build.MODEL.toUpperCase()};
//            binding.binaryView.setText(text);
//        } else {
//            String[] text = {Build.BRAND.toUpperCase(), Build.MODEL.toUpperCase(), operatorName};
//            binding.binaryView.setText(text);
//        }

        // showSignUpButton();

        binding.connectionButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                toggleVpn();
            }
        });

        binding.connectingCancelButton.setPaintFlags(
                binding.connectingCancelButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

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

        FragmentTransaction fm = getSupportFragmentManager().beginTransaction();
        regionFragment = new RegionFragment();
        fm.add(R.id.region_container, regionFragment);
        fm.commit();

        getStatus();

        // navigation drawer
        drawerToggle = new ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.drawer_open, R.string.drawer_close);
        binding.drawerLayout.addDrawerListener(drawerToggle);
        binding.toolbar.setNavigationIcon(R.drawable.account_vector);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.left_drawer, new AccountSettingsFragment()).commit();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.right_drawer, new SettingsFragment()).commit();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        ViewGroup.LayoutParams layoutParams = binding.regionContainer.getLayoutParams();
        layoutParams.height = getBottomContainerMinimumHeight();
        binding.regionContainer.requestLayout();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_right, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setting:
                binding.drawerLayout.openDrawer(GravityCompat.END);
                break;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            binding.drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_VPN_START:
                    startVpn();
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        subscription.unsubscribe();
        realm.close();
        realm = null;
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
        log("startVpn()");
        Intent intent = VpnService.prepare(MainActivity.this);
        if (intent != null) {
            startActivityForResult(intent, REQUEST_VPN_START);
        } else {
            binding.binaryView.setState(BinarySurfaceView.CONNECTING);
            binding.connectionStatus.setStatus(ConnectionStatusView.CONNECTING);
            binding.connectionButton.setStatus(VpnFlatButton.CONNECTING);
            binding.connectingCancelButton.setVisibility(View.VISIBLE);
            CypherpunkVPN.getInstance().start(getApplicationContext(), getBaseContext());
        }
    }

    private void stopVpn() {
        log("stopVpn()");
        if (status.isDisconnected()) {
            return;
        }
        CypherpunkVPN.getInstance().stop(getApplicationContext(), getBaseContext());
    }

    private void toggleVpn() {
        if (CypherpunkVpnStatus.getInstance().isDisconnected())
            startVpn();
        else
            stopVpn();
    }

    private void onVpnConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.binaryView.setState(BinarySurfaceView.CONNECTED);
                binding.connectionStatus.setStatus(ConnectionStatusView.CONNECTED);
                binding.connectionButton.setStatus(VpnFlatButton.CONNECTED);
                binding.connectingCancelButton.setVisibility(View.GONE);
            }
        });
    }

    private void onVpnDisconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.binaryView.setState(BinarySurfaceView.DISCONNECTED);
                binding.connectionStatus.setStatus(ConnectionStatusView.DISCONNECTED);
                binding.connectionButton.setStatus(VpnFlatButton.DISCONNECTED);
                binding.connectingCancelButton.setVisibility(View.GONE);
            }
        });
    }

    private void getStatus() {
        subscription = webService
                .login(new LoginRequest(UserManager.getMailAddress(), UserManager.getPassword()))
                .flatMap(new Func1<LoginResult, Single<StatusResult>>() {
                    @Override
                    public Single<StatusResult> call(LoginResult result) {
                        return webService.getStatus();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<StatusResult>() {
                    @Override
                    public void onSuccess(StatusResult status) {
                        UserSettingPref statusPref = new UserSettingPref();
                        statusPref.userStatusType = status.getType();
                        statusPref.userStatusRenewal = status.getRenewal();
                        statusPref.userStatusExpiration = status.getExpiration();
                        statusPref.save();
                    }

                    @Override
                    public void onError(Throwable error) {
                        error.printStackTrace();
                    }
                });
    }

    private String getSimOperatorName() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        return telephonyManager.getSimOperatorName();
    }

    private void showSignUpButton() {
        SpannableStringBuilder sb = new SpannableStringBuilder(getString(R.string.main_sign_up));
        sb.setSpan(new TextAppearanceSpan(this, R.style.TextAppearance_Cypherpunk_Yellow), 0, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        binding.signUpButton.setText(sb);
//        binding.signUpButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onReconnectButtonClick() {
        ViewGroup.LayoutParams layoutParams = binding.regionContainer.getLayoutParams();
        if (layoutParams.height == getBottomContainerMaximumHeight()) {
            Animation animation = new HeightAnimation(getBottomContainerMinimumHeight(), binding.regionContainer);
            regionFragment.toggleAllowIcon(false);
            animation.setDuration(300);
            binding.regionContainer.startAnimation(animation);
        }
        startVpn();
    }

    @Override
    public void onNoReconnectButtonClick() {
        ViewGroup.LayoutParams layoutParams = binding.regionContainer.getLayoutParams();
        if (layoutParams.height == getBottomContainerMaximumHeight()) {
            Animation animation = new HeightAnimation(getBottomContainerMinimumHeight(), binding.regionContainer);
            regionFragment.toggleAllowIcon(false);
            animation.setDuration(300);
            binding.regionContainer.startAnimation(animation);
        }
        stopVpn();
    }

    @Override
    public void onConnectDialogButtonClick() {
        startVpn();
    }

    @Override
    public void toggleBottomSheetState() {
        ViewGroup.LayoutParams layoutParams = binding.regionContainer.getLayoutParams();
        HeightAnimation animation;
        if (layoutParams.height == getBottomContainerMaximumHeight()) {
            animation = new HeightAnimation(getBottomContainerMinimumHeight(), binding.regionContainer);
            regionFragment.toggleAllowIcon(false);
        } else {
            animation = new HeightAnimation(getBottomContainerMaximumHeight(), binding.regionContainer);
            regionFragment.toggleAllowIcon(true);
        }
        animation.setDuration(300);
        binding.regionContainer.startAnimation(animation);
    }

    private int getStatusBarHeight() {
        final Rect rect = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rect);
        return rect.top;
    }

    private int getBottomContainerMaximumHeight() {
        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
        int toolbarHeight = binding.toolbar.getHeight();
        return dm.heightPixels - toolbarHeight - getStatusBarHeight();
    }

    private int getBottomContainerMinimumHeight() {
        int[] position = new int[2];
        binding.connectionStatus.getLocationOnScreen(position);
        int connectionStatusPosition = position[1] + binding.connectionStatus.getHeight();
        int marginTop = getResources().getDimensionPixelSize(R.dimen.bottom_sheet_margin_top);
        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
        return dm.heightPixels - (connectionStatusPosition + marginTop);
    }
}
