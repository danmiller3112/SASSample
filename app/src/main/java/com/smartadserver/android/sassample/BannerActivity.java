package com.smartadserver.android.sassample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.smartadserver.android.library.SASBannerView;
import com.smartadserver.android.library.model.SASAdElement;
import com.smartadserver.android.library.ui.SASAdView;
import com.smartadserver.android.library.ui.SASRotatingImageLoader;

/**
 * Simple activity featuring a banner ad.
 */

public class BannerActivity extends AppCompatActivity {

    /*****************************************
     * Ad Constants
     *****************************************/
    private final static int SITE_ID = 104808;
    private final static String PAGE_ID = "663262";
    private final static int FORMAT_ID = 15140;
    private final static String TARGET = "";


    /*****************************************
     * Members declarations
     *****************************************/
    // Banner view (as declared in the main.xml layout file, in res/layout)
    SASBannerView mBannerView;

    // Handler class to be notified of ad loading outcome
    SASAdView.AdResponseHandler bannerResponseHandler;

    // Button declared in main.xml
    Button mRefreshBannerButton;


    /**
     * performs Activity initialization after creation
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner);

        //Set Title
        setTitle(R.string.title_activity_banner);

        /*****************************************
         * now perform Ad related code here
         *****************************************/

        // Enable output to Android Logcat (optional)
        SASAdView.enableLogging();

        // Initialize SASBannerView
        initBannerView();

        // Create button to manually refresh the ad
        mRefreshBannerButton = (Button)this.findViewById(R.id.refreshBanner);
        mRefreshBannerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                loadBannerAd();
            }
        });

        // Load Banner ad
        loadBannerAd();

    }

    /**
     * Overriden to clean up SASAdView instances. This must be done to avoid IntentReceiver leak.
     */
    @Override
    protected void onDestroy() {
        mBannerView.onDestroy();
        super.onDestroy();
    }

    /**
     * Initialize the SASBannerView instance of this Activity
     */
    private void initBannerView() {
        // Fetch the SASBannerView inflated from the main.xml layout file
        mBannerView = (SASBannerView)this.findViewById(R.id.banner);

        // Add a loader view on the banner. This view covers the banner placement, to indicate progress, whenever the banner is loading an ad.
        // This is optional
        View loader = new SASRotatingImageLoader(this);
        loader.setBackgroundColor(0x66000000);
        mBannerView.setLoaderView(loader);

        // Instantiate the response handler used when loading an ad on the banner
        bannerResponseHandler = new SASAdView.AdResponseHandler() {
            public void adLoadingCompleted(SASAdElement adElement) {
                Log.i("Sample", "Banner loading completed");
            }

            public void adLoadingFailed(Exception e) {
                Log.i("Sample", "Banner loading failed: " + e.getMessage());
            }
        };
    }

    /**
     * Loads an ad on the banner
     */
    private void loadBannerAd() {
        // Load banner ad with appropriate parameters (siteID,pageID,formatID,master,targeting,adResponseHandler)
        mBannerView.loadAd(BannerActivity.SITE_ID, BannerActivity.PAGE_ID, BannerActivity.FORMAT_ID, true, BannerActivity.TARGET, bannerResponseHandler);
    }

}
