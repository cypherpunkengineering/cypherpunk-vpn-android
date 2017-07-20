package com.cypherpunk.privacy.ui.main;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.datasource.vpn.VpnServer;
import com.cypherpunk.privacy.domain.model.AccountSetting;
import com.cypherpunk.privacy.ui.account.AccountSettingsFragment;
import com.cypherpunk.privacy.ui.region.RegionFragment;
import com.cypherpunk.privacy.ui.settings.SettingsFragment;
import com.cypherpunk.privacy.ui.startup.IdentifyEmailActivity;
import com.cypherpunk.privacy.ui.startup.PendingActivity;
import com.cypherpunk.privacy.ui.vpn.ConnectionFragment;
import com.cypherpunk.privacy.vpn.VpnStatusHolder;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;

public class MainActivity extends AppCompatActivity implements
        SettingsFragment.SettingsFragmentListener,
        RegionFragment.RegionFragmentListener,
        ConnectionFragment.ConnectionFragmentListener,
        AccountSettingsFragment.AccountSettingsFragmentListener {

    @Inject
    AccountSetting accountSetting;

    @Inject
    VpnStatusHolder vpnStatusHolder;

    @NonNull
    private Disposable disposable = Disposables.empty();

    private ConnectionFragment connectionFragment;
    private RegionFragment regionFragment;

    @BindView(R.id.map)
    WorldMapView mapView;

    private SlidingMenu slidingMenu;
    private View reconnectPanel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CypherpunkApplication.instance.getAppComponent().inject(this);

        if (!accountSetting.isSignedIn()) {
            TaskStackBuilder.create(this)
                    .addNextIntent(IdentifyEmailActivity.createIntent(this))
                    .startActivities();
            return;
        }

        if (accountSetting.isPending()) {
            TaskStackBuilder.create(this)
                    .addNextIntent(PendingActivity.createIntent(this))
                    .startActivities();
            return;
        }

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (!getResources().getBoolean(R.bool.is_tablet)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        final FragmentManager manager = getSupportFragmentManager();
        regionFragment = (RegionFragment) manager.findFragmentById(R.id.fragment_region);
        connectionFragment = (ConnectionFragment) manager.findFragmentById(R.id.fragment_connection);

        if (savedInstanceState == null) {
            manager.beginTransaction()
                    .hide(regionFragment)
                    .commit();
        }

        slidingMenu = new SlidingMenu(this);
        slidingMenu.setMode(SlidingMenu.LEFT_RIGHT);
        slidingMenu.setMenu(R.layout.frame_main_left);
        slidingMenu.setSecondaryMenu(R.layout.frame_main_right);
        slidingMenu.setShadowDrawable(R.drawable.slide_menu_shadow_left);
        slidingMenu.setSecondaryShadowDrawable(R.drawable.slide_menu_shadow_right);
        slidingMenu.setShadowWidthRes(R.dimen.slide_menu_shadow_width);
        slidingMenu.setBehindWidthRes(R.dimen.slide_menu_width);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        slidingMenu.setFadeDegree(0.35f);
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT, false);

        reconnectPanel = LayoutInflater.from(this).inflate(R.layout.panel_reconnect, slidingMenu, false);
        slidingMenu.addView(reconnectPanel);
        reconnectPanel.setVisibility(View.GONE);
        reconnectPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setVisibility(View.GONE);
                connectionFragment.tryConnectIfNeeded();
            }
        });
    }

    @Override
    public void onDestroy() {
        disposable.dispose();
        super.onDestroy();
    }

    @Optional
    @OnClick(R.id.account_menu_button)
    void onAccountMenuButtonClicked() {
        slidingMenu.showMenu();
    }

    @Optional
    @OnClick(R.id.setting_menu_button)
    void onSettingMenuButtonClicked() {
        slidingMenu.showSecondaryMenu();
    }

    @Override
    public void onBackPressed() {
        if (slidingMenu != null) {
            if (slidingMenu.isMenuShowing()) {
                slidingMenu.showContent();
                return;
            }
            if (slidingMenu.isSecondaryMenuShowing()) {
                slidingMenu.showContent();
                return;
            }
        }

        if (!connectionFragment.isVisible()) {
            hideRegions();
            mapView.setOffsetMode(false);
            return;
        }

        super.onBackPressed();
    }

    @Override
    public void onVpnServerSelected(@NonNull VpnServer vpnServer, boolean isCypherPlay,
                                    boolean isUserSelected) {
        if (isUserSelected) {
            hideRegions();
        }
        reconnectPanel.setVisibility(View.GONE);
        mapView.setVpnServer(vpnServer);
        connectionFragment.setVpnServer(vpnServer, isCypherPlay);
        if (isUserSelected) {
            connectionFragment.tryConnectIfNeeded();
        }
    }

    @Override
    public void onVpnServerListChanged(@NonNull List<VpnServer> vpnServerList) {
        mapView.setVpnServers(vpnServerList);
    }

    @Override
    public void onRegionChangeButtonClicked() {
        getSupportFragmentManager().beginTransaction()
                .show(regionFragment)
                .hide(connectionFragment)
                // TODO: use custom transition
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();

        mapView.setOffsetMode(true);
    }

    private void hideRegions() {
        getSupportFragmentManager().beginTransaction()
                .show(connectionFragment)
                .hide(regionFragment)
                // TODO: use custom transition
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    @Override
    public void askReconnectingIfNeeded() {
        if (vpnStatusHolder.isConnected()) {
            reconnectPanel.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAccountChecked() {
        connectionFragment.update();
        regionFragment.syncServerList();
    }
}
