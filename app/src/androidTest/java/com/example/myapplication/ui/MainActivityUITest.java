package com.example.myapplication.ui;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.myapplication.RomApp.MainActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;

/**
 * UI tests for MainActivity using Espresso
 * Tests user interface interactions and navigation
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityUITest {

    @Before
    public void setUp() {
        // Launch the activity before each test
        ActivityScenario.launch(MainActivity.class);
    }

    @Test
    public void testMainActivity_LoadsSuccessfully() {
        // Verify that the main activity loads and displays key elements
        onView(withId(R.id.mainWelcome))
            .check(matches(isDisplayed()));
    }

    @Test
    public void testWelcomeCard_IsDisplayed() {
        // Verify that the welcome card is visible
        onView(withId(R.id.welcomeCard))
            .check(matches(isDisplayed()));
    }

    @Test
    public void testStartButton_IsClickable() {
        // Verify that the start button is clickable
        onView(withId(R.id.startButton))
            .check(matches(isClickable()));
    }

    @Test
    public void testStartButton_IsEnabled() {
        // Verify that the start button is enabled
        onView(withId(R.id.startButton))
            .check(matches(isEnabled()));
    }

    @Test
    public void testAppTitle_IsDisplayed() {
        // Verify that the app title is displayed
        onView(withId(R.id.appTitle))
            .check(matches(isDisplayed()));
    }

    @Test
    public void testAppDescription_IsDisplayed() {
        // Verify that the app description is displayed
        onView(withId(R.id.appDescription))
            .check(matches(isDisplayed()));
    }

    @Test
    public void testRegionButtons_AreDisplayed() {
        // Verify that region selection buttons are displayed
        onView(withId(R.id.regionButtonsContainer))
            .check(matches(isDisplayed()));
    }

    @Test
    public void testTransilvaniaButton_IsClickable() {
        // Verify that Transilvania button is clickable
        onView(withId(R.id.transilvaniaButton))
            .check(matches(isClickable()));
    }

    @Test
    public void testMunteniaButton_IsClickable() {
        // Verify that Muntenia button is clickable
        onView(withId(R.id.munteniaButton))
            .check(matches(isClickable()));
    }

    @Test
    public void testMoldovaButton_IsClickable() {
        // Verify that Moldova button is clickable
        onView(withId(R.id.moldovaButton))
            .check(matches(isClickable()));
    }

    @Test
    public void testOlteniaButton_IsClickable() {
        // Verify that Oltenia button is clickable
        onView(withId(R.id.olteniaButton))
            .check(matches(isClickable()));
    }

    @Test
    public void testDobrogeaButton_IsClickable() {
        // Verify that Dobrogea button is clickable
        onView(withId(R.id.dobrogeaButton))
            .check(matches(isClickable()));
    }

    @Test
    public void testBanatButton_IsClickable() {
        // Verify that Banat button is clickable
        onView(withId(R.id.banatButton))
            .check(matches(isClickable()));
    }

    @Test
    public void testCrisanaButton_IsClickable() {
        // Verify that Crișana button is clickable
        onView(withId(R.id.crisanaButton))
            .check(matches(isClickable()));
    }

    @Test
    public void testMaramuresButton_IsClickable() {
        // Verify that Maramureș button is clickable
        onView(withId(R.id.maramuresButton))
            .check(matches(isClickable()));
    }

    @Test
    public void testBucovinaButton_IsClickable() {
        // Verify that Bucovina button is clickable
        onView(withId(R.id.bucovinaButton))
            .check(matches(isClickable()));
    }

    @Test
    public void testSettingsButton_IsClickable() {
        // Verify that settings button is clickable
        onView(withId(R.id.settingsButton))
            .check(matches(isClickable()));
    }

    @Test
    public void testProfileButton_IsClickable() {
        // Verify that profile button is clickable
        onView(withId(R.id.profileButton))
            .check(matches(isClickable()));
    }

    @Test
    public void testLeaderboardButton_IsClickable() {
        // Verify that leaderboard button is clickable
        onView(withId(R.id.leaderboardButton))
            .check(matches(isClickable()));
    }

    @Test
    public void testRecipeButton_IsClickable() {
        // Verify that recipe button is clickable
        onView(withId(R.id.recipeButton))
            .check(matches(isClickable()));
    }

    @Test
    public void testMapButton_IsClickable() {
        // Verify that map button is clickable
        onView(withId(R.id.mapButton))
            .check(matches(isClickable()));
    }

    @Test
    public void testBottomNavigation_IsDisplayed() {
        // Verify that bottom navigation is displayed
        onView(withId(R.id.bottomNavigation))
            .check(matches(isDisplayed()));
    }

    @Test
    public void testBottomNavigation_AllItemsAreClickable() {
        // Verify that all bottom navigation items are clickable
        onView(withId(R.id.nav_home))
            .check(matches(isClickable()));
        
        onView(withId(R.id.nav_map))
            .check(matches(isClickable()));
        
        onView(withId(R.id.nav_recipes))
            .check(matches(isClickable()));
        
        onView(withId(R.id.nav_profile))
            .check(matches(isClickable()));
    }

    @Test
    public void testProgressIndicator_IsDisplayed() {
        // Verify that progress indicator is displayed
        onView(withId(R.id.progressIndicator))
            .check(matches(isDisplayed()));
    }

    @Test
    public void testAchievementBadge_IsDisplayed() {
        // Verify that achievement badge is displayed
        onView(withId(R.id.achievementBadge))
            .check(matches(isDisplayed()));
    }

    @Test
    public void testPointsDisplay_IsDisplayed() {
        // Verify that points display is visible
        onView(withId(R.id.pointsDisplay))
            .check(matches(isDisplayed()));
    }

    @Test
    public void testWelcomeMessage_ContainsExpectedText() {
        // Verify that welcome message contains expected text
        onView(withId(R.id.welcomeMessage))
            .check(matches(withText(org.hamcrest.Matchers.containsString("România"))));
    }

    @Test
    public void testAppVersion_IsDisplayed() {
        // Verify that app version is displayed
        onView(withId(R.id.appVersion))
            .check(matches(isDisplayed()));
    }

    @Test
    public void testLoadingAnimation_IsDisplayed() {
        // Verify that loading animation is displayed
        onView(withId(R.id.loadingAnimation))
            .check(matches(isDisplayed()));
    }

    @Test
    public void testErrorView_IsNotDisplayedInitially() {
        // Verify that error view is not displayed initially
        onView(withId(R.id.errorView))
            .check(matches(org.hamcrest.Matchers.not(isDisplayed())));
    }

    @Test
    public void testRetryButton_IsNotDisplayedInitially() {
        // Verify that retry button is not displayed initially
        onView(withId(R.id.retryButton))
            .check(matches(org.hamcrest.Matchers.not(isDisplayed())));
    }
} 