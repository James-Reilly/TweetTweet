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
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import io.realm.Realm;
import io.realm.RealmResults;
import me.jreilly.JamesTweet.Models.TweetRealm;
import me.jreilly.JamesTweet.R;


public class PictureFragment extends android.support.v4.app.Fragment {



    public PictureFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_picture, container, false);
        ImageView mImage = (ImageView) rootView.findViewById(R.id.tweet_picture);

        //Get the database that is storing the tweet to be accessed
        Realm realm = Realm.getInstance(this.getActivity(), "tweet.realm");


        //Query the database for the selected tweet
        RealmResults<TweetRealm> result = realm.where(TweetRealm.class).findAll();
        if(result.size() == 1){
            Picasso.with(mImage.getContext()).load(result.get(0).getMediaUrl()).into(
                    mImage
            );
        }




        return rootView;
    }







}
