
package com.android.mms.transaction;

import java.io.IOException;

import android.content.Context;
import android.net.Uri;
import android.util.Config;
import cn.com.nd.momo.api.util.Log;

import com.android.mms.data.Telephony.Mms.Sent;
import com.android.mms.exception.MmsException;
import com.android.mms.pdu.EncodedStringValue;
import com.android.mms.pdu.PduComposer;
import com.android.mms.pdu.PduPersister;
import com.android.mms.pdu.ReadRecInd;
import com.android.mms.util.MessageUtils;

/**
 * The ReadRecTransaction is responsible for sending read report notifications
 * (M-read-rec.ind) to clients that have requested them. It:
 * <ul>
 * <li>Loads the read report indication from storage (Outbox).
 * <li>Packs M-read-rec.ind and sends it.
 * <li>Notifies the TransactionService about succesful completion.
 * </ul>
 */
public class ReadRecTransaction extends Transaction {
    private static final String TAG = "ReadRecTransaction";

    private static final boolean DEBUG = false;

    private static final boolean LOCAL_LOGV = DEBUG ? Config.LOGD : Config.LOGV;

    private final Uri mReadReportURI;

    public ReadRecTransaction(Context context,
            int transId,
            TransactionSettings connectionSettings,
            String uri) {
        super(context, transId, connectionSettings);
        mReadReportURI = Uri.parse(uri);
        mId = uri;

        // Attach the transaction to the instance of RetryScheduler.
        attach(RetryScheduler.getInstance(context));
    }

    /*
     * (non-Javadoc)
     * @see com.android.mms.Transaction#process()
     */
    @Override
    public void process() {
        PduPersister persister = PduPersister.getPduPersister(mContext);

        try {
            // Load M-read-rec.ind from outbox
            ReadRecInd readRecInd = (ReadRecInd)persister.load(mReadReportURI);

            // insert the 'from' address per spec
            String lineNumber = MessageUtils.getLocalNumber();
            readRecInd.setFrom(new EncodedStringValue(lineNumber));

            // Pack M-read-rec.ind and send it
            byte[] postingData = new PduComposer(mContext, readRecInd).make();
            sendPdu(postingData);

            Uri uri = persister.move(mReadReportURI, Sent.CONTENT_URI);
            mTransactionState.setState(TransactionState.SUCCESS);
            mTransactionState.setContentUri(uri);
        } catch (IOException e) {
            if (LOCAL_LOGV) {
                Log.v(TAG, "Failed to send M-Read-Rec.Ind.", e);
            }
        } catch (MmsException e) {
            if (LOCAL_LOGV) {
                Log.v(TAG, "Failed to load message from Outbox.", e);
            }
        } catch (RuntimeException e) {
            if (LOCAL_LOGV) {
                Log.e(TAG, "Unexpected RuntimeException.", e);
            }
        } finally {
            if (mTransactionState.getState() != TransactionState.SUCCESS) {
                mTransactionState.setState(TransactionState.FAILED);
                mTransactionState.setContentUri(mReadReportURI);
            }
            notifyObservers();
        }
    }

    @Override
    public int getType() {
        return READREC_TRANSACTION;
    }
}
