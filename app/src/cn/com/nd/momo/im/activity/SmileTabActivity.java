
package cn.com.nd.momo.im.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityGroup;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import cn.com.nd.momo.R;
import cn.com.nd.momo.im.view.SmileyAdapter;
import cn.com.nd.momo.im.view.SmileyAdapter.OnSmileyListener;
import cn.com.nd.momo.im.view.SmileyGrid;
import cn.com.nd.momo.view.widget.DragableSpace;

public class SmileTabActivity extends ActivityGroup implements OnSmileyListener {

    TabHost tabHost;

    SmileyAdapter.OnSmileyListener smileyListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(R.layout.smiley_tab_activity);

        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        int viewWidth = Math.min(outMetrics.widthPixels, outMetrics.heightPixels);
        android.view.WindowManager.LayoutParams p = getWindow().getAttributes();
        p.width = viewWidth - 50;
        // p.width = 430;
        getWindow().setAttributes(p);

        tabHost = (TabHost)findViewById(R.id.smiley_tabhost);
        tabHost.setup(this.getLocalActivityManager());

        Resources res = getResources();

        addSmileyTab(R.id.smile_default, SmileyAdapter.TYPE_NORMAL,
                SmileyAdapter.FilterUbbs, "default",
                res.getDrawable(R.drawable.smiley_tab_bg_left), null,
                res.getString(R.string.txt_smiley_default));

        addSmileyTab(R.id.smile_face, SmileyAdapter.TYPE_EjMOJI,
                SmileyAdapter.EMOJI_SMILE[0], "face",
                res.getDrawable(R.drawable.smiley_tab_bg_middle),
                res.getDrawable(R.drawable.smiley_tab_face), null);

        addSmileyTab(R.id.smile_flower, SmileyAdapter.TYPE_EjMOJI,
                SmileyAdapter.EMOJI_SMILE[1], "flower",
                res.getDrawable(R.drawable.smiley_tab_bg_middle),
                res.getDrawable(R.drawable.smiley_tab_flower), null);

        addSmileyTab(R.id.smile_ring, SmileyAdapter.TYPE_EjMOJI,
                SmileyAdapter.EMOJI_SMILE[2], "ring",
                res.getDrawable(R.drawable.smiley_tab_bg_middle),
                res.getDrawable(R.drawable.smiley_tab_ring), null);

        addSmileyTab(R.id.smile_car, SmileyAdapter.TYPE_EjMOJI,
                SmileyAdapter.EMOJI_SMILE[3], "car",
                res.getDrawable(R.drawable.smiley_tab_bg_middle),
                res.getDrawable(R.drawable.smiley_tab_car), null);

        addSmileyTab(R.id.smile_sign, SmileyAdapter.TYPE_EjMOJI,
                SmileyAdapter.EMOJI_SMILE[4], "sign",
                res.getDrawable(R.drawable.smiley_tab_bg_right),
                res.getDrawable(R.drawable.smiley_tab_sign), null);

        // 设置"默认"标签文字颜色
        final TabWidget tabWidget = tabHost.getTabWidget();
        FrameLayout viewRoot = (FrameLayout)tabWidget.getChildTabViewAt(0);
        TextView view = (TextView)viewRoot.findViewById(R.id.tab_textView);
        view.setTextColor(res.getColorStateList(R.drawable.smiley_tab_default));

    }

    private void addSmileyTab(int contentResourceID, byte smileType, String smile[][],
            String tabTag,
            Drawable backGroundDrawable, Drawable icon,
            String strText) {

        View inflateView = LayoutInflater.from(this).inflate(R.layout.tabs_layout,
                tabHost.getTabContentView());
        LinearLayout view = (LinearLayout)inflateView.findViewById(contentResourceID);
        final DragableSpace dragableSpace = (DragableSpace)view.getChildAt(0);
        final LinearLayout pagerSwitcher = (LinearLayout)view.getChildAt(1);

        final List<View> mListViews = new ArrayList<View>();

        int emojiSmileLength = smile.length;

        byte page = (byte)(emojiSmileLength %
                SmileyAdapter.SMILE_COUNT_PER_PAGE == 0 ? emojiSmileLength
                / SmileyAdapter.SMILE_COUNT_PER_PAGE
                : emojiSmileLength / SmileyAdapter.SMILE_COUNT_PER_PAGE + 1);

        int index = 0;
        for (int i = 0; i < page; i++) {
            SmileyGrid mSmileyGrid = new SmileyGrid(this);
            mSmileyGrid.setOnSmileyListener(this);
            final SmileyAdapter adapter = new SmileyAdapter(this);
            adapter.setSmileType(smileType);
            adapter.setIndex(index);
            adapter.setLength(emojiSmileLength - index >=
                    SmileyAdapter.SMILE_COUNT_PER_PAGE ?
                            SmileyAdapter.SMILE_COUNT_PER_PAGE
                            : emojiSmileLength % SmileyAdapter.SMILE_COUNT_PER_PAGE);
            adapter.setSmile(smile);
            mSmileyGrid.setAdapter(adapter);
            index += SmileyAdapter.SMILE_COUNT_PER_PAGE;
            mListViews.add(mSmileyGrid);
            dragableSpace.addView(mSmileyGrid);
        }
        dragableSpace.setOnScreenChangedListener(new DragableSpace.OnScreenChangedListener() {

            @Override
            public void onScreenChanged(int curScreen, int preScreen) {
                // TODO Auto-generated method stub
                ((ImageView)pagerSwitcher.getChildAt(preScreen))
                        .setImageResource(R.drawable.point_normal);
                ((ImageView)pagerSwitcher.getChildAt(curScreen))
                        .setImageResource(R.drawable.point_choose);

            }
        });

        // 页面选择器
        // 可以使用radiogroup实现更简单
        for (int i = 0; i < page; i++) {
            ImageView pageItem = new ImageView(this);
            pageItem.setTag(new Integer(i));
            LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
            lp.leftMargin = 4;
            lp.rightMargin = 4;
            pageItem.setLayoutParams(lp);
            if (i == 0) {
                pageItem.setImageResource(R.drawable.point_choose);
            } else {
                pageItem.setImageResource(R.drawable.point_normal);
            }
            pageItem.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    if (dragableSpace.isScrollFinished()) {
                        dragableSpace.snapToScreen(((Integer)v.getTag()).intValue());
                    }
                    // ((ImageView)v).setImageResource(R.drawable.point_choose);
                    // View child = null;
                    // for (int j = 0; j < pagerSwitcher.getChildCount(); j++) {
                    // child = pagerSwitcher.getChildAt(j);
                    // if (!child.equals(v)) {
                    // ((ImageView)child).setImageResource(R.drawable.point_normal);
                    // }
                    // }
                }
            });
            pagerSwitcher.addView(pageItem);
        }

        // 添加tab页
        tabHost.addTab(tabHost
                .newTabSpec(tabTag)
                .setIndicator(inflaterTab(backGroundDrawable, icon, strText))
                .setContent(contentResourceID));

        // ViewPager实现
        /*
         * View inflateView =
         * LayoutInflater.from(this).inflate(R.layout.tabs_layout,
         * tabHost.getTabContentView()); LinearLayout view =
         * (LinearLayout)inflateView.findViewById(contentResourceID); final
         * ViewPager viewPager = (ViewPager)view.getChildAt(0); final
         * LinearLayout pagerSwitcher = (LinearLayout)view.getChildAt(1); final
         * List<View> mListViews = new ArrayList<View>(); int emojiSmileLength =
         * smile.length; byte page = (byte)(emojiSmileLength %
         * SmileyAdapter.SMILE_COUNT_PER_PAGE == 0 ? emojiSmileLength /
         * SmileyAdapter.SMILE_COUNT_PER_PAGE : emojiSmileLength /
         * SmileyAdapter.SMILE_COUNT_PER_PAGE + 1); int index = 0; for (int i =
         * 0; i < page; i++) { SmileyGrid mSmileyGrid = new SmileyGrid(this);
         * mSmileyGrid.setOnSmileyListener(this); final SmileyAdapter adapter =
         * new SmileyAdapter(this); adapter.setSmileType(smileType);
         * adapter.setIndex(index); adapter.setLength(emojiSmileLength - index
         * >= SmileyAdapter.SMILE_COUNT_PER_PAGE ?
         * SmileyAdapter.SMILE_COUNT_PER_PAGE : emojiSmileLength %
         * SmileyAdapter.SMILE_COUNT_PER_PAGE); adapter.setSmile(smile);
         * mSmileyGrid.setAdapter(adapter); mListViews.add(mSmileyGrid); index
         * += SmileyAdapter.SMILE_COUNT_PER_PAGE; }
         * viewPager.setOnPageChangeListener(new OnPageChangeListener() {
         * @Override public void onPageSelected(int curPageNum) { // TODO
         * Auto-generated method stub ImageView pageItem = null; for (int i = 0;
         * i < pagerSwitcher.getChildCount(); i++) { pageItem =
         * (ImageView)pagerSwitcher.getChildAt(i); int prePageNum =
         * ((Integer)pageItem.getTag()).intValue(); if (prePageNum !=
         * curPageNum) { pageItem.setImageResource(R.drawable.point_normal); }
         * else { pageItem.setImageResource(R.drawable.point_choose); } } }
         * @Override public void onPageScrolled(int arg0, float arg1, int arg2)
         * { // TODO Auto-generated method stub }
         * @Override public void onPageScrollStateChanged(int arg0) { // TODO
         * Auto-generated method stub } }); viewPager.setAdapter(new
         * PagerAdapter() {
         * @Override public void startUpdate(View arg0) { // TODO Auto-generated
         * method stub }
         * @Override public Parcelable saveState() { // TODO Auto-generated
         * method stub return null; }
         * @Override public void restoreState(Parcelable arg0, ClassLoader arg1)
         * { // TODO Auto-generated method stub }
         * @Override public boolean isViewFromObject(View arg0, Object arg1) {
         * // TODO Auto-generated method stub return arg0 == arg1; }
         * @Override public Object instantiateItem(View collection, int
         * position) { // TODO Auto-generated method stub
         * ((ViewPager)collection).addView(mListViews.get(position), 0); return
         * mListViews.get(position); }
         * @Override public int getCount() { // TODO Auto-generated method stub
         * return mListViews.size(); }
         * @Override public void finishUpdate(View arg0) { // TODO
         * Auto-generated method stub }
         * @Override public void destroyItem(View collection, int position,
         * Object arg2) { // TODO Auto-generated method stub
         * ((ViewPager)collection).removeView(mListViews.get(position)); } });
         * // 页面选择器 for (int i = 0; i < page; i++) { ImageView pageItem = new
         * ImageView(this); pageItem.setTag(new Integer(i)); LayoutParams lp =
         * new LayoutParams(LayoutParams.WRAP_CONTENT,
         * LayoutParams.WRAP_CONTENT); lp.leftMargin = 4; lp.rightMargin = 4;
         * pageItem.setLayoutParams(lp); if (i == 0) {
         * pageItem.setImageResource(R.drawable.point_choose); } else {
         * pageItem.setImageResource(R.drawable.point_normal); }
         * pageItem.setOnClickListener(new OnClickListener() {
         * @Override public void onClick(View v) { // TODO Auto-generated method
         * stub viewPager.setCurrentItem(((Integer)v.getTag()).intValue());
         * ((ImageView)v).setImageResource(R.drawable.point_choose); View child
         * = null; for (int j = 0; j < pagerSwitcher.getChildCount(); j++) {
         * child = pagerSwitcher.getChildAt(j); if (!child.equals(v)) {
         * ((ImageView)child).setImageResource(R.drawable.point_normal); } } }
         * }); pagerSwitcher.addView(pageItem); } // 添加tab页
         * tabHost.addTab(tabHost .newTabSpec(tabTag)
         * .setIndicator(inflaterTab(backGroundDrawable, icon, strText))
         * .setContent(contentResourceID));
         */}

    private View inflaterTab(Drawable backGroundDrawable, Drawable icon, String strText) {
        View view = LayoutInflater.from(this).inflate(R.layout.tab_item_layout, null);
        ViewGroup layout = (ViewGroup)view.findViewById(R.id.tab_bar_layout);
        layout.setBackgroundDrawable(backGroundDrawable);
        ImageView imageView = (ImageView)view.findViewById(R.id.tab_imageView);
        LayoutParams lp = (LayoutParams)imageView.getLayoutParams();
        lp.topMargin = 0;
        imageView.setLayoutParams(lp);
        if (icon == null) {
            imageView.setVisibility(View.GONE);
        } else {
            imageView.setImageDrawable(icon);
        }
        TextView textView = (TextView)view.findViewById(R.id.tab_textView);
        if (strText == null) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setText(strText);
            textView.setTextSize(16);
        }

        return view;
    }

    @Override
    public void onSelect(String smiley) {
        // TODO Auto-generated method stub
        Intent data = new Intent();
        data.putExtra("smiley", smiley);
        setResult(RESULT_OK, data);
        finish();
    }
}
