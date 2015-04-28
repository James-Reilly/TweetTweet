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

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Realm;
import io.realm.RealmResults;
import me.jreilly.JamesTweet.Adapters.RealmAdapter;
import me.jreilly.JamesTweet.Etc.ComposeActivity;
import me.jreilly.JamesTweet.Models.RealmHelper;
import me.jreilly.JamesTweet.Models.TweetRealm;
import me.jreilly.JamesTweet.Profile.ProfileActivity;
import me.jreilly.JamesTweet.R;
import me.jreilly.JamesTweet.TweetParsers.ProfileLink;
import me.jreilly.JamesTweet.TweetParsers.ProfileSwitch;
import me.jreilly.JamesTweet.UserViews.MentionsFragment;
import me.jreilly.JamesTweet.UserViews.NotifyFragment;

/**
 * Created by jreilly on 4/24/15.
 */
public class NewTweetFragment extends android.support.v4.app.Fragment {
    private static final String LOG_TAG = "NotifcationsFragment";


    /** Variable to hold the visual objects that need to be filled */

    private TextView mTweet;
    private TextView mUser;
    private TextView mDate;
    private ImageButton mImage;
    private ImageButton mProfileImage;
    private ImageButton mReplyButton;
    private ImageButton mRetweetButton;
    private ImageButton mFavoriteButton;

    /** Variables to hold the data for the RecyclerView */
    private ProfileSwitch mActivity;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;

    /**Vairables for the Realm */
    private RealmAdapter mAdapter;
    private RealmResults<TweetRealm> mDataset;
    private RealmHelper mRealmHelper;

    private long mTweetId;
    private String mRealmLoc;
    private FragmentTabHost mTabHost;

    private TweetRealm t;

    private View mRoot;

    public NewTweetFragment() {
        // Required empty public constructor
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(TweetActivity.TWEET_KEY)){
            //Get the ID of the tweet to be displayed
            mTweetId = intent.getLongExtra(TweetActivity.TWEET_KEY, 0);

            //Get the database in which this tweet is stored
            mRealmLoc = intent.getStringExtra(TweetActivity.REALM_KEY);
            Log.v(LOG_TAG, "Got Tweet id: " + mTweetId);

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.new_fragment_tweet, container, false);
        mRoot = rootView;
        //Get Visuals to set
        mUser = (TextView) rootView.findViewById(R.id.my_user);
        mTweet = (TextView) rootView.findViewById(R.id.my_text);
        mImage = (ImageButton) rootView.findViewById(R.id.my_picture);
        mDate = (TextView) rootView.findViewById(R.id.my_time);
        mProfileImage = (ImageButton) rootView.findViewById(R.id.user_image);
        mReplyButton = (ImageButton) rootView.findViewById(R.id.tweet_reply_button);
        mRetweetButton = (ImageButton) rootView.findViewById(R.id.tweet_retweet_button);
        mFavoriteButton = (ImageButton) rootView.findViewById(R.id.tweet_favorite_button);

        //private variable to use to switch to other profiles
        mActivity = (ProfileSwitch) getActivity();

        //Get the database that is storing the tweet to be accessed
        Realm realm;
        if(mRealmLoc.equals("null")){
            realm  = Realm.getInstance(this.getActivity());
        }else{
            realm = Realm.getInstance(this.getActivity(), mRealmLoc);
        }

        //get at max 15 replies from tweet and populate data
        getReplies(mTweetId, 15, true);
        //Query the database for the selected tweet
        RealmResults<TweetRealm> result = realm.where(TweetRealm.class).equalTo("id", mTweetId).findAll();
        t = result.get(0);
        setUpLocalTweet();

        //Sets the layout of the fragment
        setTweet(result);

        //Setup the favorite/retweet/layout button (Haven't changed function name)
        setUpTweetLayout();





        //Get sorted tweets

        boolean conv = (mDataset != null && mDataset.size() > 0);
        boolean media = (!result.get(0).getMediaUrl().equals("null"));
        if(conv || media){
            mTabHost = (FragmentTabHost)rootView.findViewById(android.R.id.tabhost);
            mTabHost.invalidate();

            mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.realtabcontent);
            if(conv){
                mTabHost.addTab(mTabHost.newTabSpec("conversation").setIndicator("Conversation"),
                        ReplyFragment.class, null);
            }
            if(media){
                mTabHost.addTab(mTabHost.newTabSpec("media").setIndicator("Media"),
                        PictureFragment.class, null);
            }
        }else{
            mTabHost = (FragmentTabHost)rootView.findViewById(android.R.id.tabhost);
            ViewGroup layout = (LinearLayout) rootView.findViewById(R.id.tabRemover);
            layout.removeView(mTabHost);

        }



        return rootView;
    }


    /**
     * Sets all the visual elements of the detail view of the tweet using the private variable
     * mTweetId
     */
    public void setUpTweetLayout(){
        Twitter.getApiClient().getStatusesService().show(mTweetId, null, true, true, new Callback<Tweet>() {
            @Override
            public void success(Result<Tweet> tweetResult) {
                final Tweet t = tweetResult.data;


                //Setting the replaybutton
                mReplyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), ComposeActivity.class)
                                .putExtra(ComposeActivity.REPLY_ID, mTweetId).putExtra(ComposeActivity.REPLY_USER, "@" + t.user.screenName);
                        startActivity(intent);

                    }
                });

                //Set initial favorite highlight
                if(t.favorited){
                    mFavoriteButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_toggle_star_selected));
                } else {
                    mFavoriteButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_star_outline_grey600_24dp));
                }

                //setactionlistener
                setFavoriteButton();

                //Set initial retweet highlight
                if(t.retweeted){
                    mRetweetButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_cached_selected));
                }else {
                    mRetweetButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_cached_grey600_24dp));
                }
                //setactionlistener
                setRetweetButton();


                Log.v(LOG_TAG, "Set all attributes!");
            }

            @Override
            public void failure(TwitterException e) {
                Log.e(LOG_TAG, "Could not get Tweets : " + e);
            }
        });


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

    @Override
    public void onResume(){
        super.onResume();
        setUpRealm();
        getReplies(mTweetId, 15, true);
    }

    /**
     * Sets the realm up for the use of the fragment
     * It clears the database and assigns the database the data for the adapter
     */
    public void setUpRealm(){
        //Initialize the helper
        mRealmHelper = new RealmHelper(this.getActivity(), "replies.realm");
        //Get the tweet reply realm
        Realm realm = mRealmHelper.getRealm();
        //Clear the Database from previous tweets
        realm.beginTransaction();
        realm.clear(TweetRealm.class);
        realm.commitTransaction();
        //Get sorted tweets
        mDataset = mRealmHelper.getTweets(15);
    }

    public void setUpLocalTweet(){
        //Initialize the helper
        mRealmHelper = new RealmHelper(this.getActivity(), "tweet.realm");
        //Get the tweet reply realm
        Realm realm = mRealmHelper.getRealm();
        //Clear the Database from previous tweets
        realm.beginTransaction();
        realm.clear(TweetRealm.class);
        try{
            TweetRealm tweet = realm.createObject(TweetRealm.class);
            tweet.setDate(t.getDate());
            tweet.setRetweetedBy(t.getScreename());
            tweet.setOriginalId(t.getId());
            tweet.setRetweetedStatus(t.isRetweetedStatus());
            tweet.setProfileImageUrl(t.getProfileImageUrl());
            tweet.setId(t.getId());
            tweet.setName(t.getName());
            tweet.setScreename(t.getScreename());
            tweet.setText(t.getText());
            tweet.setMediaUrl(t.getMediaUrl());
            realm.commitTransaction();
        } catch (Exception e){
            realm.cancelTransaction();
            Log.e("tweet.realm", "Error: " + e);
        }
        realm.commitTransaction();


    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        Realm realm = mRealmHelper.getRealm();
        realm.beginTransaction();
        realm.clear(TweetRealm.class);
        realm.commitTransaction();
        realm.close();

    }

    /**
     * @param id The id of the tweet to get replies to
     * @param num_left number of depths left to search
     * @param first if the first tweet in the reply search
     * Gets the replies to a tweet recursively
     */
    public void getReplies(long id, final int num_left, final boolean first){
        Twitter.getApiClient().getStatusesService().show(id, null, true, true, new Callback<Tweet>() {

            @Override
            public void success(Result<Tweet> tweetResult) {
                if(!first){
                    mRealmHelper.insertToRealm(tweetResult.data);

                }
                if(tweetResult.data.inReplyToStatusIdStr != null && num_left > 0){
                    getReplies(tweetResult.data.inReplyToStatusId, num_left - 1, false);
                }
                if(num_left == 14){
                    mTabHost = (FragmentTabHost)mRoot.findViewById(android.R.id.tabhost);


                    mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.realtabcontent);

                        mTabHost.addTab(mTabHost.newTabSpec("conversation").setIndicator("Conversation"),
                                ReplyFragment.class, null);

                }

            }
            @Override
            public void failure(TwitterException e) {
                Log.e(LOG_TAG, "Excpetion: " + e);
            }
        });
    }



    public void setTweet(RealmResults<TweetRealm> mDataset){

        int i = 0;
        String user_img = mDataset.get(i).getProfileImageUrl();
        final String user_screen = mDataset.get(i).getScreename();
        String media_url = "null";
        Date created = mDataset.get(i).getDate();
        boolean retweeted = mDataset.get(i).isRetweetedStatus();
        String original = mDataset.get(i).getRetweetedBy();
        String username = mDataset.get(i).getName();
        String text = mDataset.get(i).getText();
        final long tId = mDataset.get(i).getId();


        //Load Profile Image
        Picasso.with(mProfileImage.getContext()).load(user_img).transform(new CircleTransform()).into(
                mProfileImage
        );

        //Set profile image to go to the users profile
        mProfileImage.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mActivity.swapToProfile(user_screen);
            }
        });

        final String imageUrl = media_url;
        ViewGroup.LayoutParams params =  mImage.getLayoutParams();

        //Set Cropped Media image and zoomImage animation
        if (!imageUrl.equals("null")){

            mImage.getLayoutParams().height = 400;
            Picasso.with(mImage.getContext()).load(imageUrl).fit().centerCrop().into(
                    mImage
            );


        } else {
            //Media is not need so it is hidden.
            mImage.setImageDrawable(null);

            mImage.getLayoutParams().height = 0;

        }

        //Set Username Text Field
        Calendar cal = Calendar.getInstance();
        mDate.setText(DateUtils.getRelativeTimeSpanString(created.getTime()));
        mUser.setText(username);
        String tweetText = text;

        //Highlight Profile names/hashtags and their clickable spans
        ArrayList<int[]> hashtagSpans = getSpans(tweetText, '#');
        ArrayList<int[]> profileSpans = getSpans(tweetText, '@');

        SpannableString tweetContent = new SpannableString(tweetText);

        for( int j = 0; j < profileSpans.size(); j ++){
            int[] span = profileSpans.get(j);
            int profileStart = span[0];
            int profileEnd = span[1];

            tweetContent.setSpan(new ProfileLink(mTweet.getContext(), mActivity),
                    profileStart, profileEnd, 0);
        }
        mTweet.setMovementMethod(LinkMovementMethod.getInstance());
        mTweet.setText(tweetContent);
    }

    /**
     * @param body The text to span
     * @param prefix The symbol at the beggining of the span
     * @return The clickable spans
     * Sets the clickable spans for any prefix
     * (This program uses it for @ strings and # strings
     */
    public ArrayList<int[]> getSpans(String body, char prefix) {
        ArrayList<int[]> spans = new ArrayList<int[]>();

        Pattern pattern = Pattern.compile(prefix + "\\w+");
        Matcher matcher = pattern.matcher(body);

        // Check all occurrences
        while (matcher.find()) {
            int[] currentSpan = new int[2];
            currentSpan[0] = matcher.start();
            currentSpan[1] = matcher.end();
            spans.add(currentSpan);
        }

        return  spans;
    }

    /**
     * sets the clickListener for the favorite button
     */
    public void setFavoriteButton(){
        mFavoriteButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Twitter.getApiClient().getStatusesService().show(mTweetId, null, true, true, new Callback<Tweet>() {
                    @Override
                    public void success(Result<Tweet> tweetResult) {
                        Tweet t = tweetResult.data;
                        if (t.favorited) {
                            TwitterCore.getInstance().getApiClient().getFavoriteService().destroy(mTweetId, null, new Callback<Tweet>() {
                                @Override
                                public void success(Result result) {
                                    Toast.makeText(mFavoriteButton.getContext(), "FAVORITE!",
                                            Toast.LENGTH_SHORT).show();
                                    mFavoriteButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_star_outline_grey600_24dp));
                                }

                                @Override
                                public void failure(TwitterException e) {
                                }
                            });

                        } else {
                            TwitterCore.getInstance().getApiClient().getFavoriteService().create(mTweetId, null, new Callback<Tweet>() {
                                @Override
                                public void success(Result result) {
                                    Toast.makeText(mFavoriteButton.getContext(), "FAVORITE!",
                                            Toast.LENGTH_SHORT).show();
                                    mFavoriteButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_toggle_star_selected));
                                }

                                @Override
                                public void failure(TwitterException e) {
                                    Toast.makeText(mFavoriteButton.getContext(), "Exception " + e,
                                            Toast.LENGTH_SHORT).show();
                                }
                            });

                        }

                    }

                    @Override
                    public void failure(TwitterException e) {
                        Log.e("TweetFragment", "Exception: " + e);
                    }


                });
            }
        });

    }

    /**
     * sets the clickListener for the retweet button
     */
    public void setRetweetButton(){
        mRetweetButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Twitter.getApiClient().getStatusesService().show(mTweetId, null, true, true, new Callback<Tweet>() {
                    @Override
                    public void success(Result<Tweet> tweetResult) {
                        Tweet t = tweetResult.data;
                        if (t.retweeted) {
                            Log.v(LOG_TAG, t.currentUserRetweet.toString());
                            String retweetId = t.currentUserRetweet.toString();
                            int id = retweetId.indexOf("id_str=") + 7;
                            String id2 = retweetId.substring(id, retweetId.length() - 1);
                            Log.v(LOG_TAG, id2);
                            long rtId = Long.valueOf(id2);
                            TwitterCore.getInstance().getApiClient().getStatusesService().destroy(rtId, null, new Callback<Tweet>() {
                                @Override
                                public void success(Result<Tweet> tweetResult) {
                                    Toast.makeText(mRetweetButton.getContext(), "UN-RETWEETED!",
                                            Toast.LENGTH_SHORT).show();
                                    mRetweetButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_cached_grey600_24dp));

                                }

                                @Override
                                public void failure(TwitterException e) {
                                    Log.e("TweetFragment", "Exception: " + e);
                                }
                            });
                        } else {
                            TwitterCore.getInstance().getApiClient().getStatusesService().retweet(
                                    mTweetId, null, new Callback<Tweet>() {
                                        @Override
                                        public void success(Result result) {
                                            Toast.makeText(mRetweetButton.getContext(), "RETWEET!",
                                                    Toast.LENGTH_SHORT).show();
                                            mRetweetButton.setImageDrawable(getResources()
                                                    .getDrawable(
                                                            R.drawable.ic_action_cached_selected));
                                        }

                                        @Override
                                        public void failure(TwitterException e) {
                                        }
                                    });
                        }
                    }

                    @Override
                    public void failure(TwitterException e) {
                    }
                });
            }
        });
    }

    /**
     * Transform a square image into a circular one.
     */

    public class CircleTransform implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());

            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
            if (squaredBitmap != source) {
                source.recycle();
            }

            Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
            paint.setShader(shader);
            paint.setAntiAlias(true);

            float r = size/2f;
            canvas.drawCircle(r, r, r, paint);

            squaredBitmap.recycle();
            return bitmap;
        }

        @Override
        public String key() {
            return "circle";
        }
    }


}
