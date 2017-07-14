package cn.edu.swufe.fife.professor;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by wing on 12/4/16.
 */

public class ZoomOutViewPager extends ViewPager {
    public boolean canScroll = true;

    public ZoomOutViewPager(Context context) {
        super(context);
    }

    public ZoomOutViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPageTransformer(true, new ZoomOutPageTransformer());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return
                canScroll &&
                        super.onTouchEvent(event);
    }


    //改变系统绘制顺序
    @Override
    protected int getChildDrawingOrder(int childCount, int i) {

        int position = getCurrentItem();
        if (position < 0) {
            return i;
        } else {
            if (i == childCount - 1) {//这是最后一个需要刷新的item
                if (position > i) {
                    position = i;
                }
                return position;
            }
            if (i == position) {//这是原本要在最后一个刷新的item
                return childCount - 1;
            }
        }
        return i;
    }
}
