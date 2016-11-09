package com.cypherpunk.privacy.ui.account;

import android.content.pm.ActivityInfo;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.databinding.ActivityEditPasswordBinding;

public class EditPasswordActivity extends AppCompatActivity {

    private ActivityEditPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getResources().getBoolean(R.bool.is_tablet)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_password);

        setSupportActionBar(binding.toolbar.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            binding.toolbar.title.setText(R.string.title_activity_edit_password);
            actionBar.setHomeAsUpIndicator(R.drawable.close_vector);
        }

        binding.password.requestFocus();
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
                if (validatePassword()) {
                    updateEmail(binding.password.getText().toString());
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean validatePassword() {
        String newPassword = binding.password.getText().toString();
        String confirmPassword = binding.confirmEmail.getText().toString();

        if (TextUtils.isEmpty(newPassword)) {
            binding.password.setError(getString(R.string.error_field_required));
            return false;
        }

        if (TextUtils.isEmpty(newPassword)) {
            binding.password.setError(getString(R.string.error_field_required));
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
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
