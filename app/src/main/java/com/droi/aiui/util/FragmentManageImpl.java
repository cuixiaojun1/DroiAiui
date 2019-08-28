package com.droi.aiui.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;

import com.droi.aiui.R;
import com.droi.aiui.ui.BaseFragment;

import java.util.List;

/**
 * author      : cuixiaojun
 * date        : 2017/12/18 10:20
 * description : fragment
 */
public class FragmentManageImpl {

    private String CURR_INDEX = "currIndex";
    private int currIndex = 0;
    private List<String> fragmentTags;
    private IManageFragment iManageFragment;
    private Context context;
    private int lastIndex = 0;

    public FragmentManageImpl(Context context, IManageFragment iManageFragment) {
        this.context = context;
        this.iManageFragment = iManageFragment;
        fragmentTags = iManageFragment.initFragmentTags();
    }

    /**
     *
     * @param outState
     */
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(CURR_INDEX, currIndex);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        currIndex = savedInstanceState.getInt(CURR_INDEX);
    }

    /**
     * fragment
     */
    @SuppressLint("ResourceType")
    public void showFragment(int position) {
        if (fragmentTags == null || fragmentTags.size() == 0 || iManageFragment == null)
            return;
        currIndex = position;
        Fragment fragment = getCurrentFragment();
        if (fragment == null) {
            fragment = iManageFragment.instantFragment(currIndex);
        }

        FragmentManager fragmentManager = ((Activity) context).getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (position == 0) {
            fragmentTransaction.setCustomAnimations(R.anim.slide_left_in, R.anim.slide_left_out);
        } else if (position == 1) {
            if (lastIndex == 0) {
                fragmentTransaction.setCustomAnimations(R.anim.slide_right_in, R.anim.slide_right_out);
            } else if (lastIndex == 2) {
                fragmentTransaction.setCustomAnimations(R.anim.slide_left_in, R.anim.slide_left_out);
            }
        } else if (position == 2) {
            fragmentTransaction.setCustomAnimations(R.anim.slide_right_in, R.anim.slide_right_out);
        }
        for (int i = 0; i < fragmentTags.size(); i++) {
            if (i == currIndex) continue;
            Fragment f = fragmentManager.findFragmentByTag(fragmentTags.get(i));
            if (f != null && f.isAdded()) {
                fragmentTransaction.hide(f);
            }
        }

        if (fragment.isAdded()) {
            fragmentTransaction.show(fragment);
        } else {
            fragmentTransaction.add(iManageFragment.getFragmentContainer(), fragment, fragmentTags.get(currIndex));
        }
        fragmentTransaction.commitAllowingStateLoss();
        fragmentManager.executePendingTransactions();

        lastIndex = currIndex;
    }


    public Fragment getCurrentFragment() {
        if (fragmentTags == null || fragmentTags.size() <= currIndex) {
            return null;
        }
        return ((Activity) context).getFragmentManager().findFragmentByTag(fragmentTags.get(currIndex));
    }


    public interface IManageFragment {
        int getFragmentContainer();   //fragment container

        List<String> initFragmentTags();

        BaseFragment instantFragment(int currIndex);
    }
}