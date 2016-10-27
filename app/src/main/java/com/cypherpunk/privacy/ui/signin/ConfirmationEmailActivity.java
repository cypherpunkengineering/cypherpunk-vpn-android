package com.cypherpunk.privacy.ui.signin;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.databinding.ActivityConrirmationEmailBinding;


public class ConfirmationEmailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityConrirmationEmailBinding binding =
                DataBindingUtil.setContentView(this, R.layout.activity_conrirmation_email);

        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConfirmationEmailActivity.this, IdentifyEmailActivity.class);
                TaskStackBuilder builder = TaskStackBuilder.create(ConfirmationEmailActivity.this);
                builder.addNextIntent(intent);
                builder.startActivities();
            }
        });

        binding.resendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        binding.resendButton.setPaintFlags(
                binding.resendButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
