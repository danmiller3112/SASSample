package com.smartadserver.android.sassample;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import android.os.Bundle;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.smartadserver.android.library.SASBannerView;
import com.smartadserver.android.library.ui.SASAdView;
import com.smartadserver.android.library.ui.SASAdView.OnStateChangeListener;
import com.smartadserver.android.library.ui.SASRotatingImageLoader;
import com.smartadserver.android.library.util.SASUtil;

/**
 * Simple activity containing a ViewPager to swipe between several Fragments
 */
public class SwipeActivity extends AppCompatActivity {


    /*****************************************
     * Ad Constants
     *****************************************/
    private final static String DOMAIN = "http://mobile.smartadserver.com";
    private final static int SITE_ID = 28298;
    private final static String PAGE_ID = "430843";
    private final static int FORMAT_ID = 25090;
    private final static String TARGET = "";


    /*****************************************
     * Members declarations
     *****************************************/
    AppSectionsPagerAdapter mAppSectionsPagerAdapter;
    ViewPager mViewPager;


    /*****************************************
     * Activity
     *****************************************/

    // On Activity Create
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe);

        // Configure SAS Logging
        SASUtil.debugModeEnabled = true;
        SASUtil.enableLogging();

        // Create the adapter that will return a fragment for each of the three primary sections
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager, attaching the adapter
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // if the selected page is an ad Fragment, request that it start its content.
                if (mAppSectionsPagerAdapter.isAdPage(position)) {
                    AdSectionFragment adFragment = (AdSectionFragment)mAppSectionsPagerAdapter.getItem(position);
                    adFragment.startContent();
                }
            }
        });

        // Set up the TabLayout, attaching the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(mViewPager);

    }

    /**
     * Overriden to stop the video when the Activity is paused
     */
    public void onPause() {
        super.onPause();
        int page = mViewPager.getCurrentItem();
        if (mAppSectionsPagerAdapter.isAdPage(page)) {
            AdSectionFragment adFragment = (AdSectionFragment)mAppSectionsPagerAdapter.getItem(page);
            adFragment.stopContent();
        }
    }

    /**
     * Overriden to restart the video when the Activity is resumed on an ad page.
     */
    public void onResume() {
        super.onResume();
        int page = mViewPager.getCurrentItem();
        if (mAppSectionsPagerAdapter.isAdPage(page)) {
            AdSectionFragment adFragment = (AdSectionFragment)mAppSectionsPagerAdapter.getItem(page);
            adFragment.startContent();
        }
    }

    /*****************************************
     * PagerAdapter and Fragments
     *****************************************/

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        // declare page count
        int pageCount = 5;

        // buffer for created Fragments
        Fragment[] fragments = new Fragment[pageCount];

        // declare ad pages indices
        Integer[] adIndices = {2};
        HashSet<Integer> adIndicesSet;


        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            // init indices for ad views
            adIndicesSet = new HashSet<Integer>((Collection<Integer>)Arrays.asList(adIndices));
        }

        public boolean isAdPage(int page) {
            return adIndicesSet.contains(page);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment item = null;

            if (fragments[i] != null) {
                // return previously created fragment
                item = fragments[i];
            } else {
                // create a new one
                if (isAdPage(i)) {
                    // this is an Ad view, create an AdSectionFragment
                    item = new AdSectionFragment();
                } else {
                    // The other sections of the app are dummy content fragments.
                    Fragment fragment = new ContentSectionFragment();
                    Bundle args = new Bundle();
                    args.putInt(ContentSectionFragment.ARG_SECTION_NUMBER, i + 1);
                    fragment.setArguments(args);
                    item = fragment;
                }

                // store created fragment
                fragments[i] = item;
            }

            return item;
        }

        @Override
        public int getCount() {
            return pageCount;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = null;
            if (isAdPage(position)) {
                // this is an Ad view
                title = "Advertising";
            } else {
                // The other sections of the app are dummy placeholders.
                title = "Section " + (position + 1);
            }
            return title;
        }
    }

    /**
     * A fragment that displays ads.
     */
    public static class AdSectionFragment extends Fragment {

        public SASBannerView banner;
        View rootView;

        boolean startRequired = false;
        boolean bannerLoaded = false;



        /**
         * Method overriden to be notified when the Fragment becomes visible or gets hidden
         */
        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (!isVisibleToUser) {
                // when this view is outside the screen, send a message to the adview that it should stop playing any content.
                // note that this method is available from SAS SDK 4.2 and above. Needs to be commented out otherwise.
                stopContent();
            }
        }

        /**
         * Send a message to the creative to stop any playing content.
         */
        public synchronized void stopContent() {
            if (banner != null) {
                banner.sendMessageToWebView("stop");
            }
        }

        /**
         * Send a message to the creative to start any playable content as soon as possible (might need to wait for banner to
         * be ready. see OnStateChangeListener below)
         */
        public synchronized void startContent() {
            if (bannerLoaded) {
                doStartContent();
            } else {
                startRequired = true;
            }
        }


        private void doStartContent() {
            if (banner != null) {
                banner.sendMessageToWebView("start");
            }
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            rootView = inflater.inflate(R.layout.fragment_swipe_ad, container, false);
            banner =  (SASBannerView)rootView.findViewById(R.id.banner);
            banner.setLoaderView(new SASRotatingImageLoader(getActivity()));

            // add a state change listener on the banner to know when the creative is ready to react to the 'start' message.
            banner.addStateChangeListener(new OnStateChangeListener() {

                public void onStateChanged(SASAdView.StateChangeEvent stateChangeEvent) {
                    synchronized (AdSectionFragment.this) {
                        // listen for the VIEW_DEFAULT state change that indicates that the creative is loaded and ready.
                        if (stateChangeEvent.getType() == SASAdView.StateChangeEvent.VIEW_DEFAULT) {
                            bannerLoaded = true;
                            if (startRequired) {
                                // if start content was called before, start content now.
                                startRequired = false;
                                doStartContent();
                            }
                        }
                    }
                }
            });

            // reset 2 internal flags, and load ad
            banner.loadAd(SITE_ID, PAGE_ID, FORMAT_ID, true, TARGET);

            return rootView;
        }


        @Override
        public void onDestroyView() {
            super.onDestroyView();

            if (banner != null) {
                banner.onDestroy();
            }
        }
    }

    /**
     * A dummy fragment representing a content section of the app, but that simply displays dummy text.
     */
    public static class ContentSectionFragment extends Fragment {

        public static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_swipe_section, container, false);
            Bundle args = getArguments();
            ((TextView) rootView.findViewById(android.R.id.text1)).setText(
                    getString(R.string.swipe_section_text, args.getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

}