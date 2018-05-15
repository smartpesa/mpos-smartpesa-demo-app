package com.smartpesa.smartpesa.activity;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.util.Navigator;

import dagger.Module;
import dagger.Provides;

@Module
public class MainActivityModule {

    private MainActivity mMainActivity;

    public MainActivityModule(MainActivity mainActivity) {
        mMainActivity = mainActivity;
    }

    @Provides
    @MainActivityScope
    public Navigator provideNavigator() {
        return new Navigator(mMainActivity, R.id.container_body);
    }
}
