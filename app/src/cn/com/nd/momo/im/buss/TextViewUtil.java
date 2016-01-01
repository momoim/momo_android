
package cn.com.nd.momo.im.buss;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.util.Linkify;
import android.view.View;
import android.widget.TextView;
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.api.util.Regex;
import cn.com.nd.momo.im.view.SmileyAdapter;

/**
 * 文本处理辅助类
 * 
 * @author Tsung Wu <tsung.bz@gmail.com>
 */

public class TextViewUtil {
    private static final String TAG = "TextViewUtil";

    public static void setLinkSpans(TextView textView, CharSequence text, boolean isEditing) {
        /*
         * Linkify.WEB_URLS | 特殊处理，优先用海豚打开
         */
        if (isEditing) {
            textView.setText(addSmileySpans(textView.getContext(), text));
        } else {
            textView.setText(addUrlSpans(textView.getContext(),
                    addSmileySpans(textView.getContext(), text)));
            Linkify.addLinks(textView, Linkify.EMAIL_ADDRESSES
                    | Linkify.MAP_ADDRESSES | Linkify.PHONE_NUMBERS);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    public static void setLinkSpans(TextView textView, CharSequence text) {
        setLinkSpans(textView, text, false);
    }

    /**
     * 添加url点击事件拦截
     * 
     * @param mContext
     * @param text
     * @return
     */
    public static CharSequence addUrlSpans(final Context context, CharSequence text) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        Matcher matcher = Regex.WEB_URL_PATTERN.matcher(
                text);
        while (matcher.find()) {
            final String url = matcher.toMatchResult().group();
            Log.i(TAG, "addUrlSpans: " + url);
            ClickableSpan underline = new ClickableSpan() {

                @Override
                public void onClick(View arg0) {
                    IMUtil.openUrl(context, url);
                }
            };
            builder.setSpan(underline, matcher.start(), matcher.end(),
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }

        return builder;
    }

    public static int utf8ToUnicode(byte bytes[]) {
        int len = bytes.length;
        int mask = 0xff;
        int result = 0;
        short bitMask[] = {
                0x80, 0x40, 0x20, 0x10
        };
        for (int i = 0; i < len; i++) {
            mask ^= bitMask[i];
        }
        for (int i = 0; i < len; i++) {
            if (i == 0) {
                result = bytes[i] & mask;
            } else {
                result = (result << 6) | (bytes[i] & 0x3f);
            }
        }
        return result;
    }

    public static String unicodeToutf8(int unicode) {
        byte bytes[] = new byte[3];
        bytes[0] = (byte)(0xE0 | (unicode >> 12));
        bytes[1] = (byte)(0x80 | ((unicode >> 6) & 0x3f));
        bytes[2] = (byte)(0x80 | (unicode & 0x3f));
        return new String(bytes);
    }

    public static SpannableStringBuilder addEmojiSmileySpans(Context mContext, CharSequence text,
            int imageAlign) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        int unicode = 0;
        int resourceID = 0;
        String unicodeStr = "";
        byte bytes[] = text.toString().getBytes();
        int flag = 0;
        byte utf8Bytes[] = null;
        short charIndex = -1;
        for (int i = 0; i < bytes.length;) {
            ++charIndex;
            if ((bytes[i] & 0xe0) != 0xe0) {
                if ((bytes[i] & 0xc0) == 0xc0) {
                    i += 2;
                } else {
                    ++i;
                }
                continue;
            } else {
                flag = 0;
                if ((bytes[i] & 0x10) == 0x10) {
                    flag = 4;
                } else {
                    flag = 3;
                }
                utf8Bytes = null;
                utf8Bytes = new byte[flag];
                for (int j = 0; j < flag; j++) {
                    utf8Bytes[j] = bytes[i + j];
                }
                unicode = utf8ToUnicode(utf8Bytes);
                boolean isFound = false;
                if (unicode >= 0xE000 && unicode < 0xE538) {
                    unicodeStr = Integer.toHexString(unicode);
                    for (int j = 0; j < SmileyAdapter.EMOJI_SMILE.length; j++) {
                        for (int k = 0; k < SmileyAdapter.EMOJI_SMILE[j].length; k++) {
                            if (SmileyAdapter.EMOJI_SMILE[j][k][1].equals(unicodeStr)) {
                                resourceID = Integer.parseInt(SmileyAdapter.EMOJI_SMILE[j][k][0]);
                                builder.setSpan(new ImageSpan(mContext, resourceID,
                                        imageAlign), charIndex, charIndex + 1,
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                isFound = true;
                                break;
                            }
                        }
                        if (isFound) {
                            break;
                        }
                    }
                } else if ((unicode >= 0x2600 && unicode <= 0x3299)
                        || (unicode >= 0x1f000 && unicode <= 0x1f700)) {
                    unicodeStr = Integer.toHexString(unicode);
                    for (int j = 0; j < SmileyAdapter.EMOJI_SMILE.length; j++) {
                        for (int k = 0; k < SmileyAdapter.EMOJI_SMILE[j].length; k++) {
                            if (SmileyAdapter.EMOJI_SMILE[j][k][2].equals(unicodeStr)) {
                                byte offset = 1;
                                if (unicode >= 0x1f000 && unicode <= 0x1f700) {
                                    offset = 2;
                                }
                                resourceID = Integer.parseInt(SmileyAdapter.EMOJI_SMILE[j][k][0]);
                                builder.setSpan(new ImageSpan(mContext, resourceID,
                                        imageAlign), charIndex, charIndex + offset,
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                isFound = true;
                                if (unicode >= 0x1f000 && unicode <= 0x1f700) {
                                    ++charIndex;
                                }
                                break;
                            }
                        }
                        if (isFound) {
                            break;
                        }
                    }
                }
                i += flag;
            }
        }
        return builder;
    }

    public static CharSequence addSmileySpans(Context mContext,
            CharSequence text) {
        // 之所以不用stringbuffer是因为这个东西可以将object添加进去
        // SpannableStringBuilder builder = new SpannableStringBuilder(text);
        SpannableStringBuilder builder = addEmojiSmileySpans(mContext, text,
                ImageSpan.ALIGN_BASELINE);

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

    static class Hit {
        public int start;

        public int end;

        public Hit(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}
