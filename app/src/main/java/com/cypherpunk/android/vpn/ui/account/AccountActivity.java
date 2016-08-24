package com.cypherpunk.android.vpn.ui.account;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.data.api.UserManager;
import com.cypherpunk.android.vpn.databinding.ActivityAccountBinding;
import com.cypherpunk.android.vpn.ui.settings.UpgradePlanActivity;
import com.cypherpunk.android.vpn.ui.setup.IntroductionActivity;
import com.cypherpunk.android.vpn.vpn.CypherpunkVPN;

public class AccountActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityAccountBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_account);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        UserManager user = UserManager.getInstance(this);
        binding.mail.setText(user.getMailAddress());

        // TODO: set account grade
        binding.upgradeAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AccountActivity.this, UpgradePlanActivity.class
                ));
            }
        });
        binding.signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void signOut() {
        CypherpunkVPN.stop(getApplicationContext(), getBaseContext());
        UserManager manager = UserManager.getInstance(this);
        manager.clearUser();
        Intent intent = new Intent(this, IntroductionActivity.class);
        TaskStackBuilder builder = TaskStackBuilder.create(this);
        builder.addNextIntent(intent);
        builder.startActivities();
        finish();
    }
}
