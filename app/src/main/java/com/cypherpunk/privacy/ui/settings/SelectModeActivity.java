package com.cypherpunk.privacy.ui.settings;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RadioGroup;

import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.databinding.ActivitySelectModeBinding;
import com.cypherpunk.privacy.ui.main.MainActivity;


/**
 * (Unused)
 */
public class SelectModeActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {

    private ActivitySelectModeBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_select_mode);
        binding.radioGroup.setOnCheckedChangeListener(this);
        binding.maxSpeedRadioButton.setChecked(true);

        binding.okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SelectModeActivity.this, MainActivity.class);
                TaskStackBuilder builder = TaskStackBuilder.create(SelectModeActivity.this);
                builder.addNextIntent(intent);
                builder.startActivities();
            }
        });

        binding.skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SelectModeActivity.this, MainActivity.class);
                TaskStackBuilder builder = TaskStackBuilder.create(SelectModeActivity.this);
                builder.addNextIntent(intent);
                builder.startActivities();
            }
        });
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int id) {
        int selectedModeName = R.string.settings_mode_max_speed;
        switch (id) {
            case R.id.max_privacy_radio_button:
                selectedModeName = R.string.settings_mode_max_privacy;
                break;
            case R.id.max_speed_radio_button:
                selectedModeName = R.string.settings_mode_max_speed;
                break;
            case R.id.max_freedom_radio_button:
                selectedModeName = R.string.settings_mode_max_freedom;
                break;
        }
        binding.selectedModeTitle.setText(selectedModeName);
    }
}
