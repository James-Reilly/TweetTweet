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
package me.jreilly.JamesTweet.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;

import me.jreilly.JamesTweet.Adapters.MyTwitterApiClient;
import me.jreilly.JamesTweet.Etc.twitterRelationship.TwitterRelationship;
import me.jreilly.JamesTweet.Etc.unfollow.DestroyObject;
import me.jreilly.JamesTweet.R;

/**
 * Created by jreilly on 1/19/15.
 */
public class ProfileActivity extends ActionBarActivity {
    MenuItem followButton;
    public static final String PROFILE_KEY = "profile_id";
    String mUserId;
    Boolean following;

    private final String LOG_TAG = "ProfileAcitivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(ProfileActivity.PROFILE_KEY)){
            mUserId = intent.getStringExtra(ProfileActivity.PROFILE_KEY);

        }
        getFreindships();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ProfileFragmentV2())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        followButton = menu.findItem(R.id.action_follow);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        // Handle presses on the action bar items

        switch (item.getItemId()) {
            case R.id.action_follow:
                if(!mUserId.equals(Twitter.getSessionManager().getActiveSession().getUserName())){
                    followUnfollow();
                }
                return true;
            case R.id.action_settings:
                return true;
            case android.R.id.home:
                onBackPressed();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Checks if the current user if following the selected profile
     * Updates the UI to reflect any changes
     */
    public void getFreindships(){
        final MyTwitterApiClient mClient  = new MyTwitterApiClient(Twitter.getSessionManager().getActiveSession());
        final String mMyUsername = Twitter.getSessionManager().getActiveSession().getUserName();
        Log.v("PROFILE", "Checking Source: " + mUserId + " and Target: " + mMyUsername);
        mClient.getCustomService().show(mUserId, mMyUsername, new Callback<TwitterRelationship>() {
            @Override
            public void success(Result<TwitterRelationship> result) {
                if(result != null && result.data != null){
                    if (result.data.getRelationship().getTarget().isFollowing()){
                        followButton.setIcon(R.drawable.ic_action_social_person_2);
                        Log.v(LOG_TAG, "Target: " + result.data.getRelationship().getTarget().isFollowing());

                    }else{
                        followButton.setIcon(R.drawable.ic_action_social_person_add);
                    }
                    following = result.data.getRelationship().getTarget().isFollowing();

                }

            }

            @Override
            public void failure(TwitterException e) {
                Log.e(LOG_TAG, "Freinship Exception: " + e);
            }
        });
    }

    /**
     * If the current user is following the selected user it unfollows the user and updates the UI
     * If the current user is not following the selected user it follows the user and updates the UI
     */
    public void followUnfollow(){
        //get the Twitter API client
        MyTwitterApiClient mClient  = new MyTwitterApiClient(Twitter.getSessionManager().getActiveSession());
        if(following){
            //get the custom service
            mClient.getCustomService().destroy(mUserId, new Callback<DestroyObject>() {
                @Override
                public void success(Result<DestroyObject> result) {
                    Log.v(LOG_TAG, "Un-Followed!");
                    //update UI
                    getFreindships();
                }

                @Override
                public void failure(TwitterException e) {
                    Log.e(LOG_TAG, "Exception UnFollow: " + e);
                }
            });


        }else{
            mClient.getCustomService().create(mUserId, true, new Callback<DestroyObject>() {
                @Override
                public void success(Result<DestroyObject> result) {
                    Log.v(LOG_TAG, "Followed!");
                    //Update UI
                    getFreindships();
                }

                @Override
                public void failure(TwitterException e) {
                    Log.e(LOG_TAG, "Exception Follow: " + e);
                }
            });

        }
    }

}
