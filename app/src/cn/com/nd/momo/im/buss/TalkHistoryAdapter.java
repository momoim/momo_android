
package cn.com.nd.momo.im.buss;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import cn.com.nd.momo.im.buss.TextViewUtil.Hit;
import cn.com.nd.momo.im.view.SmileyAdapter;


/**
 * 聊天详情列表聊天记录详细UI展示适配器
 * 
 * @date Nov 25, 2011
 * @author Tsung Wu <tsung.bz@gmail.com>
 */
public class TalkHistoryAdapter {
    public static final String TAG = "TalkHistoryAdapter";

    private static boolean inMatches(ArrayList<Hit> matches, Hit hit) {
        boolean result = false;
        for (Iterator<Hit> itr = matches.iterator(); itr.hasNext();) {
            Hit curr = itr.next();
            if (hit.start >= curr.start && hit.start < curr.end) {
                result = true;
                break;
            }
        }
        return result;
    }

    public static CharSequence addSmileySpans(Context mContext,
            CharSequence text) {
        // 之所以不用stringbuffer是因为这个东西可以将object添加进去
        SpannableStringBuilder builder = new SpannableStringBuilder(text);

        ArrayList<Hit> matches = new ArrayList<Hit>();
        for (int i = 0; i < SmileyAdapter.FilterUbbs.length; i++) {// 循环遍历你所有的表情进行查找替换
            // 正则匹配
            Matcher matcher = Pattern.compile(
                    "\\[" + SmileyAdapter.FilterUbbs[i][1] + "\\]").matcher(
                    text);
            Hit hit;
            while (matcher.find()) {// 查找匹配的类型
                hit = new Hit(matcher.start(), matcher.end());
                if (!inMatches(matches, hit)) {
                    matches.add(hit);
                    int resId = Integer
                            .parseInt(SmileyAdapter.FilterUbbs[i][0]);// 需要替换的图片的资源ID
                    // 哪哪，最重要的是这句话，将文字替换成图片
                    builder.setSpan(new ImageSpan(mContext, resId,
                            ImageSpan.ALIGN_BASELINE), matcher.start(), matcher
                            .end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }

        return builder;
    }
}
