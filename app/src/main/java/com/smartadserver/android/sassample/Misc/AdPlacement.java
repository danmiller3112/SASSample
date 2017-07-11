package com.smartadserver.android.sassample.Misc;

/**
 * Created by Thomas Geley on 02/06/2016.
 */

public class AdPlacement {

    public int mSiteID;
    public String mPageID;
    public int mFormatID;
    public String mTarget;

    public AdPlacement (int siteId, String pageId, int formatId, String target) {
        mSiteID = siteId;
        mPageID = pageId;
        mFormatID = formatId;
        mTarget = target;
    }

}
