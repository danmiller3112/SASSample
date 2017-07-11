package com.smartadserver.android.sassample;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.smartadserver.android.sassample.Misc.AdViewWrapper;
import com.smartadserver.android.sassample.ViewHolders.ListItemHolder;
import com.smartadserver.android.sassample.ViewHolders.DividerItemDecoration;

import java.util.ArrayList;

public class RecyclerActivity extends AppCompatActivity {

    /*****************************************
     * Ad Constants
     *****************************************/
    private final static String DOMAIN = "http://mobile.smartadserver.com";
    private final static int SITE_ID = 104808;
    private final static int FORMAT_ID = 15140;
    private final static String TARGET = "";

    private final static int AD_SPACING = 10;

    /*****************************************
     * Members declarations
     *****************************************/
    // UI
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private ArrayList<AdViewWrapper> mBannerWrappers;

    /**
     * performs Activity initialization after creation
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler);

        //Get UI Objects
        mRecyclerView = (RecyclerView) findViewById(R.id.activity_recycler_recycler_view);

        //Create Layout Manager
        mLayoutManager = new LinearLayoutManager(this);

        //BindViews
        bindViews();

        //Load Ads
        createAndLoadAds();
    }

    /**
     * Overriden to clean up SASAdView instances. This must be done to avoid IntentReceiver leak.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Make sur to destory all banners to avoid leaks
        for (int i = 0; i < mBannerWrappers.size(); i++) {
            final AdViewWrapper wrapper = mBannerWrappers.get(i);
            if (wrapper.mBanner != null) {
                wrapper.mBanner.onDestroy();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // add a OnGlobalLayoutListener to execute adaptBannerHeight once the activity has its new size set (otherwise it
        // uses previous orientation's size which is not what we want).
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        mRecyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                updateBannersHeight();
                            }
                        });
                    }
                });
            }
        });
    }

    /**
     * Bind Views.
     */
    private void bindViews() {
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(new ListLayoutAdapter());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
    }

    /**
     * Create and load Ads
     */
    private void createAndLoadAds() {
        mBannerWrappers = new ArrayList<AdViewWrapper>(3);
        mBannerWrappers.add(createBannerWrapper(SITE_ID, "663529", FORMAT_ID, TARGET));
        mBannerWrappers.add(createBannerWrapper(SITE_ID, "719318", FORMAT_ID, TARGET));
        mBannerWrappers.add(createBannerWrapper(SITE_ID, "663531", FORMAT_ID, TARGET));
    }


    /**
     * Create banner wrapper with param to load banner
     * See custom class "AdViewWrapper" for complete implementation
     */
    private AdViewWrapper createBannerWrapper(int siteId, String pageId, int formatId, String target) {
        LayoutInflater layoutInflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = layoutInflater.inflate(R.layout.list_ad, mRecyclerView, false);
        AdViewWrapper bannerWrapper = new AdViewWrapper(v);
        bannerWrapper.loadAd(DOMAIN, siteId, pageId, formatId, target);
        return bannerWrapper;
    }


    /**
     * Update banners height
     */
    private void updateBannersHeight() {
        if (mBannerWrappers != null && mBannerWrappers.size() > 0 ) {
            for (int i = 0; i < mBannerWrappers.size(); i++) {
                final AdViewWrapper wrapper = mBannerWrappers.get(i);
                int defaultHeight = (int) (50 * getResources().getDisplayMetrics().density);
                wrapper.updateBannerSize(defaultHeight);
            }
        }
    }

    /**
     * Find the correct AdViewWrapper for a given BannerViewHolder.
     * This will be used to unset holders in wrapper on recycle.
     */
    private AdViewWrapper wrapperForHolder(BannerViewHolder holder) {
        if (mBannerWrappers != null && mBannerWrappers.size() > 0 ) {
            for (int i = 0; i < mBannerWrappers.size(); i++) {
                final AdViewWrapper wrapper = mBannerWrappers.get(i);
                if (wrapper.mHolder == holder) {
                    return wrapper;
                }
            }
        }
        return null;
    }


    /**
     * The adapter class responsible for creating RecyclerView.ViewHolder instances for different cells.
     */
    private class ListLayoutAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int VIEW_TYPE_TEXT = 0;
        private static final int VIEW_TYPE_AD = 1;

        @Override
        public int getItemViewType(int position) {
            if (position == 0 || position % AD_SPACING != 0) {
                return VIEW_TYPE_TEXT;
            } else {
                return VIEW_TYPE_AD;
            }
        }

        /**
         * Get the correct AdViewWrapper for a given position
         */
        private AdViewWrapper wrapperForPosition(int position) {
            int intermediaire = (position / AD_SPACING) - 1;
            int index = intermediaire % mBannerWrappers.size();
            if (index < mBannerWrappers.size()){
                return mBannerWrappers.get(index);
            } else {
                return null;
            }
        }

        /**
         * On create : return the proper ViewHolder according to viewType.
         */
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (VIEW_TYPE_TEXT == viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
                return new ListItemHolder(v);
            } else {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_banner_holder, parent, false);
                return new BannerViewHolder(v);
            }
        }

        /**
         * On bind :
         * Type Text : Populate list view cell as you wish.
         * Type Ad : Get the AdViewWrapper corresponding to position and set its holder.
         */
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder listViewHolder, final int position) {
            if (VIEW_TYPE_TEXT == getItemViewType(position)) {
                ListItemHolder holder = (ListItemHolder) listViewHolder;
                String title = (position == 0) ? "Multiple banners in RecyclerView integration":"Nullam orci justo condimentum";
                String subTitle = (position == 0) ? "See implementation in RecyclerActivity. Please scroll down to see the ads.":"Phasellus in tellus eget arcu volutpat bibendum vulputate ac sapien. Vivamus enim elit, gravida vel consequat sit amet, scelerisque vitae ex.";
                holder.configureForItem(title, subTitle, position);
            } else {
                final BannerViewHolder bannerHolder = (BannerViewHolder) listViewHolder;
                final AdViewWrapper wrapper = wrapperForPosition(position);
                if (wrapper != null && wrapper.isAvailable()) { //To be available a wrapper must not have a ViewHolder.
                    wrapper.setHolder(bannerHolder);
                }
            }
        }

        /**
         * On recycle :
         * If the ViewHolder to be recycled was holding an Ad, tell the wrapper to unset it's holder.
         * The wrapper will become available again to be displayed in another holder.
         */
        @Override
        public void onViewRecycled(RecyclerView.ViewHolder holder) {
            super.onViewRecycled(holder);
            if (holder instanceof BannerViewHolder) {
                AdViewWrapper wrapper = wrapperForHolder((BannerViewHolder) holder);
                if (wrapper != null) {
                    wrapper.setHolder(null);
                }
            }
        }


        @Override
        public int getItemCount() {
            return 400;
        }

    }

    /**
     * ViewHolder subclass for banner ad cell
     */
    public class BannerViewHolder extends RecyclerView.ViewHolder {
        private BannerViewHolder(View itemView) {
            super(itemView);
        }
    }

}
