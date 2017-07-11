package com.smartadserver.android.sassample;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.smartadserver.android.library.SASInterstitialView;
import com.smartadserver.android.library.model.SASAdElement;
import com.smartadserver.android.library.ui.SASAdView;
import com.smartadserver.android.library.ui.SASRotatingImageLoader;

public class InterstitialActivity extends AppCompatActivity {

    /*****************************************
     * Ad Constants
     *****************************************/
    private final static int SITE_ID = 104808;
    private final static String PAGE_ID = "663264";
    private final static int FORMAT_ID = 12167;
    private final static String TARGET = "";

    /*****************************************
     * Members declarations
     *****************************************/
    // Interstitial view (this view is not part of any xml layout file)
    SASInterstitialView mInterstitialView;

    // Handler classe to be notified of ad loading outcome
    SASAdView.AdResponseHandler interstitialResponseHandler;

    // Button declared in main.xml
    Button mDisplayInterstitialButton;


    /**
     * performs Activity initialization after creation
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interstitial);

        /*****************************************
         * Now perform Ad related code here
         *****************************************/
        // Enable output to Android Logcat (optional)
        SASAdView.enableLogging();

        // Initialize SASInterstitialView
        initInterstitialView();

        // Create button to manually refresh interstitial
        mDisplayInterstitialButton = (Button)this.findViewById(R.id.loadInterstitial);
        mDisplayInterstitialButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                loadInterstitialAd();
            }
        });

    }

    /**
     * Overriden to clean up SASAdView instances. This must be done to avoid IntentReceiver leak.
     */
    @Override
    protected void onDestroy() {
        mInterstitialView.onDestroy();
        super.onDestroy();
    }


    /**
     * initialize the SASInterstitialView instance of this Activity
     */
    private void initInterstitialView() {

        // Create SASInterstitialView instance
        mInterstitialView = new SASInterstitialView(this);

        // Add a loader view on the interstitial view. This view is displayed fullscreen, to indicate progress,
        // whenever the interstitial is loading an ad.
        View loader = new SASRotatingImageLoader(this);
        loader.setBackgroundColor(Color.WHITE);
        mInterstitialView.setLoaderView(loader);

        // Add a state change listener on the SASInterstitialView instance to monitor MRAID states changes.
        // Useful for instance to perform some actions as soon as the interstitial disappears.
        mInterstitialView.addStateChangeListener(new SASAdView.OnStateChangeListener() {
            public void onStateChanged(SASAdView.StateChangeEvent stateChangeEvent) {
                switch(stateChangeEvent.getType()) {
                    case SASAdView.StateChangeEvent.VIEW_DEFAULT:
                        // the MRAID Ad View is in default state
                        Log.i("Sample", "Interstitial MRAID state : DEFAULT");
                        break;
                    case SASAdView.StateChangeEvent.VIEW_EXPANDED:
                        // the MRAID Ad View is in expanded state
                        Log.i("Sample", "Interstitial MRAID state : EXPANDED");
                        break;
                    case SASAdView.StateChangeEvent.VIEW_HIDDEN:
                        // the MRAID Ad View is in hidden state
                        Log.i("Sample", "Interstitial MRAID state : HIDDEN");
                        break;
                }
            }
        });

        // Instantiate the response handler used when loading an interstitial ad
        interstitialResponseHandler = new SASAdView.AdResponseHandler() {
            public void adLoadingCompleted(SASAdElement adElement) {
                Log.i("Sample", "Interstitial loading completed");
            }

            public void adLoadingFailed(Exception e) {
                Log.i("Sample", "Interstitial loading failed: " + e.getMessage());
            }
        };
    }

    /**
     * Loads an interstitial ad
     */
    private void loadInterstitialAd() {
        // Load interstitial ad with appropriate parameters (siteID,pageID,formatID,master,targeting,adResponseHandler)
        mInterstitialView.loadAd(InterstitialActivity.SITE_ID, InterstitialActivity.PAGE_ID, InterstitialActivity.FORMAT_ID, true, InterstitialActivity.TARGET, interstitialResponseHandler);
    }

}
