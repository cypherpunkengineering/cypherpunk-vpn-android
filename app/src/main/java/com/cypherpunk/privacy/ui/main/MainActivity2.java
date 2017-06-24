package com.cypherpunk.privacy.ui.main;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.ImageView;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.datasource.vpn.VpnServer;
import com.cypherpunk.privacy.domain.model.AccountSetting;
import com.cypherpunk.privacy.domain.model.VpnSetting;
import com.cypherpunk.privacy.domain.repository.VpnServerRepository;
import com.cypherpunk.privacy.ui.region.RegionFragment2;
import com.cypherpunk.privacy.ui.settings.AskReconnectDialogFragment;
import com.cypherpunk.privacy.ui.startup.IdentifyEmailActivity;
import com.cypherpunk.privacy.ui.vpn.ConnectionFragment;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity2 extends AppCompatActivity implements
        AskReconnectDialogFragment.ConnectDialogListener,
        RegionFragment2.RegionFragmentListener,
        ConnectionFragment.ConnectionFragmentListener {

    @Inject
    VpnSetting vpnSetting;

    @Inject
    AccountSetting accountSetting;

    @Inject
    VpnServerRepository vpnServerRepository;

    private ConnectionFragment connectionFragment;
    private RegionFragment2 regionFragment;

    @BindView(R.id.marker)
    ImageView markerView;

    private SlidingMenu slidingMenu;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
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

        final FragmentManager manager = getSupportFragmentManager();
        regionFragment = (RegionFragment2) manager.findFragmentById(R.id.fragment_region);
        connectionFragment = (ConnectionFragment) manager.findFragmentById(R.id.fragment_connection);
        manager.beginTransaction()
                .hide(regionFragment)
                .commit();

        slidingMenu = new SlidingMenu(this);
        slidingMenu.setMode(SlidingMenu.LEFT_RIGHT);
        slidingMenu.setMenu(R.layout.frame_main_left);
        slidingMenu.setSecondaryMenu(R.layout.frame_main_right);
        slidingMenu.setShadowDrawable(R.drawable.slide_menu_shadow);
        slidingMenu.setSecondaryShadowDrawable(R.drawable.slide_menu_shadow_right);
        slidingMenu.setShadowWidthRes(R.dimen.slide_menu_shadow_width);
        slidingMenu.setBehindWidthRes(R.dimen.slide_menu_width);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        slidingMenu.setFadeDegree(0.35f);
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT, true);

        VpnServer vpnServer = null;
        final String id = vpnSetting.regionId();
        if (!TextUtils.isEmpty(id)) {
            vpnServer = vpnServerRepository.find(id);
        }
        if (vpnServer == null) {
            vpnServer = vpnServerRepository.fastest();
        }
        if (vpnServer != null) {
            connectionFragment.setVpnServer(vpnServer);
        }
    }

    @OnClick(R.id.account_menu_button)
    void onAccountMenuButtonClicked() {
        slidingMenu.showMenu();
    }

    @OnClick(R.id.setting_menu_button)
    void onSettingMenuButtonClicked() {
        slidingMenu.showSecondaryMenu();
    }

    @Override
    public void onBackPressed() {
        if (!getResources().getBoolean(R.bool.is_tablet) && slidingMenu.isMenuShowing()) {
            slidingMenu.showContent();
        } else if (slidingMenu.isSecondaryMenuShowing()) {
            slidingMenu.showContent();
        } else if (!connectionFragment.isVisible()) {
            hideRegions();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onVpnServerSelected(@NonNull VpnServer vpnServer) {
        hideRegions();
        connectionFragment.setVpnServer(vpnServer);
        connectionFragment.tryConnectIfNeeded();
    }

    @Override
    public void onRegionChangeButtonClicked() {
        getSupportFragmentManager().beginTransaction()
                .show(regionFragment)
                .hide(connectionFragment)
                // TODO: use custom transition
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();

        markerView.setColorFilter(ContextCompat.getColor(this, R.color.marker_background));
    }

    private void hideRegions() {
        getSupportFragmentManager().beginTransaction()
                .show(connectionFragment)
                .hide(regionFragment)
                // TODO: use custom transition
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();

        markerView.setColorFilter(ContextCompat.getColor(this, R.color.marker_foreground));
    }

    // FIXME:
    @Override
    public void onConnectDialogButtonClick() {
        connectionFragment.tryConnectIfNeeded();
    }
}
