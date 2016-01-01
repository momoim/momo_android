
package cn.com.nd.momo.view;

import java.util.Date;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

import cn.com.nd.momo.R;
import cn.com.nd.momo.api.util.Log;

public class PullToRefreshListView extends ListView implements OnScrollListener {
    private final static String TAG = "PullToRefreshListView";

    private final static int RELEASE_To_REFRESH = 0;

    private final static int PULL_To_REFRESH = 1;

    // 正在刷新
    private final static int REFRESHING = 2;

    // 刷新完成
    private final static int DONE = 3;

    private final static int LOADING = 4;

    // 实际的padding的距离与界面上偏移距离的比例
    private final static int RATIO = 2;

    private LayoutInflater inflater;

    private LinearLayout headView;

    private LinearLayout footView;

    private TextView tipsTextview;

    private TextView lastUpdatedTextView;

    private ImageView arrowImageView;

    private ProgressBar progressBar;

    private RotateAnimation animation;

    private RotateAnimation reverseAnimation;

    private ProgressBar footerProgessBar;

    private TextView footerTextView;

    // 用于保证startY的值在一个完整的touch事件中只被记录一次
    private boolean isRecored;

    private int headContentWidth;

    private int headContentHeight;

    private int startY;

    private int firstItemIndex;

    private int lastItem;

    private int state;

    private boolean isBack;

    private OnRefreshListener refreshListener;

    private OnLoadNextPageListener loadNextPageListener;

    private boolean isRefreshable;

    public final static int PAGE_ITEM_COUNT = 10;

    private int totolItemCount = 0;

    private boolean isAbleLoadNextPage = false;

    private Context mContext;

    int i = 1;

    public PullToRefreshListView(Context context) {
        super(context);
        mContext = context;
        init(context);
    }

    public PullToRefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init(context);
    }

    private void init(Context context) {
        setCacheColorHint(context.getResources().getColor(R.color.transparent));
        inflater = LayoutInflater.from(context);

        headView = (LinearLayout)inflater.inflate(R.layout.custom_list_view_header, null);

        arrowImageView = (ImageView)headView.findViewById(R.id.head_arrowImageView);
        arrowImageView.setMinimumWidth(70);
        arrowImageView.setMinimumHeight(50);
        progressBar = (ProgressBar)headView.findViewById(R.id.head_progressBar);
        tipsTextview = (TextView)headView.findViewById(R.id.head_tipsTextView);
        lastUpdatedTextView = (TextView)headView.findViewById(R.id.head_lastUpdatedTextView);
        measureView(headView);
        headContentHeight = headView.getMeasuredHeight();
        headContentWidth = headView.getMeasuredWidth();

        headView.setPadding(0, -1 * headContentHeight, 0, 0);
        headView.invalidate();

        Log.v("size", "width:" + headContentWidth + " height:" + headContentHeight);
        footView = (LinearLayout)inflater.inflate(R.layout.list_view_footer, null);

        footerProgessBar = (ProgressBar)footView.findViewById(R.id.progressBar_footer);
        footerTextView = (TextView)footView.findViewById(R.id.text_footer);
        addHeaderView(headView, null, false);
        addFooterView(footView, null, false);
        setOnScrollListener(this);

        animation = new RotateAnimation(0, -180,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(250);
        animation.setFillAfter(true);

        reverseAnimation = new RotateAnimation(-180, 0,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        reverseAnimation.setInterpolator(new LinearInterpolator());
        reverseAnimation.setDuration(200);
        reverseAnimation.setFillAfter(true);

        state = DONE;
        isRefreshable = false;
    }

    @Override
    public void addFooterView(View v) {
        super.addFooterView(v);
    }

    public void onScroll(AbsListView arg0, int firstVisiableItem, int visibleItemCount,
            int totalItemCount) {
        firstItemIndex = firstVisiableItem;
        this.totolItemCount = totalItemCount;
        lastItem = firstVisiableItem + visibleItemCount - getHeaderViewsCount();
        Log.i(TAG, "onScroll===lastItem:" + lastItem);
        if (lastItem > getFooterViewsCount() && loadNextPageListener != null && isAbleLoadNextPage) {
            if (lastItem >= totolItemCount - getHeaderViewsCount()) {
                footerProgessBar.setVisibility(View.VISIBLE);
                this.setAbleLoadNextPage(false);
                this.loadNextPageListener.onLoadNextPage();
            } else {
                footerProgessBar.setVisibility(View.GONE);
            }
        }

    }

    public void setFooterText(String text) {
        if (text != null && text.length() > 0) {
            footerTextView.setVisibility(View.VISIBLE);
        } else {
            footerTextView.setVisibility(View.GONE);
        }
        footerTextView.setText(text);
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        Log.d(TAG, "onScrollStateChanged===pageItemCount:" + PAGE_ITEM_COUNT + " lastItem:"
                + lastItem
                + " totolItemCount:" + totolItemCount + " headerviewcount:" + getHeaderViewsCount()
                + " scrollState:" + scrollState
                + " footerviewcount:" + getFooterViewsCount());
    }

    public void forceRefresh(boolean needToast) {
        if (state != REFRESHING) {
            state = REFRESHING;
            changeHeaderViewByState();
            onRefresh(needToast);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (isRefreshable) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (firstItemIndex == 0 && !isRecored) {
                        isRecored = true;
                        startY = (int)event.getY();
                        Log.v(TAG, "在down时候记录当前位置‘");
                    }
                    break;

                case MotionEvent.ACTION_UP:

                    if (state != REFRESHING && state != LOADING) {
                        if (state == DONE) {
                            // 什么都不做
                        }
                        if (state == PULL_To_REFRESH) {
                            state = DONE;
                            changeHeaderViewByState();

                            Log.v(TAG, "由下拉刷新状态，到done状态");
                        }
                        if (state == RELEASE_To_REFRESH) {
                            state = REFRESHING;
                            changeHeaderViewByState();
                            onRefresh(true);
                            Log.v(TAG, "由松开刷新状态，到done状态");
                        }
                    }

                    isRecored = false;
                    isBack = false;

                    break;

                case MotionEvent.ACTION_MOVE:
                    int tempY = (int)event.getY();

                    if (!isRecored && firstItemIndex == 0) {
                        Log.v(TAG, "在move时候记录下位置");
                        isRecored = true;
                        startY = tempY;
                    }

                    if (state != REFRESHING && isRecored && state != LOADING) {

                        // 保证在设置padding的过程中，当前的位置一直是在head，否则如果当列表超出屏幕的话，当在上推的时候，列表会同时进行滚动

                        // 可以松手去刷新了
                        if (state == RELEASE_To_REFRESH) {

                            setSelection(0);

                            // 往上推了，推到了屏幕足够掩盖head的程度，但是还没有推到全部掩盖的地步
                            if (((tempY - startY) / RATIO < headContentHeight)
                                    && (tempY - startY) > 0) {
                                state = PULL_To_REFRESH;
                                changeHeaderViewByState();

                                Log.v(TAG, "由松开刷新状态转变到下拉刷新状态");
                            }
                            // 一下子推到顶了
                            else if (tempY - startY <= 0) {
                                state = DONE;
                                changeHeaderViewByState();

                                Log.v(TAG, "由松开刷新状态转变到done状态");
                            }
                            // 往下拉了，或者还没有上推到屏幕顶部掩盖head的地步
                            else {
                                // 不用进行特别的操作，只用更新paddingTop的值就行了
                            }
                        }
                        // 还没有到达显示松开刷新的时候,DONE或者是PULL_To_REFRESH状态
                        if (state == PULL_To_REFRESH) {

                            setSelection(0);

                            // 下拉到可以进入RELEASE_TO_REFRESH的状态
                            if ((tempY - startY) / RATIO >= headContentHeight) {
                                state = RELEASE_To_REFRESH;
                                isBack = true;
                                changeHeaderViewByState();

                                Log.v(TAG, "由done或者下拉刷新状态转变到松开刷新");
                            }
                            // 上推到顶了
                            else if (tempY - startY <= 0) {
                                state = DONE;
                                changeHeaderViewByState();

                                Log.v(TAG, "由DOne或者下拉刷新状态转变到done状态");
                            }
                        }

                        // done状态下
                        if (state == DONE) {
                            if (tempY - startY > 0) {
                                state = PULL_To_REFRESH;
                                changeHeaderViewByState();
                            }
                        }

                        // 更新headView的size
                        if (state == PULL_To_REFRESH) {
                            headView.setPadding(0, -1 * headContentHeight
                                    + (tempY - startY) / RATIO, 0, 0);

                        }

                        // 更新headView的paddingTop
                        if (state == RELEASE_To_REFRESH) {
                            headView.setPadding(0, (tempY - startY) / RATIO
                                    - headContentHeight, 0, 0);
                        }

                    }

                    break;
            }
        }

        return super.onTouchEvent(event);
    }

    // 当状态改变时候，调用该方法，以更新界面
    private void changeHeaderViewByState() {
        switch (state) {
            case RELEASE_To_REFRESH:
                arrowImageView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                tipsTextview.setVisibility(View.VISIBLE);
                lastUpdatedTextView.setVisibility(View.VISIBLE);

                arrowImageView.clearAnimation();
                arrowImageView.startAnimation(animation);

                tipsTextview.setText(mContext.getString(R.string.release_to_refresh));

                Log.v(TAG, "当前状态，松开刷新");
                break;
            case PULL_To_REFRESH:
                progressBar.setVisibility(View.GONE);
                tipsTextview.setVisibility(View.VISIBLE);
                lastUpdatedTextView.setVisibility(View.VISIBLE);
                arrowImageView.clearAnimation();
                arrowImageView.setVisibility(View.VISIBLE);
                // 是由RELEASE_To_REFRESH状态转变来的
                if (isBack) {
                    isBack = false;
                    arrowImageView.clearAnimation();
                    arrowImageView.startAnimation(reverseAnimation);

                    tipsTextview.setText(mContext.getString(R.string.pull_to_refresh));
                } else {
                    tipsTextview.setText(mContext.getString(R.string.pull_to_refresh));
                }
                Log.v(TAG, "当前状态，下拉刷新");
                break;

            case REFRESHING:

                headView.setPadding(0, 0, 0, 0);

                progressBar.setVisibility(View.VISIBLE);
                arrowImageView.clearAnimation();
                arrowImageView.setVisibility(View.GONE);
                tipsTextview.setText(mContext.getString(R.string.refreshing));
                lastUpdatedTextView.setVisibility(View.VISIBLE);

                Log.v(TAG, "当前状态,正在刷新...");
                break;
            case DONE:
                headView.setPadding(0, -1 * headContentHeight, 0, 0);

                progressBar.setVisibility(View.GONE);
                arrowImageView.clearAnimation();
                arrowImageView.setImageResource(R.drawable.arrow);
                tipsTextview.setText(mContext.getString(R.string.pull_to_refresh));
                lastUpdatedTextView.setVisibility(View.VISIBLE);

                Log.v(TAG, "当前状态，done");
                break;
        }
    }

    public void setonRefreshListener(OnRefreshListener refreshListener) {
        this.refreshListener = refreshListener;
        isRefreshable = true;
    }

    public interface OnRefreshListener {
        public void onRefresh(boolean needToast);
    }

    public void setonLoadNextPageListener(OnLoadNextPageListener loadNextPageListener) {
        this.loadNextPageListener = loadNextPageListener;
    }

    public interface OnLoadNextPageListener {
        public void onLoadNextPage();
    }

    public void onRefreshComplete() {
        state = DONE;
        lastUpdatedTextView.setText(new Date().toLocaleString());
        changeHeaderViewByState();
        Log.v(TAG, "onRefreshComplete() 被调用。。。");
    }
    
    public void onRefreshComplete(CharSequence lastUpdated) {
        state = DONE;
        lastUpdatedTextView.setText(lastUpdated);
        changeHeaderViewByState();
        Log.v(TAG, "onRefreshComplete() 被调用。。。");
    }
    
    private void onRefresh(boolean needToast) {
        if (refreshListener != null) {
            refreshListener.onRefresh(needToast);
        }
    }

    // 此方法直接照搬自网络上的一个下拉刷新的demo，此处是“估计”headView的width以及height
    private void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    public void setAdapter(BaseAdapter adapter) {
        lastUpdatedTextView.setText(new Date().toLocaleString());
        super.setAdapter(adapter);
    }

    public void setAbleLoadNextPage(boolean isable) {
        isAbleLoadNextPage = isable;
        footerProgessBar.setVisibility(View.GONE);
    }

    public boolean getAbleLoadNextPage() {
        return isAbleLoadNextPage;
    }
}
