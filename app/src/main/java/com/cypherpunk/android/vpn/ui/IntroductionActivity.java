package com.cypherpunk.android.vpn.ui;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.databinding.ActivityIntroductionBinding;
import com.cypherpunk.android.vpn.ui.signin.SignInActivity;
import com.cypherpunk.android.vpn.ui.signin.SignUpActivity;


public class IntroductionActivity extends AppCompatActivity {

    private ActivityIntroductionBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_introduction);

        binding.createAccountButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(IntroductionActivity.this, SignUpActivity.class));
                    }
                });

        binding.signInButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(IntroductionActivity.this, SignInActivity.class));
                    }
                });
    }
}
