/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.jreilly.JamesTweet.Dashboard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.User;

import io.fabric.sdk.android.Fabric;
import me.jreilly.JamesTweet.Adapters.NavAdapter;
import me.jreilly.JamesTweet.Etc.DividerItemDecoration;
import me.jreilly.JamesTweet.Etc.SettingsActivity;
import me.jreilly.JamesTweet.Profile.ProfileActivity;
import me.jreilly.JamesTweet.R;
import me.jreilly.JamesTweet.UserViews.FavoritesFragment;
import me.jreilly.JamesTweet.UserViews.MentionsFragment;
import me.jreilly.JamesTweet.UserViews.NotificationsFragment;

/**
 * The main activity of the app.  It includes a navigation drawer to switch between features
 * It defaults to the timeline fragment (DashFragment)
 */

public class DashActivity extends ActionBarActivity{


    private Toolbar toolbar;
    RecyclerView mDrawerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    DrawerLayout mDrawer;

    Activity mActivity;

    ActionBarDrawerToggle mDrawerToggle;

    String mUsername;
    String mprofileUrl;

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "0uHL6HeVjnkKnRgw4QtFGUt0c";
    private static final String TWITTER_SECRET = "eA9rAd6tCbeWcmMhYvPG7Oo5BejcRItnZP0lYO4xv99QmjemCb";

    /*String Array of the navigation drawer items */
    String[] navItems = {

            "Timeline",
            "Mentions",
            "Favorites",
            "Settings"
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initialize Fabric on create
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        Fabric.with(this, new Crashlytics());
        //SetContentView
        setContentView(R.layout.activity_main);
        //Get the current users information


        Twitter.getApiClient().getAccountService().verifyCredentials(true, null, new Callback<User>() {
            @Override
            public void success(Result<User> userResult) {

                mUsername = userResult.data.name;
                mprofileUrl = userResult.data.profileImageUrl;
                mAdapter.notifyDataSetChanged();

            }

            @Override
            public void failure(TwitterException e) {

            }
        });
        //Initialize the navigation drawer
        initDrawer();
        //Start the dashboard fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content_frame, new DashFragment())
                    .commit();
        }


        //Set Support ActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



    }

    /**
     * Initializes the navigation drawer and its contents
     */
    public void initDrawer(){
        //Find the view id
        mDrawerView = (RecyclerView) findViewById(R.id.left_drawer);
        //Drawer size is fixed
        mDrawerView.setHasFixedSize(true);
        //Add List Dividers
        mDrawerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

        //Initialize the Nav Adapter to populate the drawer
        mAdapter = new NavAdapter(navItems, this, mUsername, mprofileUrl);
        mActivity = this;
        mDrawerView.setAdapter(mAdapter);

        //Detect swipe to open navigation drawer
        final GestureDetector mGestureDetector = new GestureDetector(DashActivity.this, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onSingleTapUp(MotionEvent e){
                return true;
            }
        });

        //Detect Item CLicks and respond accordingly
        mDrawerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {

            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                View child = rv.findChildViewUnder(e.getX(),e.getY());

                if (child != null && mGestureDetector.onTouchEvent(e)){

                    if(rv.getChildPosition(child) == 0){
                        swapToProfile();
                    } else if (navItems[rv.getChildPosition(child) - 1].equals("Timeline")){
                        swapToTimeline();
                    } else if (navItems[rv.getChildPosition(child) - 1].equals("Mentions")){
                        mDrawer.closeDrawers();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.content_frame, new NotificationsFragment()).commit();
                    } else if (navItems[rv.getChildPosition(child) - 1].equals("Favorites")) {
                        mDrawer.closeDrawers();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.content_frame, new FavoritesFragment()).commit();
                    } else if (navItems[rv.getChildPosition(child) - 1].equals("Settings")){
                        Intent intent = new Intent(mActivity, SettingsActivity.class);
                        startActivity(intent);
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }
        });

        mLayoutManager = new LinearLayoutManager(this);
        mDrawerView.setLayoutManager(mLayoutManager);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this,mDrawer,this.toolbar, R.string.openDrawer, R.string.closeDrawer){
            @Override
            public void onDrawerOpened(View drawerView){
                super.onDrawerOpened(drawerView);
            }
            @Override
            public void onDrawerClosed(View drawerView){
                super.onDrawerClosed(drawerView);
            }
        };

        mDrawer.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();


    }

    /**
     * starts the profile activity of the current users profile
     */
    public void swapToProfile(){
        //Get Screen name of User
        String uId = Twitter.getSessionManager().getActiveSession().getUserName();
        Intent intent = new Intent(mActivity, ProfileActivity.class)
                .putExtra(ProfileActivity.PROFILE_KEY, uId);
        startActivity(intent);
    }

    /**
     * Swaps the fragment of the Activity to the current users DashFragment (timeline)
     */
    public void swapToTimeline(){
        mDrawer.closeDrawers();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, new DashFragment()).commit();
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == android.R.id.home){

            if(mDrawer.isDrawerOpen(mDrawerView)){
                mDrawer.closeDrawer(mDrawerView);
            }else {
                mDrawer.openDrawer(mDrawerView);
            }
        }

        return super.onOptionsItemSelected(item);
    }

}
