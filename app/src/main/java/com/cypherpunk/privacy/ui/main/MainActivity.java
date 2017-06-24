package com.cypherpunk.privacy.ui.main;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.domain.model.AccountSetting;
import com.cypherpunk.privacy.domain.model.VpnSetting;
import com.cypherpunk.privacy.domain.repository.VpnServerRepository;
import com.cypherpunk.privacy.ui.account.AccountSettingsFragment;
import com.cypherpunk.privacy.ui.common.BinaryTextureView;
import com.cypherpunk.privacy.ui.common.ConnectionStatusView;
import com.cypherpunk.privacy.ui.common.VpnFlatButton;
import com.cypherpunk.privacy.ui.region.RegionFragment;
import com.cypherpunk.privacy.ui.settings.AskReconnectDialogFragment;
import com.cypherpunk.privacy.ui.settings.SettingsFragment;
import com.cypherpunk.privacy.ui.startup.IdentifyEmailActivity;
import com.cypherpunk.privacy.vpn.CypherpunkVPN;
import com.cypherpunk.privacy.vpn.CypherpunkVpnStatus;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.blinkt.openvpn.core.VpnStatus;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements
        AskReconnectDialogFragment.ConnectDialogListener,
        RegionFragment.RegionFragmentListener {

    private static final int REQUEST_VPN_START = 0;

    private CypherpunkVpnStatus status;
    private RegionFragment regionFragment;
    private SlidingMenu slidingMenu;

    @Inject
    VpnSetting vpnSetting;

    @Inject
    AccountSetting accountSetting;

    @Inject
    VpnServerRepository vpnServerRepository;

    @BindView(R.id.container)
    View container;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.action_menu_left)
    ActionMenuView leftActionMenuView;

    @BindView(R.id.binary_view)
    BinaryTextureView binaryView;

    @BindView(R.id.connection_status)
    ConnectionStatusView connectionStatusView;

    @BindView(R.id.region_container)
    View regionContainer;

    @BindView(R.id.connection_button)
    VpnFlatButton connectionButton;

    @BindView(R.id.connecting_cancel_button)
    TextView connectingCancelButton;

    @Nullable
    @BindView(R.id.national_flag)
    ImageView flagView;

    @Nullable
    @BindView(R.id.region_name)
    TextView nameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        CypherpunkApplication.instance.getAppComponent().inject(this);

        if (!accountSetting.isSignedIn()) {
            TaskStackBuilder.create(this)
                    .addNextIntent(IdentifyEmailActivity.createIntent(this))
                    .startActivities();
            finish();
        }

        if (!getResources().getBoolean(R.bool.is_tablet)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            findViewById(android.R.id.content).setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        status = CypherpunkVpnStatus.getInstance();

        setSupportActionBar(toolbar);

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
        final TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        final String operatorName = telephonyManager.getSimOperatorName();
        if (TextUtils.isEmpty(operatorName)) {
            String[] text = {Build.BRAND.toUpperCase(), Build.MODEL.toUpperCase()};
            binaryView.setText(text);
        } else {
            String[] text = {Build.BRAND.toUpperCase(), Build.MODEL.toUpperCase(), operatorName};
            binaryView.setText(text);
        }

        // showSignUpButton();

        connectionButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (CypherpunkVpnStatus.getInstance().isDisconnected()) {
                    startVpn();
                } else {
                    stopVpn();
                    regionFragment.refreshServerList();
                }
            }
        });
        connectingCancelButton.setPaintFlags(
                connectingCancelButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        connectingCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // cancel
                stopVpn();
            }
        });
        leftActionMenuView.setOnMenuItemClickListener(new ActionMenuView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelected(item);
            }
        });
        VpnStatus.addStateListener(new VpnStatus.StateListener() {
            @Override
            public void updateState(String state, String logmessage, int localizedResId, VpnStatus.ConnectionStatus level) {
                switch (level) {
                    case LEVEL_CONNECTED:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binaryView.setState(BinaryTextureView.CONNECTED);
                                connectionStatusView.setStatus(ConnectionStatusView.CONNECTED);
                                connectionButton.setStatus(VpnFlatButton.CONNECTED);
                                connectingCancelButton.setVisibility(View.GONE);
                            }
                        });
                        break;
                    case LEVEL_NOT_CONNECTED:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binaryView.setState(BinaryTextureView.DISCONNECTED);
                                connectionStatusView.setStatus(ConnectionStatusView.DISCONNECTED);
                                connectionButton.setStatus(VpnFlatButton.DISCONNECTED);
                                connectingCancelButton.setVisibility(View.GONE);
                            }
                        });
                        break;
                }
            }
        });

        FragmentTransaction fm = getSupportFragmentManager().beginTransaction();
        regionFragment = RegionFragment.newInstance();
        fm.add(R.id.region_container, regionFragment);
        fm.commit();

        if (getResources().getBoolean(R.bool.is_tablet)) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.right_drawer, new TabletDrawerFragment()).commit();
        } else {
            final ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeAsUpIndicator(R.drawable.ic_account_vector);
            }

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
            ViewGroup.LayoutParams layoutParams = regionContainer.getLayoutParams();
            layoutParams.height = getBottomContainerMinimumHeight();
            regionContainer.requestLayout();
        }

        int statusBarHeight = getStatusBarHeight();
        container.setPadding(0, statusBarHeight, 0, 0);
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
        binaryView.startAnimation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        binaryView.stopAnimation();
    }

    private void startVpn() {
        Timber.d("startVpn()");
        Intent intent = VpnService.prepare(MainActivity.this);
        if (intent != null) {
            startActivityForResult(intent, REQUEST_VPN_START);
        } else {
            binaryView.setState(BinaryTextureView.CONNECTING);
            connectionStatusView.setStatus(ConnectionStatusView.CONNECTING);
            connectionButton.setStatus(VpnFlatButton.CONNECTING);
            connectingCancelButton.setVisibility(View.VISIBLE);
            CypherpunkVPN.getInstance().start(getApplicationContext(), getBaseContext(),
                    vpnSetting, accountSetting, vpnServerRepository);
        }
    }

    private void stopVpn() {
        Timber.d("stopVpn()");
        if (status.isDisconnected()) {
            return;
        }
        CypherpunkVPN.getInstance().stop(vpnSetting);
    }

    @Override
    public void onConnectDialogButtonClick() {
        startVpn();
    }

    @Override
    public void toggleBottomSheetState() {
        if (!getResources().getBoolean(R.bool.is_tablet)) {
            ViewGroup.LayoutParams layoutParams = regionContainer.getLayoutParams();
            HeightAnimation animation;
            if (layoutParams.height == getBottomContainerMaximumHeight()) {
                animation = new HeightAnimation(getBottomContainerMinimumHeight(), regionContainer);
            } else {
                animation = new HeightAnimation(getBottomContainerMaximumHeight(), regionContainer);
            }
            animation.setDuration(300);
            regionContainer.startAnimation(animation);
        }
    }

    @Override
    public void onSelectedRegionChanged(@NonNull String regionName, @DrawableRes int nationalFlagResId, boolean connectNow) {
        if (getResources().getBoolean(R.bool.is_tablet)) {
            nameView.setText(regionName);
            flagView.setImageResource(nationalFlagResId);
        }
        ViewGroup.LayoutParams layoutParams = regionContainer.getLayoutParams();
        if (layoutParams.height == getBottomContainerMaximumHeight()) {
            Animation animation = new HeightAnimation(getBottomContainerMinimumHeight(), regionContainer);
            animation.setDuration(300);
            regionContainer.startAnimation(animation);
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
        int toolbarHeight = toolbar.getHeight();
        return dm.heightPixels - toolbarHeight - getStatusBarHeight();
    }

    private int getBottomContainerMinimumHeight() {
        int[] position = new int[2];
        connectionStatusView.getLocationOnScreen(position);
        int connectionStatusPosition = position[1] + connectionStatusView.getHeight();
        int marginTop = getResources().getDimensionPixelSize(R.dimen.bottom_sheet_margin_top);
        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
        return dm.heightPixels - (connectionStatusPosition + marginTop);
    }
}
