package com.smartpesa.smartpesa.util;

import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;

public class ColorUtils {
    @ColorInt
    public static int getColor(Resources resources, @ColorRes int colorResId, @NonNull Resources.Theme theme) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return resources.getColor(colorResId, theme);
        } else {
            return resources.getColor(colorResId);
        }
    }
}
