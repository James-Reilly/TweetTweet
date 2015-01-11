package me.jreilly.JamesTweet;


import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.OAuthSigning;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;
import com.twitter.sdk.android.tweetui.TweetUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class DashFragment extends android.support.v4.app.Fragment {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Tweet> mTweetObjects = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String[] mDataset;
    private final String LOG_TAG = "TweetFetcher";
    private int mTotalItems = 20;



    public DashFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_timeline);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.fragment_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(){
                getMainUserTweets(true);
            }

        });

        mLayoutManager = new LinearLayoutManager(getActivity());


        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new MyAdapter(mTweetObjects);
        mRecyclerView.setAdapter(mAdapter);


        mRecyclerView.setOnScrollListener(
                new EndlessRecyclerOnScrollListener((LinearLayoutManager)mLayoutManager) {
                  @Override
                  public void onLoadMore() {
                      mTotalItems += 20;
                      mSwipeRefreshLayout.setRefreshing(true);
                      getMainUserTweets(false);
                  }
              });

        getMainUserTweets(true);

        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();
        getMainUserTweets(true);
    }

    /*
    Pulls the hometimeline of the user current session's user
    It updates the data with the new tweets
     */
    public void getMainUserTweets(boolean pageRefresh) {

        final StatusesService service = Twitter.getApiClient().getStatusesService();

        if (pageRefresh){
            service.homeTimeline(50, null, null, null, null, null, null, new Callback<List<Tweet>>() {
                @Override
                public void success(Result<List<Tweet>> listResult) {
                    mTweetObjects.clear();
                    mTweetObjects.addAll(listResult.data);
                    mRecyclerView.getAdapter().notifyDataSetChanged();
                    mSwipeRefreshLayout.setRefreshing(false);


                }

                @Override
                public void failure(TwitterException e) {
                    Log.v(LOG_TAG, "No tweets collected on main refresh! " + e);
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            });
        } else if (mTweetObjects.size() != 200) {
            long maxId = mTweetObjects.get(mTotalItems - 20 - 1).id;
            service.homeTimeline(51, null, maxId, null, null, null, null, new Callback<List<Tweet>>() {
                @Override
                public void success(Result<List<Tweet>> listResult) {

                    mTweetObjects.addAll(listResult.data.subList(0,listResult.data.size() - 1));
                    mRecyclerView.getAdapter().notifyDataSetChanged();
                    mSwipeRefreshLayout.setRefreshing(false);
                }

                @Override
                public void failure(TwitterException e) {
                    Log.v(LOG_TAG, "No tweets collected! on other refresh : " + e);
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            });
        } else {
            Context context = getActivity();
            CharSequence text = "End of Stream";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

    public abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {
        private int previousToral = 0;
        private boolean loading = true;
        private int visibleThreshold = 5;
        int firstVisibleItem, visibleItemCount, totalItemCount;

        private LinearLayoutManager mLinearLayoutManager;

        public EndlessRecyclerOnScrollListener(LinearLayoutManager linearLayoutManager){
            this.mLinearLayoutManager = linearLayoutManager;
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy){
            super.onScrolled(recyclerView,dx,dy);

            visibleItemCount = recyclerView.getChildCount();
            totalItemCount = mLinearLayoutManager.getItemCount();
            firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();

            if (loading){
                if (totalItemCount > previousToral) {
                    loading = false;
                    previousToral = totalItemCount;
                }
            }
            if (!loading && (totalItemCount - visibleItemCount)
                    <= (firstVisibleItem + visibleThreshold)) {
                onLoadMore();
                loading = true;

            }
        }

        public abstract void onLoadMore();


    }
}
