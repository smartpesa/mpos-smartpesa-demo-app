package com.smartpesa.smartpesa.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import timber.log.Timber;

public class Navigator {
    private final Context mContext;
    private final int mViewResId;

    public Navigator(Context context, int viewResId) {
        mContext = context;
        mViewResId = viewResId;
    }

    public <T extends Fragment> T pushFragment(Class<T> fragmentClass) {
        return pushFragment(fragmentClass, null, null);
    }

    public <T extends Fragment> T pushFragment(Class<T> fragmentClass, @NonNull String backStackName) {
        return pushFragment(fragmentClass, null, backStackName);
    }

    public <T extends Fragment> T pushFragment(Class<T> fragmentClass, @NonNull Bundle bundle) {
        return pushFragment(fragmentClass, bundle, null);
    }

    public <T extends Fragment> T pushFragment(Class<T> fragmentClass, @Nullable Bundle bundle, @Nullable String backStackName) {
        Fragment fragment = Fragment.instantiate(mContext, fragmentClass.getName(), bundle);
        pushFragment(fragment, backStackName);
        return (T) fragment;
    }

    public void pushFragment(Fragment fragment) {
        pushFragment(fragment, null);
    }

    public void pushFragment(Fragment fragment, @Nullable String backStackName) {
        if (mContext instanceof AppCompatActivity) {
            FragmentManager manager = ((AppCompatActivity) mContext).getSupportFragmentManager();

            FragmentTransaction fragmentTransaction = manager.beginTransaction();
            fragmentTransaction.replace(mViewResId, fragment, fragment.getClass().getName());
            fragmentTransaction.addToBackStack(backStackName);
            try {
                fragmentTransaction.commitAllowingStateLoss();
            } catch (Exception e) {
                Timber.e(e, "Failed to commit fragment");
            }
        } else {
            throw new RuntimeException("Invalid Activity class, Activity must extends AppCompatActivity");
        }
    }

    public <T extends Activity> void pushActivity(Class<T> activityClass) {
        Intent i = new Intent(mContext, activityClass);
        mContext.startActivity(i);
    }
}
