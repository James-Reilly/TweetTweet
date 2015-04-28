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

package me.jreilly.JamesTweet.UserViews;

import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.jreilly.JamesTweet.R;

/**
 * Created by jreilly on 4/4/15.
 */
public class NotificationsFragment extends android.support.v4.app.Fragment  {

    private static final String LOG_TAG = "NotifcationsFragment";



    private FragmentTabHost mTabHost;


    public NotificationsFragment() {
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
        View rootView = inflater.inflate(R.layout.notifications_fragment, container, false);
        this.getActivity().setTitle("Mentions");


        mTabHost = (FragmentTabHost)rootView.findViewById(android.R.id.tabhost);

        mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.realtabcontent);
        mTabHost.addTab(mTabHost.newTabSpec("notifications").setIndicator("Notifications"),
                NotifyFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("mentions").setIndicator("Mentions"),
                MentionsFragment.class, null);

        return rootView;
    }





}