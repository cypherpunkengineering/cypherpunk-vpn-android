package com.cypherpunk.privacy.ui.vpn;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.datasource.vpn.VpnServer;
import com.cypherpunk.privacy.domain.model.AccountSetting;
import com.cypherpunk.privacy.domain.repository.VpnServerRepository;
import com.cypherpunk.privacy.ui.common.FlagView;
import com.cypherpunk.privacy.ui.main.ConnectionView;
import com.cypherpunk.privacy.vpn.VpnManager;
import com.cypherpunk.privacy.vpn.VpnStatusHolder;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import de.blinkt.openvpn.core.VpnStatus;

/**
 * fragment for connection handling
 */
public class ConnectionFragment extends Fragment implements VpnStatusHolder.StateListener {

    private static final int REQUEST_CODE_START_VPN = 0;

    public interface ConnectionFragmentListener {
        void onRegionChangeButtonClicked();
    }

    @Nullable
    private ConnectionFragmentListener listener;

    @Inject
    AccountSetting accountSetting;

    @Inject
    VpnServerRepository vpnServerRepository;

    @Inject
    VpnStatusHolder vpnStatusHolder;

    @Inject
    VpnManager vpnManager;

    private Unbinder unbinder;

    @BindView(R.id.connection_button)
    ConnectionView connectionButton;

    @BindView(R.id.status)
    TextView statusView;

    @BindView(R.id.region_name)
    TextView regionNameView;

    @BindView(R.id.region_flag)
    FlagView regionFlagView;

    private VpnServer vpnServer;
    private Handler handler;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ConnectionFragmentListener) {
            listener = (ConnectionFragmentListener) context;
        }
    }

    @Override
    public void onDetach() {
        listener = null;
        super.onDetach();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CypherpunkApplication.instance.getAppComponent().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_connection, container, false);
        unbinder = ButterKnife.bind(this, view);
        handler = new Handler();
        updateState(vpnStatusHolder.status());
        vpnStatusHolder.addListener(this);
        return view;
    }

    @Override
    public void onDestroyView() {
        handler = null;
        unbinder.unbind();
        vpnStatusHolder.removeListener(this);
        super.onDestroyView();
    }

    @OnClick(R.id.region_container)
    void onRegionContainerClicked() {
        if (listener != null) {
            listener.onRegionChangeButtonClicked();
        }
    }

    public void setVpnServer(@NonNull VpnServer vpnServer) {
        this.vpnServer = vpnServer;
        if (regionNameView != null) {
            regionNameView.setText(vpnServer.name());
            regionFlagView.setCountry(vpnServer.country());
        }
    }

    @OnClick(R.id.connection_button)
    void onConnectionButtonClicked() {
        if (connectionButton.isConnectedOrConnecting()) {
            if (vpnStatusHolder.isDisconnected()) {
                onDisconnected();
            } else {
                tryDisconnect();
            }
        } else {
            tryConnectIfNeeded();
        }
    }

    public void tryConnectIfNeeded() {
        final Intent intent = VpnService.prepare(getContext());
        if (intent != null) {
            startActivityForResult(intent, REQUEST_CODE_START_VPN);
        } else {
            tryConnect();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_START_VPN && resultCode == Activity.RESULT_OK) {
            tryConnect();
        }
    }

    private void tryConnect() {
        if (vpnServer == null) {
            return;
        }
        if (connectionButton.tryConnect()) {
            statusView.setText(R.string.status_connecting);
            vpnManager.start(getContext(), vpnServer);
        }
    }

    private void tryDisconnect() {
        if (connectionButton.tryDisconnect()) {
            statusView.setText(R.string.status_disconnecting);
            vpnManager.stop();
        }
    }

    private void onConnected() {
        connectionButton.setConnected();
        statusView.setText(R.string.status_connected);
    }

    private void onDisconnected() {
        connectionButton.setDisconnected();
        statusView.setText(R.string.status_disconnected);
    }

    private void updateState(@NonNull VpnStatus.ConnectionStatus status) {
        switch (status) {
            case LEVEL_CONNECTED:
                onConnected();
                break;
            case LEVEL_NOT_CONNECTED:
                onDisconnected();
                break;
        }
    }

    @Override
    public void onStateChanged(@NonNull final VpnStatus.ConnectionStatus status) {
        if (handler != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    updateState(status);
                }
            });
        }
    }
}
