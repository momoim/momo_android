
package com.android.mms.data;

public class Contact {
    private String mName;

    private String mNumber;

    private long mRecipientId;

    public Contact() {

    }

    public Contact(String name, String number) {
        this.mName = name;
        this.mNumber = number;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getNumber() {
        return mNumber;
    }

    public void setNumber(String number) {
        this.mNumber = number;
    }

    public long getRecipientId() {
        return mRecipientId;
    }

    public void setRecipientId(long recipientId) {
        this.mRecipientId = recipientId;
    }

    private Contact(String number) {
        mNumber = number;
    }

    /**
     * 通过电话号码获取联系人
     * 
     * @param number 联系人电话号码
     * @return 联系人
     */
    static Contact get(String number) {
        return null;
    }

    /**
     * TODO: 调用联系人的方法，获取此号码对应的联系人详细信息
     * 
     * @param c
     */
    static void fill(Contact c) {
    }
}
