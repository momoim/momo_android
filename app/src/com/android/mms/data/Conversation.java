
package com.android.mms.data;

import android.net.Uri;

import com.android.mms.data.Telephony.Threads;

public class Conversation {

    public static final Uri sAllThreadsUri =
            Threads.CONTENT_URI.buildUpon().appendQueryParameter("simple", "true").build();

    public static final String[] ALL_THREADS_PROJECTION = {
            Threads._ID,
            Threads.DATE,
            Threads.MESSAGE_COUNT,
            Threads.RECIPIENT_IDS,
            Threads.SNIPPET,
            Threads.SNIPPET_CHARSET,
            Threads.READ,
            Threads.ERROR,
            Threads.HAS_ATTACHMENT
    };

    public static final String[] UNREAD_PROJECTION = {
            Threads._ID,
            Threads.READ
    };

    public static final String UNREAD_SELECTION = "(read=0 OR seen=0)";

    public static final String[] SEEN_PROJECTION = new String[] {
            "seen"
    };

    public static final int ID = 0;

    public static final int DATE = 1;

    public static final int MESSAGE_COUNT = 2;

    public static final int RECIPIENT_IDS = 3;

    public static final int SNIPPET = 4;

    public static final int SNIPPET_CS = 5;

    public static final int READ = 6;

    public static final int ERROR = 7;

    public static final int HAS_ATTACHMENT = 8;

    private long id;

    private long date;

    private int messageCount;

    private String snippet;

    private String snippetCs;

    private boolean hasRead;

    private int error;

    private boolean hasAttachment;

    private String receipents;

    private ContactList contactList;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public String getSnippetCs() {
        return snippetCs;
    }

    public void setSnippetCs(String snippetCs) {
        this.snippetCs = snippetCs;
    }

    public boolean hasRead() {
        return hasRead;
    }

    public void setRead(boolean hasRead) {
        this.hasRead = hasRead;
    }

    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }

    public boolean hasAttachment() {
        return hasAttachment;
    }

    public void setAttachment(boolean hasAttachment) {
        this.hasAttachment = hasAttachment;
    }

    public String getReceipents() {
        return receipents;
    }

    public void setReceipents(String receipents) {
        this.receipents = receipents;
    }

    public ContactList getContactList() {
        return contactList;
    }

    public void setContactList(ContactList contactList) {
        this.contactList = contactList;
    }
}
