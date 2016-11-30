package com.example.safetysitter.safetysitter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by omeredut on 17/11/2016.
 */

public class SafetySitterPagerAdapter extends FragmentStatePagerAdapter {

    public static int NUMBER_OF_PAGES_IN_VIEW_PAGER;
    public SafetySitterPagerAdapter(FragmentManager supportFragmentManager) {
        super(supportFragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        if (NUMBER_OF_PAGES_IN_VIEW_PAGER == 2) {
            switch (position) {
                case 0:
                    return new ScreenSlideSafetySitterFragmentSecondPage();
                case 1:
                    return new ScreenSlideSafetySitterFragmentThirdPage();
                default:
                    return new ScreenSlideSafetySitterFragmentThirdPage();
            }
        } else {
            switch (position) {
                case 0:
                    return new ScreenSlideSafetySitterFragmentFirstPage();
                case 1:
                    return new ScreenSlideSafetySitterFragmentSecondPage();
                case 2:
                    return new ScreenSlideSafetySitterFragmentThirdPage();
                default:
                    return new ScreenSlideSafetySitterFragmentThirdPage();
            }
        }
    }


    @Override
    public int getCount() {
        return NUMBER_OF_PAGES_IN_VIEW_PAGER;
    }


}
