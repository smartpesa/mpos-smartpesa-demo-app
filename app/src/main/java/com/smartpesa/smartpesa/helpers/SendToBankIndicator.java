package com.smartpesa.smartpesa.helpers;

import com.wang.avi.Indicator;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayList;

public class SendToBankIndicator extends Indicator {

    public static final float SCALE=1.0f;

    //scale x ,y
    private float[] scaleFloats=new float[]{SCALE, SCALE, SCALE, SCALE, SCALE, SCALE};

    @Override
    public void draw(Canvas canvas, Paint paint) {
        float circleSpacing=50;
        float radius=(Math.min(getWidth(),getHeight())-circleSpacing*2)/12;
        float x = getWidth()/ 2-(radius*2+circleSpacing);
        float y=getHeight() / 2;
        for (int i = 0; i < 5; i++) {
            canvas.save();
            float translateX=x+(radius*2)*i+circleSpacing*i;
            canvas.translate(translateX, y);
            canvas.scale(scaleFloats[i], scaleFloats[i]);
            canvas.drawCircle(0, 0, radius, paint);
            canvas.restore();
        }
    }

    @Override
    public ArrayList<ValueAnimator> onCreateAnimators() {
        ArrayList<ValueAnimator> animators=new ArrayList<>();
        int[] delays=new int[]{100, 200, 300, 400, 500};
        for (int i = 0; i < 5; i++) {
            final int index=i;

            final ValueAnimator scaleAnim=ValueAnimator.ofFloat(0.2f,1,0.2f);

            scaleAnim.setDuration(1000);
            scaleAnim.setRepeatCount(-1);
            scaleAnim.setStartDelay(delays[i]);

            addUpdateListener(scaleAnim,new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    scaleFloats[index] = (float) animation.getAnimatedValue();
                    postInvalidate();
                }
            });
            animators.add(scaleAnim);
        }
        return animators;
    }


}