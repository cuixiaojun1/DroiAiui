package com.droi.aiui.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by cuixiaojun on 18-3-22.
 */

public class RemindListView extends ListView {
    public RemindListView(Context context) {
        super(context);
    }

    public RemindListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RemindListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);//Measure specification mode: The child can be as large as it wants up to the specified size.����>����ScrollViewǶ��ListViewֻ��ʾһ�е����⣬�˴���ListView��ռ�Ĵ�С��Ҫ��Ĵ�Сһ����
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}