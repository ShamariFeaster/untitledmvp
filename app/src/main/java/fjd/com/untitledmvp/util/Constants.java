package fjd.com.untitledmvp.util;

import android.app.Service;

import fjd.com.untitledmvp.R;
import fjd.com.untitledmvp.service.ChatListenerService;
import fjd.com.untitledmvp.state.GlobalState;

/**
 * Created by WZHJTN on 10/2/2015.
 */
public final class Constants {
    final public static String FBURL = "https://nitework.firebaseio.com";
    final public static  String AWS_KEY = "AKIAIUN66RZMHV2GVCKA";
    final public static  String AWS_SECRET = "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";
    final public static  String AWS_BUCKET = "untitled-mvp-images";
    final public static String MOCK_UID = "facebook:723263297817907";
    final public static String MOCK_FN = "Shamari";
    final public static String MOCK_EMAIL = "alistproducer2@gmail.com";
    final public static String MOCK_LN = "feaster";
    final public static String MOCK_UN = MOCK_FN;
    final public static  String NO_IMAGE_YET = "-1";
    final public static String USE_KEY = "-1"; //signal to use key as value
    final public static String CONVO_KEY = "convoId";
    final public static String LOGOUT_KEY = "logout";
    final public static Class<ChatListenerService> SERVICE_CLASS_TKN = ChatListenerService.class;
    final public static String SERVICE_CONVO_IDS = "convoIDs";
    final public static String CURR_USER_KEY = "currUser";

    final public  static String FLG_REBIND_SERVICE = "rebindSvc";

    final public static String BROADCAST_ACTION
            = GlobalState.GetContext().getResources()
            .getText(R.string.broadcast_activity_name).toString();
    final public static String BROADCAST_NEW_MATCH = "com.fjd.newMatch";
    final public static String BROADCAST_NEW_MESSAGE = "com.fjd.newMessage";
    final public static int BROADCAST_TYPE_NEW_MATCH = 1;
    final public static int BROADCAST_TYPE_NEW_MESSAGE = 0;
    final public static int BROADCAST_HANDLED = 1;
    final public static int BROADCAST_UNHANDLED = 0;
    final public static String BC_NEW_MSG_EXTRAS_CONVO_ID = "broadcast.msg.convoId";
    final public static String BC_NEW_MSG_EXTRAS_TEXT = "broadcast.msg.text";
    final public static String BC_NEW_MSG_EXTRAS_USER = "broadcast.msg.user";
    final public static String BC_NEW_MSG_EXTRAS_SENDER = "broadcast.msg.sender";
    final public static String BC_NEW_MSG_EXTRAS_ICON = "broadcast.msg.icon";

    final public static String BC_NEW_MATCH_EXTRAS_MATCH_FN = "broadcast.matches.fn";
    final public static String BC_NEW_MATCH_EXTRAS_ICON = "broadcast.matches.icon";
}
