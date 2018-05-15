package com.smartpesa.smartpesa.activity;

import com.smartpesa.smartpesa.util.Navigator;

import dagger.Subcomponent;

@MainActivityScope
@Subcomponent(
        modules = {
                MainActivityModule.class
        }
)
public interface MainActivityComponent {
    Navigator provideNavigator();
}
