
package com.android.mms.data;

public class WorkingMessage {
    // Errors from setAttachment()
    public static final int OK = 0;

    public static final int UNKNOWN_ERROR = -1;

    public static final int MESSAGE_SIZE_EXCEEDED = -2;

    public static final int UNSUPPORTED_TYPE = -3;

    public static final int IMAGE_TOO_LARGE = -4;

    // Attachment types
    public static final int TEXT = 0;

    public static final int IMAGE = 1;

    public static final int VIDEO = 2;

    public static final int AUDIO = 3;

    public static final int SLIDESHOW = 4;

    // Current attachment type of the message; one of the above values.
    private int mAttachmentType;

}
