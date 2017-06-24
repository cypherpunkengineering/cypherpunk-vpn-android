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

import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.datasource.vpn.VpnServer;
import com.cypherpunk.privacy.domain.model.AccountSetting;
import com.cypherpunk.privacy.domain.model.VpnSetting;
import com.cypherpunk.privacy.domain.repository.VpnServerRepository;
import com.cypherpunk.privacy.ui.common.FlagView;
import com.cypherpunk.privacy.vpn.CypherpunkVPN;
import com.cypherpunk.privacy.vpn.CypherpunkVpnStatus;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import de.blinkt.openvpn.core.VpnStatus;

/**
 * fragment for connection handling
 */
public class ConnectionFragment extends Fragment implements VpnStatus.StateListener {

    private static final int REQUEST_CODE_START_VPN = 0;

    public interface ConnectionFragmentListener {
        void onRegionChangeButtonClicked();
    }

    @Nullable
    private ConnectionFragmentListener listener;

    private enum Status {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        DISCONNECTING,
        UNKNOWN
    }

    @NonNull
    private Status connectionStatus = Status.UNKNOWN;

    @Inject
    VpnSetting vpnSetting;

    @Inject
    AccountSetting accountSetting;

    @Inject
    VpnServerRepository vpnServerRepository;

    @BindView(R.id.status)
    TextView statusView;

    @BindView(R.id.region_name)
    TextView regionNameView;

    @BindView(R.id.region_flag)
    FlagView regionFlagView;

    private Unbinder unbinder;

    private final CypherpunkVpnStatus status = CypherpunkVpnStatus.getInstance();
    private final CypherpunkVPN cypherpunkVPN = CypherpunkVPN.getInstance();

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_connection, container, false);
        unbinder = ButterKnife.bind(this, view);
        handler = new Handler();
        VpnStatus.addStateListener(this);
        return view;
    }

    @Override
    public void onDestroyView() {
        handler = null;
        unbinder.unbind();
        VpnStatus.removeStateListener(this);
        super.onDestroyView();
    }

    @OnClick(R.id.region_container)
    void onRegionContainerClicked() {
        if (listener != null) {
            listener.onRegionChangeButtonClicked();
        }
    }

    public void setVpnServer(@NonNull VpnServer vpnServer) {
        if (regionNameView != null) {
            regionNameView.setText(vpnServer.name());
            regionFlagView.setCountry(vpnServer.country());
        }
    }

    @OnClick(R.id.connection_button)
    void onConnectionButtonClicked() {
        switch (connectionStatus) {
            case DISCONNECTED:
            case DISCONNECTING:
                tryConnectIfNeeded();
                break;
            case CONNECTED:
            case CONNECTING:
            default:
                if (status.isDisconnected()) {
                    onDisconnected();
                } else {
                    tryDisconnect();
                }
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
        connectionStatus = Status.CONNECTING;
        statusView.setText(R.string.status_connecting);
        cypherpunkVPN.start(getContext().getApplicationContext(), getContext(),
                vpnSetting, accountSetting, vpnServerRepository);
    }

    private void tryDisconnect() {
        connectionStatus = Status.DISCONNECTING;
        statusView.setText(R.string.status_disconnecting);
        cypherpunkVPN.stop(vpnSetting);
    }

    private void onConnected() {
        connectionStatus = Status.CONNECTED;
        statusView.setText(R.string.status_connected);
    }

    private void onDisconnected() {
        connectionStatus = Status.DISCONNECTED;
        statusView.setText(R.string.status_disconnected);
    }

    @Override
    public void updateState(String state, String logmessage, int localizedResId, final VpnStatus.ConnectionStatus level) {
        if (handler != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    switch (level) {
                        case LEVEL_CONNECTED:
                            onConnected();
                            break;
                        case LEVEL_NOT_CONNECTED:
                            onDisconnected();
                            break;
                    }
                }
            });
        }
    }
}
