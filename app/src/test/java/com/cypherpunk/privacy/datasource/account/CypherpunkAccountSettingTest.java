package com.cypherpunk.privacy.datasource.account;

import android.content.Context;

import com.cypherpunk.privacy.BuildConfig;
import com.cypherpunk.privacy.CypherpunkTestApplication;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, application = CypherpunkTestApplication.class)
public class CypherpunkAccountSettingTest {

    @Test
    public void is_pending() throws Exception {
        final Context context = RuntimeEnvironment.application;
        final CypherpunkAccountSetting accountSetting = spy(new CypherpunkAccountSetting(context));

        // true when invitation
        doReturn(Account.Type.INVITATION).when(accountSetting).accountType();
        assertThat(accountSetting.isPending()).isTrue();

        // true when pending
        doReturn(Account.Type.PENDING).when(accountSetting).accountType();
        assertThat(accountSetting.isPending()).isTrue();

        // false when free
        doReturn(Account.Type.FREE).when(accountSetting).accountType();
        assertThat(accountSetting.isPending()).isFalse();

        // false when developer
        doReturn(Account.Type.DEVELOPER).when(accountSetting).accountType();
        assertThat(accountSetting.isPending()).isFalse();

        // false when organization
        doReturn(Account.Type.ORGANIZATION).when(accountSetting).accountType();
        assertThat(accountSetting.isPending()).isFalse();

        // false when premium
        doReturn(Account.Type.PREMIUM).when(accountSetting).accountType();
        assertThat(accountSetting.isPending()).isFalse();
    }
}
