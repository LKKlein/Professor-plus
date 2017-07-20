package cn.edu.swufe.fife.professor;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by wing on 12/4/16.
 */

public class ZoomHeaderView extends LinearLayout {

    private float mTouchSlop;
    private float iDownY;
    private ZoomOutViewPager mViewPager;
    private float mFirstY;
    private TextView mCloseTxt;

    private final int ANIMATE_LENGTH = 300;

    public ZoomHeaderView(Context context) {
        super(context);
    }

    public ZoomHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mViewPager = (ZoomOutViewPager) getChildAt(1);
        mFirstY = getY();
        mCloseTxt = (TextView) findViewById(R.id.tv_close);
    }

    public ZoomHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_MOVE:

                float moveY = ev.getY() - iDownY;
                float currentY = getY();

                //向上滑动viewpager整体移动
                if (currentY + moveY < 0 && currentY + moveY > -getHeight() / 4) {
                    doPagerUp(moveY, currentY);
                }

                //向下移动
                if (currentY + moveY > 0 && currentY + moveY < 1200) {
                    doPagerDown(moveY, currentY);
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:

                float upY = ev.getY() - iDownY;
                float currentUpY = getY();
                //超过阀值 结束Activity
                if (upY + currentUpY > 280) {
                    finish();
                }

                //不在任何阀值  恢复
                if (currentUpY + upY < 280) {
                    if (currentUpY + upY < -getHeight() / 4) {
                        restore(-getHeight() / 4);
                    } else if (currentUpY + upY > 0) {
                        restore((upY + currentUpY) / 4);
                    } else {
                        restore(upY + currentUpY);
                    }
                }

                return true;
        }
        return super.onTouchEvent(ev);
    }

    private void doPagerDown(float moveY, float currentY) {
        int pos = mViewPager.getCurrentItem();
        View v = mViewPager.getChildAt(pos);
        v.setTranslationY((currentY + moveY) / 4);
        mCloseTxt.setAlpha(v.getY() / 76);
    }

    private void doPagerUp(float moveY, float currentY) {
        setTranslationY(currentY + moveY);
        mCloseTxt.setAlpha(0f);
    }

    public void restore(float y) {
        mCloseTxt.setAlpha(0f);
        if (y > mFirstY) {
            ValueAnimator closeVa = ValueAnimator.ofFloat(1, 0);
            closeVa.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mCloseTxt.setAlpha((Float) animation.getAnimatedValue());
                }
            });
            closeVa.setDuration(ANIMATE_LENGTH);
            closeVa.start();
        }

        ValueAnimator restoreVa = ValueAnimator.ofFloat(y, mFirstY);
        restoreVa.setInterpolator(new DecelerateInterpolator());
        restoreVa.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float y = (float) animation.getAnimatedValue();
                if (y < 0) {
                    setTranslationY(y);
                } else {
                    int pos = mViewPager.getCurrentItem();
                    View v = mViewPager.getChildAt(pos);
                    v.setTranslationY(y);
                }
                mViewPager.canScroll = true;
            }
        });
        restoreVa.setDuration(ANIMATE_LENGTH);
        restoreVa.start();
    }

    private void finish() {
        TranslateAnimation finishTa = new TranslateAnimation(0, 0, 0, 1000);
        finishTa.setDuration(ANIMATE_LENGTH);
        finishTa.setFillAfter(true);
        finishTa.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ((Activity) getContext()).finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        startAnimation(finishTa);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                iDownY = (int) ev.getY();

                break;
            case MotionEvent.ACTION_MOVE:
                int moveY = (int) ev.getY();
                if (Math.abs(moveY - iDownY) > mTouchSlop) {

                    return true;
                }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public ZoomOutViewPager getViewPager() {
        return mViewPager;
    }
}
