package com.dus.flowlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dusheng on 2017/3/23.
 */
public class FlowView extends ViewGroup {

    private Context context;
    public FlowView(Context context) {
        this(context,null);
    }

    public FlowView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public FlowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    /**
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 获取宽高及其测量模式
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // wrap_content下的宽和高
        int width = 0;
        int height = 0;
        // 记录每一行的宽度和高度
        int lineWidth = 0;
        int lineHeight = 0;
        int cCount = getChildCount();
        for(int i=0;i<cCount;i++){
            View child = getChildAt(i);
            // 测量子view占据的宽和高
            measureChild(child,widthMeasureSpec,heightMeasureSpec);
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            int childWidth = child.getMeasuredWidth()+lp.leftMargin+lp.rightMargin;
            int childHeight = child.getMeasuredHeight()+lp.topMargin+lp.bottomMargin;

            // 换行处理
            if(lineWidth+childWidth>sizeWidth-getPaddingLeft()-getPaddingRight()){// 需要换行
                    // viewGroup的宽度
                    width = Math.max(width,lineWidth);
                    // 重置line Width
                    lineWidth = childWidth;
                    // viewGroup的高度
                    height += lineHeight;
                    lineHeight = childHeight;
            }else{// 不需换行
               lineWidth+=childWidth;
               lineHeight = Math.max(lineHeight,childHeight);
            }

            if(i==cCount-1){// 最后一个子view
                width = Math.max(lineWidth,width);
                height += lineHeight;
            }
        }

        setMeasuredDimension(widthMode==MeasureSpec.EXACTLY?sizeWidth:width+getPaddingLeft()+getPaddingRight(),
                            heightMode==MeasureSpec.EXACTLY?sizeHeight:height+getPaddingTop()+getPaddingBottom()
                );
    }

    // 存放所有的子view 每个位置对应一行元素
    private List<List<View>> mAllViews = new ArrayList<>();
    // 存放每一行的高度
    private List<Integer> mLineHeight = new ArrayList<>();
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        // 初始化
        mAllViews.clear();
        mLineHeight.clear();

        // 1. 遍历元素存入集合中

        // 获取容器宽度
        int width= getWidth();
        Log.i("FlowView","width----------"+width);

        int lineWidth = 0;
        int lineHeight = 0;
        // 存放每一行的view
        List<View> lineViews = new ArrayList<>();

        int cCount = getChildCount();
        for(int i=0;i<cCount;i++){
            View child = getChildAt(i);
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            // 获取子view的宽高
            int childWidth = child.getMeasuredWidth()+lp.leftMargin+lp.rightMargin;
            int childHeight = child.getMeasuredHeight()+lp.topMargin+lp.bottomMargin;
            // 换行
            if(lineWidth + childWidth>width){
                mAllViews.add(lineViews);
                mLineHeight.add(childHeight);
                lineWidth = 0;
                // 重置lineViews
                lineViews = new ArrayList<>();
            }
            // 不需要换行
            lineWidth += childWidth;
            lineHeight = Math.max(childHeight,lineHeight);
            lineViews.add(child);
        }

        // 最后一行
        // 此处特殊处理的原因：不需要换行时，前面并没有将当前lineView加入集合中
        // 需要换行时，加入集合的是上一行的lineViews
        mAllViews.add(lineViews);
        mLineHeight.add(lineHeight);
        // 2. 遍历mAllViews 获取left、 top、right、bottom
        int left = 0;
        int top = 0;
        for(int i=0;i<mAllViews.size();i++){
            // 每一行的所有元素
            lineViews = mAllViews.get(i);
            // 当前行的行高
            lineHeight = mLineHeight.get(i);
            // 遍历每一行的子元素
            for(int j=0;j<lineViews.size();j++){
                View child = lineViews.get(j);
                if(child.getVisibility()==View.GONE){
                    continue;
                }
                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                // 计算view 的 left top right bottom
                int cl = left + lp.leftMargin;
                int ct = top + lp.topMargin;
                int cr = child.getMeasuredWidth()+cl;
                int cb = ct+child.getMeasuredHeight();
                child.layout(cl,ct,cr,cb);
                // 重置left top
                left += child.getMeasuredWidth()+lp.leftMargin+lp.rightMargin;
            }
            left = 0;
            top += lineHeight;
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(),attrs);
    }

//    // 刷新view
//    public void setData(List<String> datas){
//        TextView tv;
//        for(int i=0;i<datas.size();i++){
//            tv = new TextView(context);
//            tv.setText(datas.get(i));
//            this.addView(tv);
//        }
//        postInvalidate();
//    }
}
