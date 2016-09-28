package com.cypherpunk.android.vpn.ui.settings;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.databinding.ActivityEditEmailBinding;

public class EditEmailActivity extends AppCompatActivity {

    private ActivityEditEmailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_email);

        setSupportActionBar(binding.toolbar.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            binding.toolbar.title.setText(R.string.title_activity_edit_email);
        }

        binding.email.requestFocus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_done:
                if (validateEmail()) {
                    updateEmail(binding.email.getText().toString());
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private boolean validateEmail() {
        String newEmail = binding.email.getText().toString();
        String confirmEmail = binding.confirmEmail.getText().toString();

        if (TextUtils.isEmpty(newEmail)) {
            binding.email.setError(getString(R.string.error_field_required));
            return false;
        }

        if (TextUtils.isEmpty(newEmail)) {
            binding.email.setError(getString(R.string.error_field_required));
            return false;
        }

        if (!newEmail.equals(confirmEmail)) {
            binding.confirmEmail.setError(getString(R.string.edit_email_account_error_do_not_match));
            return false;
        }

        return true;
    }

    private void updateEmail(@NonNull String email) {
        // TODO: update email
        finish();
    }
}
