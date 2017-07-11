package com.smartadserver.android.sassample;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.smartadserver.android.library.SASBannerView;
import com.smartadserver.android.library.model.SASAdElement;
import com.smartadserver.android.library.ui.SASAdView;
import com.smartadserver.android.sassample.ViewHolders.DividerItemDecoration;

public class SimpleRecyclerActivity extends AppCompatActivity {

    /* --------------------------- */
    /* Const declaration
    /* --------------------------- */

    private final static int MAX_BANNER_HEIGHT = 500;
    private final static int DEFAULT_BANNER_HEIGHT = 0;

    private final static String DOMAIN = "http://mobile.smartadserver.com";
    private final static int SITE_ID = 104808;
    private final static String PAGE_ID = "663262";
    private final static int FORMAT_ID = 15140;
    private final static String TARGET = "";

    private static final int AD_POSITION = 10;

    /* --------------------------- */
    /* Members declaration
    /* --------------------------- */

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager = null;

    // reference to the sole BannerListViewHolder created for onDestroy purposes
    private BannerListViewHolder mBannerHolder;


    /* --------------------------- */
    /* Implementation
    /* --------------------------- */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLayoutManager = new LinearLayoutManager(this);
        setContentView(R.layout.activity_simple_recycler);

        bindViews();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mBannerHolder != null && mBannerHolder.banner != null) {
            mBannerHolder.banner.onDestroy();
        }
    }

    /**
     * Bind Views
     */
    private void bindViews() {
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_list);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(new ListLayoutAdapter());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
    }

    /**
     * Resize the height of the cell containing the banner.
     */
    private void resizeBannerCell(final int height, BannerListViewHolder bannerHolder) {

        if (bannerHolder != null) {
            final SASBannerView banner = bannerHolder.banner;
            final ViewGroup.LayoutParams bannerLayout = bannerHolder.banner.getLayoutParams();
            bannerLayout.height = height;
            banner.setLayoutParams(bannerLayout);
        }

    }

    /**
     * ViewHolder subclass for generic text cell
     */
    private class TextListViewHolder extends RecyclerView.ViewHolder {

        public TextView textViewIndex;
        public TextView textViewTitle;
        public TextView textViewSubtitle;

        public TextListViewHolder(View itemView) {
            super(itemView);
            textViewIndex = (TextView) itemView.findViewById(R.id.index);
            textViewTitle = (TextView) itemView.findViewById(R.id.title);
            textViewSubtitle = (TextView) itemView.findViewById(R.id.subtitle);
        }

        public void setTextViewTitle(String title) {
            textViewTitle.setText(title);
        }

        public void setTextViewSubtitle(String subtitle) {
            textViewSubtitle.setText(subtitle);
        }

        public void setTextViewIndex(int index) {
            if (index > 0) {
                textViewIndex.setVisibility(View.VISIBLE);
                textViewIndex.setText(String.valueOf(index));
                textViewSubtitle.setTextColor(Color.parseColor("#bbbbbb"));
            } else {
                textViewIndex.setVisibility(View.GONE);
                textViewSubtitle.setTextColor(Color.parseColor("#ff009688"));
            }
        }
    }

    /**
     * ViewHolder subclass for banner ad cell
     */
    private class BannerListViewHolder extends RecyclerView.ViewHolder {

        public SASBannerView banner;
        // flag to force ad request to occur only once
        private boolean mIsAdLoaded = false;


        public BannerListViewHolder(View itemView) {
            super(itemView);
            banner = (SASBannerView) itemView.findViewById(R.id.banner);
        }

        public void loadAd() {
            if (!mIsAdLoaded) {
                SASAdView.setBaseUrl(DOMAIN);
                banner.loadAd(SITE_ID, PAGE_ID, FORMAT_ID, true, TARGET, new SASAdView.AdResponseHandler() {
                    @Override
                    public void adLoadingCompleted(SASAdElement sasAdElement) {
                        final SASAdElement adElement = sasAdElement;

                        Runnable resizeRunnable = new Runnable() {
                            @Override
                            public void run() {
                                int height = adElement.getPortraitHeight();
                                int width = adElement.getPortraitWidth();
                                double creativeRatio = width / (double) height;
                                int screenWidth = mRecyclerView.getWidth();
                                int neededHeight = (int) Math.max(DEFAULT_BANNER_HEIGHT, Math.min(MAX_BANNER_HEIGHT, screenWidth / creativeRatio));
                                resizeBannerCell(neededHeight, BannerListViewHolder.this);
                            }
                        };

                        banner.executeOnUIThread(resizeRunnable);
                    }


                    @Override
                    public void adLoadingFailed(Exception e) {
                        banner.executeOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                resizeBannerCell(0, BannerListViewHolder.this);
                            }
                        });
                    }
                });
                banner.addStateChangeListener(new SASAdView.OnStateChangeListener() {
                    @Override
                    public void onStateChanged(SASAdView.StateChangeEvent stateChangeEvent) {
                        if (stateChangeEvent.getType() == SASAdView.StateChangeEvent.VIEW_DEFAULT) {
                            // Fix issue with loader not removed on some KitKat versions (4.4.2 and below)
                            if (android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
                                banner.removeLoaderView(banner.getLoaderView());
                            }
                        }
                    }
                });

                // uncomment this line to mark ad as being requested for the SASBannerView
                // Ad will not be requested again when its cell is scrolled out of the screen and
                // in again.
                //mIsAdLoaded = true;
            }
        }
    }

    /**
     * The adapter class responsible for creating RecyclerView.ViewHolder instances for different cells.
     */
    private class ListLayoutAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int VIEW_TYPE_TEXT = 0;
        private static final int VIEW_TYPE_AD = 1;

        @Override
        public int getItemViewType(int position) {
            if (position == AD_POSITION) {
                return VIEW_TYPE_AD;
            } else {
                return VIEW_TYPE_TEXT;
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (VIEW_TYPE_TEXT == viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
                return new TextListViewHolder(v);
            } else {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_ad, parent, false);
                mBannerHolder = new BannerListViewHolder(v);
                return mBannerHolder;
            }
        }


        /* (non-Javadoc)
		 * @see android.support.v7.widget.RecyclerView.Adapter#onViewRecycled(android.support.v7.widget.RecyclerView.ViewHolder)
		 */
        @Override
        public void onViewRecycled(RecyclerView.ViewHolder holder) {
            super.onViewRecycled(holder);

            if (holder instanceof BannerListViewHolder) {
                BannerListViewHolder bannerHolder = (BannerListViewHolder)holder;
                // do not reset the banner if the persisting mIsAdLoaded flag is true (as the ad of banner
                // will not be requested again when the cell is disposed/created again)
                if (!bannerHolder.mIsAdLoaded) {
                    bannerHolder.banner.reset();
                    resizeBannerCell(DEFAULT_BANNER_HEIGHT,(BannerListViewHolder)holder);
                }
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder listViewHolder, final int position) {
            if (VIEW_TYPE_TEXT == getItemViewType(position)) {
                TextListViewHolder holder = (TextListViewHolder) listViewHolder;
                holder.textViewTitle.setGravity(Gravity.LEFT);

                String title = (position == 0) ? "Single banner in RecyclerView integration":"Nullam orci justo condimentum";
                String subTitle = (position == 0) ? "See implementation in SimpleRecyclerActivity. Please scroll down to see the ads.":"Phasellus in tellus eget arcu volutpat bibendum vulputate ac sapien. Vivamus enim elit, gravida vel consequat sit amet, scelerisque vitae ex.";

                holder.setTextViewTitle(title);
                holder.setTextViewSubtitle(subTitle);
                holder.setTextViewIndex(position);
            } else {
                final BannerListViewHolder bannerHolder = (BannerListViewHolder) listViewHolder;
                bannerHolder.loadAd();
            }
        }

        @Override
        public int getItemCount() {
            return 30;
        }

    }

}
