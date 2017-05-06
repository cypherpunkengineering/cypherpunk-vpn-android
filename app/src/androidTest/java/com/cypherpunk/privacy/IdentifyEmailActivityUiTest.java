package com.cypherpunk.privacy;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.cypherpunk.privacy.dagger.AppComponent;
import com.cypherpunk.privacy.dagger.DaggerAppComponent;
import com.cypherpunk.privacy.dagger.RealmModule;
import com.cypherpunk.privacy.data.api.CypherpunkService;
import com.cypherpunk.privacy.data.api.json.EmailRequest;
import com.cypherpunk.privacy.ui.signin.IdentifyEmailActivity;
import com.cypherpunk.privacy.ui.signin.LoginActivity;
import com.cypherpunk.privacy.ui.signin.SignUpActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.adapter.rxjava.HttpException;
import rx.Single;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * {@link IdentifyEmailActivity} のテスト
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class IdentifyEmailActivityUiTest {

    @Rule
    public IntentsTestRule<IdentifyEmailActivity> activityRule = new IntentsTestRule<>(IdentifyEmailActivity.class, false, false);

    @Mock
    CypherpunkService cypherpunkService;

    @Before
    public void setp() throws Exception {
        MockitoAnnotations.initMocks(this);

        final CypherpunkApplication app = (CypherpunkApplication) InstrumentationRegistry
                .getInstrumentation()
                .getTargetContext()
                .getApplicationContext();

        final AppComponent component = DaggerAppComponent.builder()
                .clientModule(new MockClientModule(cypherpunkService))
                .realmModule(new RealmModule())
                .build();

        app.setAppComponent(component);
    }

    @Test
    public void validate_test() {
        // test
        final Intent intent = IdentifyEmailActivity.createIntent(InstrumentationRegistry.getContext());
        activityRule.launchActivity(intent);

        // フィールドが空の状態でボタンクリック
        onView(withId(R.id.submit_button)).perform(click());

        // エラーメッセージが表示されるかチェック
        onView(withText(R.string.error_field_required)).check(matches(isDisplayed()));

        // a と入力
        onView(withId(R.id.edit_text)).perform(typeText("a"));

        // エラーメッセージが表示されなくなったかチェック
        onView(withText(R.string.error_field_required)).check(doesNotExist());

        // フィールドが a の状態でボタンクリック
        onView(withId(R.id.submit_button)).perform(click());

        // エラーメッセージが表示されるかチェック
        onView(withText(R.string.error_invalid_email)).check(matches(isDisplayed()));
    }

    @Test
    public void success_test() {
        // setup
        final ResponseBody responseBody = mock(ResponseBody.class);
        doReturn(Single.just(responseBody)).when(cypherpunkService).identifyEmail(any(EmailRequest.class));

//        Instrumentation.ActivityMonitor monitor = new Instrumentation.ActivityMonitor(SignInActivity.class.getCanonicalName(), null, false);
//        InstrumentationRegistry.getInstrumentation().addMonitor(monitor);

        // test
        final Intent intent = IdentifyEmailActivity.createIntent(InstrumentationRegistry.getContext());
        activityRule.launchActivity(intent);

        // a@b.c と入力
        onView(withId(R.id.edit_text)).perform(typeText("a@b.c"));

        // ボタンクリック
        onView(withId(R.id.submit_button)).perform(click());

        intended(allOf(hasComponent(LoginActivity.class.getName()), hasExtra(is("email"), notNullValue())));
    }

    @Test
    public void error_test() {
        // setup
        final Response response = Response.error(401, mock(ResponseBody.class));
        doReturn(Single.error(new HttpException(response))).when(cypherpunkService).identifyEmail(any(EmailRequest.class));

        // test
        final Intent intent = IdentifyEmailActivity.createIntent(InstrumentationRegistry.getContext());
        activityRule.launchActivity(intent);

        // a@b.c と入力
        onView(withId(R.id.edit_text)).perform(typeText("a@b.c"));

        // ボタンクリック
        onView(withId(R.id.submit_button)).perform(click());

        // SignUpActivity に遷移するかチェック
        intended(allOf(hasComponent(SignUpActivity.class.getName()), hasExtra(is("email"), notNullValue())));
    }
}
