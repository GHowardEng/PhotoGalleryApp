package com.example.photogalleryapp;

import android.widget.EditText;

import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import android.view.View;
import android.widget.TextView;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;



@RunWith(AndroidJUnit4.class)
public class UITest {
    // Method to test text field of editText
    Matcher<View> isEditTextValueEqualTo(final String content) {

        return new TypeSafeMatcher<View>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("Match Edit Text Value with View ID Value : :  " + content);
            }

            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof TextView) && !(view instanceof EditText)) {
                    return false;
                }
                if (view != null) {
                    String text;
                    if (view instanceof TextView) {
                        text =((TextView) view).getText().toString();
                    } else {
                        text =((EditText) view).getText().toString();
                    }

                    return (text.equalsIgnoreCase(content));
                }
                return false;
            }
        };
    }
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);
    @Test
    public void ensureTextChangesWork() {
        String test = "TestCaption";
        // Select caption box and type test caption
        onView(withId(R.id.editText)).perform(click());
        onView(withId(R.id.editText)).perform(replaceText(test), closeSoftKeyboard());
        onView(withId(R.id.applyCaption)).perform(click());
        // Scroll to next photo, scroll back
        onView(withId(R.id.btnRight)).perform(click());
        onView(withId(R.id.btnLeft)).perform(click());
        // Check that caption is same as test caption
        onView(withId(R.id.editText)).check(matches(isEditTextValueEqualTo(test)));
    }
}

