package com.cypherpunk.privacy.ui.main;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionMenuView;
import android.telephony.TelephonyManager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.CompoundButton;

import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.model.UserSetting;
import com.cypherpunk.privacy.ui.account.AccountSettingsFragment;
import com.cypherpunk.privacy.ui.region.RegionFragment;
import com.cypherpunk.privacy.ui.settings.SettingConnectDialogFragment;
import com.cypherpunk.privacy.ui.settings.SettingsFragment;
import com.cypherpunk.privacy.ui.startup.IdentifyEmailActivity;
import com.cypherpunk.privacy.vpn.CypherpunkVPN;
import com.cypherpunk.privacy.vpn.CypherpunkVpnStatus;
import com.cypherpunk.privacy.widget.BinaryTextureView;
import com.cypherpunk.privacy.widget.ConnectionStatusView;
import com.cypherpunk.privacy.widget.VpnFlatButton;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import de.blinkt.openvpn.core.VpnStatus;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity
        implements VpnStatus.StateListener,
        SettingConnectDialogFragment.ConnectDialogListener,
        RegionFragment.RegionFragmentListener {

    private static final int REQUEST_VPN_START = 0;

    private com.cypherpunk.privacy.databinding.ActivityMainBinding binding;
    private CypherpunkVpnStatus status;
    private RegionFragment regionFragment;
    private SlidingMenu slidingMenu;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            findViewById(android.R.id.content).setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        if (!UserSetting.instance().isSignedIn()) {
            TaskStackBuilder.create(this)
                    .addNextIntent(IdentifyEmailActivity.createIntent(this))
                    .startActivities();
            finish();
        }

        if (!getResources().getBoolean(R.bool.is_tablet)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        status = CypherpunkVpnStatus.getInstance();
        setSupportActionBar(binding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowTitleEnabled(false);

        slidingMenu = new SlidingMenu(this);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        slidingMenu.setShadowWidthRes(R.dimen.slide_menu_shadow_width);
        slidingMenu.setShadowDrawable(R.drawable.slide_menu_shadow);
        slidingMenu.setMode(SlidingMenu.LEFT_RIGHT);
        slidingMenu.setFadeDegree(0.35f);
        slidingMenu.setBehindWidthRes(R.dimen.slide_menu_width);
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT, true);
        slidingMenu.setSecondaryMenu(R.layout.frame_main_right);
        slidingMenu.setMenu(R.layout.frame_main_left);
        slidingMenu.setSecondaryShadowDrawable(R.drawable.slide_menu_shadow_right);

        // background
        String operatorName = getSimOperatorName();
        if (TextUtils.isEmpty(operatorName)) {
            String[] text = {Build.BRAND.toUpperCase(), Build.MODEL.toUpperCase()};
            binding.binaryView.setText(text);
        } else {
            String[] text = {Build.BRAND.toUpperCase(), Build.MODEL.toUpperCase(), operatorName};
            binding.binaryView.setText(text);
        }

        // showSignUpButton();

        binding.connectionButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                toggleVpn();
            }
        });
        binding.connectingCancelButton.setPaintFlags(
                binding.connectingCancelButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        binding.connectingCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // cancel
                stopVpn();
            }
        });
        binding.actionMenuLeft.setOnMenuItemClickListener(new ActionMenuView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelected(item);
            }
        });
        VpnStatus.addStateListener(this);

        FragmentTransaction fm = getSupportFragmentManager().beginTransaction();
        regionFragment = new RegionFragment();
        fm.add(R.id.region_container, regionFragment);
        fm.commit();

        if (getResources().getBoolean(R.bool.is_tablet)) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.right_drawer, new TabletDrawerFragment()).commit();
        } else {
            actionBar.setHomeButtonEnabled(true);
            binding.toolbar.setNavigationIcon(R.drawable.account_vector);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.left_drawer, new AccountSettingsFragment()).commit();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.right_drawer, new SettingsFragment()).commit();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!getResources().getBoolean(R.bool.is_tablet)) {
            ViewGroup.LayoutParams layoutParams = binding.regionContainer.getLayoutParams();
            layoutParams.height = getBottomContainerMinimumHeight();
            binding.regionContainer.requestLayout();
        }

        int statusBarHeight = getStatusBarHeight();
        binding.container.setPadding(0, statusBarHeight, 0, 0);
        slidingMenu.getMenu().setPadding(0, statusBarHeight, 0, 0);
        slidingMenu.getSecondaryMenu().setPadding(0, statusBarHeight, 0, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_right, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                slidingMenu.showMenu();
                break;
            case R.id.action_setting:
                slidingMenu.showSecondaryMenu();
                break;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (!getResources().getBoolean(R.bool.is_tablet) && slidingMenu.isMenuShowing()) {
            slidingMenu.showContent();
        } else if (slidingMenu.isSecondaryMenuShowing()) {
            slidingMenu.showContent();
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
    protected void onResume() {
        super.onResume();
        binding.binaryView.startAnimation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.binaryView.stopAnimation();
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

    private void startVpn() {
        Timber.d("startVpn()");
        Intent intent = VpnService.prepare(MainActivity.this);
        if (intent != null) {
            startActivityForResult(intent, REQUEST_VPN_START);
        } else {
            binding.binaryView.setState(BinaryTextureView.CONNECTING);
            binding.connectionStatus.setStatus(ConnectionStatusView.CONNECTING);
            binding.connectionButton.setStatus(VpnFlatButton.CONNECTING);
            binding.connectingCancelButton.setVisibility(View.VISIBLE);
            CypherpunkVPN.getInstance().start(getApplicationContext(), getBaseContext());
        }
    }

    private void stopVpn() {
        Timber.d("stopVpn()");
        if (status.isDisconnected()) {
            return;
        }
        CypherpunkVPN.getInstance().stop();
    }

    private void toggleVpn() {
        if (CypherpunkVpnStatus.getInstance().isDisconnected()) {
            startVpn();
        } else {
            stopVpn();
            regionFragment.refreshServerList();
        }
    }

    private void onVpnConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.binaryView.setState(BinaryTextureView.CONNECTED);
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
                binding.binaryView.setState(BinaryTextureView.DISCONNECTED);
                binding.connectionStatus.setStatus(ConnectionStatusView.DISCONNECTED);
                binding.connectionButton.setStatus(VpnFlatButton.DISCONNECTED);
                binding.connectingCancelButton.setVisibility(View.GONE);
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
    public void onConnectDialogButtonClick() {
        startVpn();
    }

    @Override
    public void toggleBottomSheetState() {
        if (!getResources().getBoolean(R.bool.is_tablet)) {
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
    }

    @Override
    public void onSelectedRegionChanged(@NonNull String regionName, @DrawableRes int nationalFlagResId, boolean connectNow) {
        if (getResources().getBoolean(R.bool.is_tablet)) {
            binding.regionName.setText(regionName);
            binding.nationalFlag.setImageResource(nationalFlagResId);
        }
        ViewGroup.LayoutParams layoutParams = binding.regionContainer.getLayoutParams();
        if (layoutParams.height == getBottomContainerMaximumHeight()) {
            Animation animation = new HeightAnimation(getBottomContainerMinimumHeight(), binding.regionContainer);
            regionFragment.toggleAllowIcon(false);
            animation.setDuration(300);
            binding.regionContainer.startAnimation(animation);
        }
        if (connectNow) {
            startVpn();
        }
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
