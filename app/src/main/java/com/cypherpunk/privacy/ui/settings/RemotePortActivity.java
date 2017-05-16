package com.cypherpunk.privacy.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Checkable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.model.CypherpunkSetting;
import com.cypherpunk.privacy.model.CypherpunkSetting.RemotePortCategory;
import com.cypherpunk.privacy.model.CypherpunkSetting.RemotePortPort;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

public class RemotePortActivity extends AppCompatActivity {

    @NonNull
    public static Intent createIntent(@NonNull Context context) {
        return new Intent(context, RemotePortActivity.class);
    }

    private final List<Checkable> checkableList = new ArrayList<>();
    private CypherpunkSetting cypherpunkSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_port);
        ButterKnife.bind(this);

        if (!getResources().getBoolean(R.bool.is_tablet)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        final Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);

        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.close_vector);
        }

        cypherpunkSetting = new CypherpunkSetting();

        final LinearLayout container = ButterKnife.findById(this, R.id.container);
        final LayoutInflater inflater = LayoutInflater.from(this);

        final CypherpunkSetting.RemotePort remotePort = cypherpunkSetting.remotePort();

        for (final RemotePortCategory category : RemotePortCategory.values()) {
            final LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.remote_port_category,
                    container, false);
            container.addView(ll);

            final TextView textView = ButterKnife.findById(ll, R.id.category_name);
            textView.setText(category.name());

            for (final RemotePortPort port : RemotePortPort.values()) {
                final View view = inflater.inflate(R.layout.remote_port_item, ll, false);
                ll.addView(view);

                final Checkable checkable = ButterKnife.findById(view, R.id.checkable);
                checkableList.add(checkable);

                final TextView portView = ButterKnife.findById(view, R.id.text);
                portView.setText(String.valueOf(port.value()));

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!checkable.isChecked()) {
                            for (Checkable c : checkableList) {
                                c.setChecked(false);
                            }
                            checkable.setChecked(true);

                            update(new CypherpunkSetting.RemotePort(category, port));
                        }
                    }
                });

                ll.addView(inflater.inflate(R.layout.divider, ll, false));

                if (remotePort.category == category && remotePort.port == port) {
                    checkable.setChecked(true);
                }
            }
        }
    }

    private void update(@NonNull CypherpunkSetting.RemotePort remotePort) {
        setResult(RESULT_OK);
        cypherpunkSetting.updateRemotePort(remotePort);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
