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
import static androidx.test.espresso.action.ViewActions.clearText;
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
    Matcher<View> isTextValueEqualTo(final String content) {

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
                    // Eliminate invalid characters from text field
                    text = text.substring(0,content.length());
                    // Test equality and return result
                    return (text.equalsIgnoreCase(content));
                }
                return false;
            }
        };
    }
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);
    @Rule
    public ActivityTestRule<SearchActivity> sActivityRule = new ActivityTestRule<>(SearchActivity.class);
    @Test
    public void ensureCaptionChangesWork() {
        String test = "TestCaption";
        // Select caption box and type test caption
        onView(withId(R.id.editText)).perform(click());
        onView(withId(R.id.editText)).perform(clearText(), typeText(test),closeSoftKeyboard());
        onView(withId(R.id.applyCaption)).perform(click());
        // Scroll to next photo, scroll back
        onView(withId(R.id.btnRight)).perform(click());
        onView(withId(R.id.btnLeft)).perform(click());
        // Check that caption is same as test caption
        onView(withId(R.id.editText)).check(matches(isTextValueEqualTo(test)));

    }
    @Test
    public void ensureSearchingWorks() {
        String startDate = "2020-02-17";
        String endDate   = "2020-02-18";
        String latitude  = "49";
        String longitude = "-122";
        String dist = "100";

        // Go to search activity
        onView(withId(R.id.btnFilter)).perform((click()));

        // Click on filter boxes, enter start and end dates, location data
        onView(withId(R.id.search_fromDate)).perform(click());
        onView(withId(R.id.search_fromDate)).perform(typeText(startDate), closeSoftKeyboard());
        onView(withId(R.id.search_toDate)).perform(click());
        onView(withId(R.id.search_toDate)).perform(typeText(endDate), closeSoftKeyboard());
        onView(withId(R.id.editLat)).perform(click());
        onView(withId(R.id.editLat)).perform(typeText(latitude), closeSoftKeyboard());
        onView(withId(R.id.editLong)).perform(click());
        onView(withId(R.id.editLong)).perform(typeText(longitude), closeSoftKeyboard());
        onView(withId(R.id.editDist)).perform(click());
        onView(withId(R.id.editDist)).perform(typeText(dist), closeSoftKeyboard());

        // Click on search button
        onView(withId(R.id.search_search)).perform(click());

        // Scroll through 3 expected photos and ensure that all images have date in range
        for (int i = 0; i<=2; i++){
            onView(withId(R.id.dateText)).check(matches(isTextValueEqualTo(startDate)));
            onView(withId(R.id.btnRight)).perform(click());
        }
        onView(withId(R.id.dateText)).check(matches(isTextValueEqualTo(startDate)));

        String cap = "Two";
        // Go to search activity
        // Type search caption
        onView(withId(R.id.btnFilter)).perform((click()));
        onView(withId(R.id.captionText)).perform(click());
        onView(withId(R.id.captionText)).perform(typeText(cap), closeSoftKeyboard());
        onView(withId(R.id.search_search)).perform(click());

        // Check only one photo is returned
        onView(withId(R.id.editText)).check(matches(isTextValueEqualTo(cap)));
        onView(withId(R.id.btnRight)).perform(click());
        onView(withId(R.id.btnRight)).perform(click());
        onView(withId(R.id.editText)).check(matches(isTextValueEqualTo(cap)));

        String noPhotos = "No photos found. Try adjusting search filters.";

        // Test that searching for old dates or distant locations return no results
        onView(withId(R.id.btnFilter)).perform((click()));
        onView(withId(R.id.search_fromDate)).perform(click());
        onView(withId(R.id.search_fromDate)).perform(typeText("2010-01-01"), closeSoftKeyboard());
        onView(withId(R.id.search_toDate)).perform(click());
        onView(withId(R.id.search_toDate)).perform(typeText("2014-01-01"), closeSoftKeyboard());
        onView(withId(R.id.search_search)).perform(click());
        onView(withId(R.id.noResult)).check(matches(isTextValueEqualTo(noPhotos)));

        // Show all photos again
        onView(withId(R.id.btnFilter)).perform((click()));
        onView(withId(R.id.search_fromDate)).perform(click());
        onView(withId(R.id.search_fromDate)).perform(typeText("2010-01-01"), closeSoftKeyboard());
        onView(withId(R.id.search_toDate)).perform(click());
        onView(withId(R.id.search_toDate)).perform(typeText("2025-01-01"), closeSoftKeyboard());
        onView(withId(R.id.search_search)).perform(click());
        onView(withId(R.id.noResult)).check(matches(isTextValueEqualTo("")));

        // Set to distant location
        onView(withId(R.id.btnFilter)).perform((click()));
        onView(withId(R.id.editLat)).perform(click());
        onView(withId(R.id.editLat)).perform(typeText("0.0"), closeSoftKeyboard());
        onView(withId(R.id.editLong)).perform(click());
        onView(withId(R.id.editLong)).perform(typeText("0.0"), closeSoftKeyboard());
        onView(withId(R.id.editDist)).perform(click());
        onView(withId(R.id.editDist)).perform(typeText(dist), closeSoftKeyboard());
        onView(withId(R.id.search_search)).perform(click());
        onView(withId(R.id.noResult)).check(matches(isTextValueEqualTo(noPhotos)));
    }

}

