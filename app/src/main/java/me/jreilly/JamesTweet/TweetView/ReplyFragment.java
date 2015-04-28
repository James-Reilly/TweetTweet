/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.jreilly.JamesTweet.TweetView;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import io.realm.Realm;
import io.realm.RealmResults;
import me.jreilly.JamesTweet.Adapters.RealmAdapter;
import me.jreilly.JamesTweet.Models.RealmHelper;
import me.jreilly.JamesTweet.Models.TweetRealm;
import me.jreilly.JamesTweet.Profile.ProfileActivity;
import me.jreilly.JamesTweet.R;
import me.jreilly.JamesTweet.TweetParsers.ProfileSwitch;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ReplyFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ReplyFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReplyFragment extends android.support.v4.app.Fragment implements ProfileSwitch{
    private String LOG_TAG = "ReplyFragment";

    /** Variables to hold the data for the RecyclerView */
    private ProfileSwitch mActivity;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;

    /**Vairables for the Realm */
    private RealmAdapter mAdapter;
    private RealmResults<TweetRealm> mDataset;
    private RealmHelper mRealmHelper;

    public ReplyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_reply, container, false);


        //Get the database that is storing the tweet to be accessed
        Realm realm = Realm.getInstance(this.getActivity(), "replies.realm");


        //Query the database for the selected tweet
        mDataset = realm.where(TweetRealm.class).findAll();


        //Initialize the RecyclerView to hold the replies
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_replies);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        //Initialize variables for the adapter
        ProfileSwitch pFragment = this;
        int shortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        //initialize the RealmAdapter for the tweet
        mAdapter = new RealmAdapter(mDataset, rootView, shortAnimationDuration, pFragment, null, true);

        //Set the adapter to the recyclerview
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }


    /**
     * @param uId The ID of the profile to switch to
     * Starts the ProfileActivity with the uID passed as the intent
     */
    public void swapToProfile(String uId){
        Intent intent = new Intent(getActivity(), ProfileActivity.class)
                .putExtra(ProfileActivity.PROFILE_KEY, uId);
        startActivity(intent);
        getActivity().finish();
    }

    /**
     * @param tweetId The ID of the tweet to show
     * Starts the TweetActivity with the tweetID passed as the intent
     */
    public void swapToTweet(long tweetId, View view){
        Intent intent = new Intent(getActivity(), TweetActivity.class)
                .putExtra(TweetActivity.TWEET_KEY, tweetId).putExtra(TweetActivity.REALM_KEY,
                        "replies.realm");
        String transitionName = getString(R.transition.transition);
        Log.v(LOG_TAG, transitionName);
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(this.getActivity(),
                        view,   // The view which starts the transition
                        transitionName    // The transitionName of the view we’re transitioning to
                );

        ActivityCompat.startActivity(this.getActivity(), intent, options.toBundle());
    }

}
