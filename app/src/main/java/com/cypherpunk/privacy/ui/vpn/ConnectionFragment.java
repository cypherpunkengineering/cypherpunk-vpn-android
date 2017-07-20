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
import com.cypherpunk.privacy.datasource.vpn.InternetKillSwitch;
import com.cypherpunk.privacy.datasource.vpn.VpnServer;
import com.cypherpunk.privacy.domain.model.AccountSetting;
import com.cypherpunk.privacy.domain.model.VpnSetting;
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
    private static final String KEY_STATUS = "status";

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

    @Inject
    VpnSetting vpnSetting;

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

    private enum Status {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        DISCONNECTING
    }

    @NonNull
    private Status status = Status.DISCONNECTED;

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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Status newStatus;
        if (savedInstanceState != null) {
            newStatus = (Status) savedInstanceState.getSerializable(KEY_STATUS);
        } else {
            newStatus = convert(vpnStatusHolder.status());
        }
        onNewStatus(newStatus);
    }

    @Override
    public void onStart() {
        super.onStart();
        // update text for kill switch
        update();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_STATUS, status);
    }

    @OnClick(R.id.region_container)
    void onRegionContainerClicked() {
        if (listener != null) {
            listener.onRegionChangeButtonClicked();
        }
    }

    public void setVpnServer(@NonNull VpnServer vpnServer, boolean isCypherPlay) {
        this.vpnServer = vpnServer;
        if (regionNameView != null) {
            if (isCypherPlay) {
                regionNameView.setText(R.string.region_cypher_play);
                regionFlagView.setImageResource(R.drawable.ic_cypher_play);
            } else {
                regionNameView.setText(vpnServer.name());
                regionFlagView.setCountry(vpnServer.country());
            }
        }
    }

    /**
     * check user is active or not.
     * if user is not active, disabled connection switch and kill current connection.
     */
    public void update() {
        final boolean isActive = accountSetting.isActive();
        connectionButton.setEnabled(isActive);
        connectionButton.setAlpha(isActive ? 1f : 0.3f);
        if (!isActive) {
            statusView.setText(R.string.status_account_expired);
            switch (status) {
                case CONNECTED:
                case CONNECTING:
                    // try disconnect
                    if (vpnStatusHolder.isDisconnected()) {
                        // already disconnected
                        onNewStatus(Status.DISCONNECTED);
                    } else {
                        onNewStatus(Status.DISCONNECTING);
                        vpnManager.stop();
                    }
                    break;
            }
        } else {
            if (status == Status.DISCONNECTED) {
                if (vpnSetting.internetKillSwitch() == InternetKillSwitch.ALWAYS_ON) {
                    statusView.setText(R.string.status_kill_switch_active);
                } else {
                    statusView.setText(R.string.status_disconnected);
                }
            }
        }
    }

    @OnClick(R.id.connection_button)
    void onConnectionButtonClicked() {
        switch (status) {
            case CONNECTED:
            case CONNECTING:
                // try disconnect
                if (vpnStatusHolder.isDisconnected()) {
                    // already disconnected
                    onNewStatus(Status.DISCONNECTED);
                } else {
                    onNewStatus(Status.DISCONNECTING);
                    vpnManager.stop();
                }
                break;
            case DISCONNECTED:
            case DISCONNECTING:
                // try connect
                tryConnectIfNeeded();
                break;
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
        if (vpnServer != null) {
            onNewStatus(Status.CONNECTING);
            vpnManager.start(getContext(), vpnServer);
        }
    }

    private void onNewStatus(@Nullable Status newStatus) {
        if (newStatus != null && newStatus != status) {
            status = newStatus;
            switch (status) {
                case CONNECTED:
                    connectionButton.setConnected();
                    statusView.setText(R.string.status_connected);
                    break;

                case CONNECTING:
                    connectionButton.setConnecting();
                    statusView.setText(R.string.status_connecting);
                    break;

                case DISCONNECTED:
                    connectionButton.setDisconnected();
                    if (vpnSetting.internetKillSwitch() == InternetKillSwitch.ALWAYS_ON) {
                        statusView.setText(R.string.status_kill_switch_active);
                    } else {
                        statusView.setText(R.string.status_disconnected);
                    }
                    break;

                case DISCONNECTING:
                    connectionButton.setDisconnecting();
                    statusView.setText(R.string.status_disconnecting);
                    break;
            }
        }
        if (!connectionButton.isEnabled()) {
            statusView.setText(R.string.status_account_expired);
        }
    }

    @Override
    public void onStateChanged(@NonNull final VpnStatus.ConnectionStatus connectionStatus) {
        if (handler != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onNewStatus(convert(connectionStatus));
                }
            });
        }
    }

    @Nullable
    private static Status convert(@NonNull VpnStatus.ConnectionStatus connectionStatus) {
        switch (connectionStatus) {
            case LEVEL_CONNECTED:
                return Status.CONNECTED;
            case LEVEL_NOT_CONNECTED:
                return Status.DISCONNECTED;
        }
        return null;
    }
}
