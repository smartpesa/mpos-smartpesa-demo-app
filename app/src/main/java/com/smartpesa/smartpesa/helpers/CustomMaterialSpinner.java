package com.smartpesa.smartpesa.helpers;

import com.smartpesa.smartpesa.R;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class CustomMaterialSpinner extends MaterialBetterSpinner {

    public CustomMaterialSpinner(Context context) {
        super(context);
    }

    public CustomMaterialSpinner(Context arg0, AttributeSet arg1) {
        super(arg0, arg1);
    }

    public CustomMaterialSpinner(Context arg0, AttributeSet arg1, int arg2) {
        super(arg0, arg1, arg2);
    }

    /**
     * Override to fix enable/disable problem.
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isEnabled())
            return super.onTouchEvent(event);
        else return false;
    }

    @Override
    public void setCompoundDrawablesWithIntrinsicBounds(Drawable left, Drawable top, Drawable right, Drawable bottom) {
        Drawable dropdownIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_expand_more_black_18dp);
        if (dropdownIcon != null) {
            right = dropdownIcon;
        }
        super.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
    }
}
