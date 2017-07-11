package com.smartadserver.android.sassample.Misc;

import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.smartadserver.android.library.SASBannerView;
import com.smartadserver.android.library.model.SASAdElement;
import com.smartadserver.android.library.ui.SASAdView;
import com.smartadserver.android.sassample.R;
import com.smartadserver.android.sassample.RecyclerActivity;


/**
 * Created by Thomas Geley on 03/06/2016.
 */

public class AdViewWrapper {

    /* --------------------------- */
    /* Const declaration
    /* --------------------------- */

    private final static int MAX_BANNER_HEIGHT = 1800;
    private final static int DEFAULT_BANNER_HEIGHT = 0;

    private final static String TAG = AdViewWrapper.class.getSimpleName();


    /* --------------------------- */
    /* Members declaration
    /* --------------------------- */
    public SASBannerView mBanner;
    public RecyclerActivity.BannerViewHolder mHolder;
    private boolean mIsAdLoaded;


    public AdViewWrapper (View itemView) {
        mBanner = (SASBannerView) itemView.findViewById(R.id.banner); //Instantiate banner from layout.

        // add video event listener
        mBanner.addVideoEventListener(new SASAdView.OnVideoEventListener() {
            @Override
            public void onVideoEvent(int videoEvent) {
                switch (videoEvent) {
                    case VIDEO_START:
                        Log.d(TAG, "Video event : VIDEO_START");
                        break;
                    case VIDEO_PAUSE:
                        Log.d(TAG, "Video event : VIDEO_PAUSE");
                        break;
                    case VIDEO_RESUME:
                        Log.d(TAG, "Video event : VIDEO_RESUME");
                        break;
                    case VIDEO_REWIND:
                        Log.d(TAG, "Video event : VIDEO_REWIND");
                        break;
                    case VIDEO_FIRST_QUARTILE:
                        Log.d(TAG, "Video event : VIDEO_FIRST_QUARTILE");
                        break;
                    case VIDEO_MIDPOINT:
                        Log.d(TAG, "Video event : VIDEO_MIDPOINT");
                        break;
                    case VIDEO_THIRD_QUARTILE:
                        Log.d(TAG, "Video event : VIDEO_THIRD_QUARTILE");
                        break;
                    case VIDEO_COMPLETE:
                        Log.d(TAG, "Video event : VIDEO_COMPLETE");
                        break;
                    case VIDEO_SKIP:
                        Log.d(TAG, "Video event : VIDEO_SKIP");
                        break;
                    case VIDEO_ENTER_FULLSCREEN:
                        Log.d(TAG, "Video event : VIDEO_ENTER_FULLSCREEN");
                        break;
                    case VIDEO_EXIT_FULLSCREEN:
                        Log.d(TAG, "Video event : VIDEO_EXIT_FULLSCREEN");
                        break;
                }
            }
        });
    }


    public boolean isAvailable() {
        return mHolder == null;
    }

    /**
     * Set and unset ViewHolder
     * The banner will be added (remove) to the ViewHolder.itemView and displayed on screen
     */
    public void setHolder(RecyclerActivity.BannerViewHolder aHolder) {

        if (mHolder == aHolder) {
            return;
        }

        //Set new holder. Note that holder passed can be null (when Recyclerview.ViewHolder is recycled for example)
        mHolder = aHolder;

        //Remove banner from it's parent if possible
        ViewGroup parent = ((ViewGroup) mBanner.getParent());
        // dismiss sticky mode if activated (mandatory before removing the banner or it will be messed up...)
        mBanner.dismissStickyMode(false);
        if (parent != null) { parent.removeView(mBanner); }

        //If holder is not null, add banner as a child
        //Then update banner size with a post (so it is executed when all layout has been updated)
        if (mHolder != null) {
            ViewGroup vg = (ViewGroup)mHolder.itemView.findViewById(R.id.banner_container);
            vg.removeAllViews();
            vg.addView(mBanner);
            mBanner.setScrollListenerEnabled(true);
            mBanner.post(new Runnable() {
                @Override
                public void run() {
                    updateBannerSize(DEFAULT_BANNER_HEIGHT);
                }
            });
        }
    }

    /**
     * Resize banner cell.
     * Will update the height of the holder and of the banner.
     * @param height the target height
     */
    private void resizeBannerCell(final int height) {

        if (mHolder != null) {
            //Update holder itemview layout so it can display the banner
            final ViewGroup.LayoutParams holderLayout = mHolder.itemView.getLayoutParams();
            holderLayout.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            mHolder.itemView.setLayoutParams(holderLayout);
            View tv = mHolder.itemView.findViewById(R.id.placeholder);
            tv.setVisibility(View.GONE);

            //Update banner layout to fit the proper height
            final ViewGroup.LayoutParams bannerLayout = mBanner.getLayoutParams();
            bannerLayout.height = height;
            mBanner.setLayoutParams(bannerLayout);
        }

    }


    public void updateBannerSize(final int defaultHeight) {
        //Only execute if banner exist, holder exists, ad is loaded and not expanded.
        if (mBanner != null && mHolder != null && mIsAdLoaded && !mBanner.isExpanded()) {

            mBanner.executeOnUIThread(new Runnable() {
                @Override
                public void run() {
                    //Use getOptimalHeight() convenient method to display your ad with the proper aspect ratio !
                    int height = mBanner.getOptimalHeight();
                    if (height <= 0) {
                        height = defaultHeight;
                    }
                    // Resize the table view cell if an height value is available
                    height = (int) Math.max(DEFAULT_BANNER_HEIGHT, Math.min(MAX_BANNER_HEIGHT, height));
                    resizeBannerCell(height);
                }
            });

        }
    }


    /**
     * Loading an ad.
     * @param domain the ad call domain
     * @param siteId the placement siteID
     * @param pageId the placement pageID
     * @param formatId the placement formatID
     * @param target the placement target
     */
    public void loadAd(String domain, int siteId, String pageId, int formatId, String target) {

        //Checking for ad loaded is not mandatory here, it just prevents us from reloading the ad.
        if (!mIsAdLoaded) {

            SASAdView.setBaseUrl(domain);

            mBanner.loadAd(siteId, pageId, formatId, true, target, new SASAdView.AdResponseHandler() {

                //Create AdResponse Handler.
                //When the ad is loaded, we want to resize it to its optimal height in a recycler view.
                @Override
                public void adLoadingCompleted(SASAdElement sasAdElement) {

                    Runnable resizeRunnable = new Runnable() {
                        @Override
                        public void run() {
                            resizeBannerCell(mBanner.getOptimalHeight());
                        }
                    };

                    mBanner.executeOnUIThread(resizeRunnable);

                    //Comment this line if you want to be able to reload banner (every 2 minutes for example)
                    mIsAdLoaded = true;
                }

                //If ad loading fails, just set the banner height to 0.
                @Override
                public void adLoadingFailed(Exception e) {
                    mBanner.executeOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            resizeBannerCell(0);
                        }
                    });
                }
            });


            mBanner.addStateChangeListener(new SASAdView.OnStateChangeListener() {
                @Override
                public void onStateChanged(SASAdView.StateChangeEvent stateChangeEvent) {
                    if (stateChangeEvent.getType() == SASAdView.StateChangeEvent.VIEW_DEFAULT) {
                        // Fix issue with loader not removed on some KitKat versions (4.4.2 and below)
                        if (android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
                            mBanner.removeLoaderView(mBanner.getLoaderView());
                        }
                    }
                }
            });
        }
    }
}
