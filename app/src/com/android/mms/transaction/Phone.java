
package com.android.mms.transaction;

public class Phone {
    /**
     * APN types for data connections. These are usage categories for an APN
     * entry. One APN entry may support multiple APN types, eg, a single APN may
     * service regular internet traffic ("default") as well as MMS-specific
     * connections.<br/>
     * APN_TYPE_ALL is a special type to indicate that this APN entry can
     * service all data connections.
     */
    static final String APN_TYPE_ALL = "*";

    /** APN type for default data traffic */
    static final String APN_TYPE_DEFAULT = "default";

    /** APN type for MMS traffic */
    static final String APN_TYPE_MMS = "mms";

    /** APN type for SUPL assisted GPS */
    static final String APN_TYPE_SUPL = "supl";

    /** APN type for DUN traffic */
    static final String APN_TYPE_DUN = "dun";

    /** APN type for HiPri traffic */
    static final String APN_TYPE_HIPRI = "hipri";

    static final String STATE_KEY = "state";

    static final String PHONE_NAME_KEY = "phoneName";

    static final String FAILURE_REASON_KEY = "reason";

    static final String STATE_CHANGE_REASON_KEY = "reason";

    static final String DATA_APN_TYPES_KEY = "apnType";

    static final String DATA_APN_KEY = "apn";

    // "Features" accessible through the connectivity manager
    static final String FEATURE_ENABLE_MMS = "enableMMS";

    static final String FEATURE_ENABLE_SUPL = "enableSUPL";

    static final String FEATURE_ENABLE_DUN = "enableDUN";

    static final String FEATURE_ENABLE_HIPRI = "enableHIPRI";

    /**
     * Return codes for <code>enableApnType()</code>
     */
    static final int APN_ALREADY_ACTIVE = 0;

    static final int APN_REQUEST_STARTED = 1;

    static final int APN_TYPE_NOT_AVAILABLE = 2;

    static final int APN_REQUEST_FAILED = 3;

}
