package com.smartpesa.smartpesa.helpers;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

public class MenuItem {

    public int id;
    @DrawableRes
    public int imgResId;
    @StringRes
    public int textResId;

    public MenuItem(int id, @DrawableRes int imgResId, @StringRes int textResId) {
        this.id = id;
        this.imgResId = imgResId;
        this.textResId = textResId;
    }
}
