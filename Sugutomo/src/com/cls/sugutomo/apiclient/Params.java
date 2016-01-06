package com.cls.sugutomo.apiclient;

import com.cls.sugutomo.utils.Global;

public class Params {

	// PARAMS
	public static final String PARAM_ANDROID = "0";
	public static final String PARAM_PLATFORM = "platform";
	public static final String PARAM_PLATFORM_VERSION = "platform_version";
	public static final String PARAM_PHONE_NAME = "phone_name";
	public static final String PARAM_UDID = "udid";
	public static final String PARAM_USER_ID = "user_id";
	public static final String PARAM_USER_ID_ = "_user_id";
	public static final String PARAM_STATUS = "status";
	public static final String PARAM_TYPE_LOGIN = "type_login";
	public static final String PARAM_FIRST_LOGIN = "first_login";
	public static final String PARAM_IS_FRIEND = "is_friend";
	public static final String PARAM_LAST_LOGIN_DATETIME = "last_login_datetime";
	public static final String PARAM_LASTLOGIN = "lastlogin";
	public static final String PARAM_TOKEN = "token";
	public static final String PARAM_TOKEN_ = "_token";
	public static final String PARAM_AVATAR = "avatar";

	public static final String PARAM_PROFILE = "profile";
	public static final String PARAM_PROFILE_ITEM = "profile_item";
	public static final String PARAM_PROFILE_ITEM_CREATE = "user_profile_client";
	public static final String PARAM_AGE = "age";
	public static final String PARAM_USER_AVATAR = "avatar";
	public static final String PARAM_AGE_MIN = "age_min";
	public static final String PARAM_AGE_MAX = "age_max";

	public static final String PARAM_DISTANCE_LIMIT = "distance_limit";
	public static final String PARAM_GPS_IS_ON = "gps_is_on";
	public static final String PARAMS_UPDATE_GPS = "update_gps";
	public static final String FAVORITE_INDEX  = "favorite_idx";
	public static final String PARAM_DURATION = "duration";
	//in app purchase google
	public static final String PARAM_PURCHASE_INFO = "signed_data";
	public static final String PARAM_PURCHASE_SIGNTURE = "signature";
	public static final String PARAM_PURCHASE_MONEY_YEN = "money";
	
	// METHOD
	public static String REQUEST_METHOD_ACCOUNT = "account/";
	public static String REQUEST_METHOD_USER = "user/";
	public static String REQUEST_METHOD_STAMP = "stamp/";
	public static String REGISTER_ACCOUNT_EMAIL = "register_account_email";
	public static String LOGIN_BY_EMAIL = "login_by_email";
	public static String REGISTER_BY_FACEBOOK = "register_by_facebook";
	public static String LOGIN_BY_FACEBOOK = "login_by_facebook";
	public static String RESEND_EMAIL_ACCTIVED = "send_email_active_acc_again";
	public static String GET_PROFILE_MASTER_DATA = "get_profile_master_data";
	public static String CREATE_PROFILE_USER = "create_profile";
	public static String LOAD_MAP = "load_map";
	public static String UPDATE_LOCATION = "save_user_location";
	public static String GET_TOP_APPEAL = "get_top_appeal";
	public static String GET_USER_LIST = "get_user_list";
	public static String GET_LIST_STAMP = "get_list_stamp";

	// New API request
	public static final String AVATAR_LIST = "avatarList";
	public static final String LIST_PROFILE = "LIST_PROFILE";
	public static final String USER_LOGIN = "user/login";
	public static final String USER_GET_LOGIN = "user/getlogin";
	public static final String REGISTER = "register";
	public static final String USER = "user";
	public static final String PROFILES = "profiles";
	public static final String PURCHASE = "purchase";
	public static final String USER_FB_LOGIN = "user/login_via_fb";
	public static final String RESEND_EMAIL_ACTIVE = "resend_mail_active";
	public static final String AVATAR_UPLOAD = "avatar_upload";
	public static final String AVATAR_REMOVE = "avatar_remove";
	public static final String AVATAR_SETDEFAULT = "avatar_setdefault";
	public static final String MY_PROFILE = "my_profile";
	public static final String SAVE_PROFILE = "save_profile";
	public static final String GET_PROFILE = "get_profile";
	public static final String LOGOUT = "logout";
	public static final String RESET_PASSWORD = "request_password";
	public static final String MAP = "map";
	public static final String CHANGE_PASS ="user/change_password";
	public static final String CHANGE_MAIL ="user/change_email";
	public static final String DELETE_ACC ="user/delete";
	public static final String SAVE_SETTING ="user/save_settting";
	public static final String NOTIFYCATION_NEWUSER= "notification_newuser";
	public static final String NOTIFYCATION_RANK= "notification_ranking";
	public static final String NOTIFYCATION_FAVORITE= "notification_favorite";
	public static final String APPEAL = "appeal";
	public static final String RANK = "rank";
	public static final String FAVORITE = "favorite";
	public static final String FAVORITE_ADD = "add";
	public static final String FAVORITE_REMOVE = "remove";
	public static final String SETTING = "setting_app";
	public static final String SETTING_GPS = "setting_gps";
	public static final String SETTING_SOUND = "setting_sound";
	public static final String SETTING_VIBE = "setting_vibe";
	public static final String NOTIFI_TIME = "NOTIFI_TIME";
	public static final int  SETTING_DEFAULT_LOCATION = 200;
	public static final String SETTING_MY_LOCATION = "setting_my_location";
	public static final String SETTING_NOTIFYCATION_MAIL = "setting_notify_mail";
	public static final String SETTING_NOTIFYCATION_MESSENGER_SHOW_BIGVIEW = "setting_notify_mess_bigview";
	public static final String SETTING_NOTIFYCATION_MESSENGER = "setting_notify_mess";
	public static final String SETTING_NOTIFYCATION_FOOTPRINT = "setting_notify_fottprint";
	public static final String SETTING_NOTIFYCATION_SMS_SUPPORT = "setting_notify_sms_sup";
	public static final String SETTING_NOTIFYCATION_NEW_USER = "setting_notify_newuser";
	public static final String SETTING_NOTIFYCATION_SUBMIT_TO_FAVORITE = "setting_notify_submittofavorite";
	public static final String SETTING_NOTIFYCATION_WHEN_IN_TOP = "setting_notify_top_user";
	public static final String SETTING_BIGVIEW_MESSENGER_SHOW_BIGVIEW= "SETTING_BIGVIEW_MESSENGER_SHOW_BIGVIEW";
	public static final String SETTING_BIGVIEW_SMS_SUPPORT="SETTING_BIGVIEW_SMS_SUPPORT";
	public static final String SETTING_BIGVIEW_SUBMIT_TO_FAVORITE="SETTING_BIGVIEW_SUBMIT_TO_FAVORITE";
	public static final String SETTING_BIGVIEW_WHEN_IN_TOP="SETTING_BIGVIEW_WHEN_IN_TOP";
	public static final String SETTING_BIGVIEW_NEW_USER="SETTING_BIGVIEW_NEW_USER";
	public static final int SETTING_NOTIFYCATION_MESSENGER_ALL = 1;
	public static final int SETTING_NOTIFYCATION_MESSENGER_FAVORITE = 2;
	public static final String NOTIFY_ID ="notify_id";
	public static final String NOTIFY_STATE_APP ="notify_state_app";
	public static final String PARAM_STAMP = "stamp";
	public static final String PARAM_IMAGE_FULL_URL = "image_url";
	public static final String PARAM_DISTANCE = "distance";
	public static final String PARAM_STRAT_FROM_NOTIFICATION = "start_from_notification";
	public static final String PARAM_MESSENGER = "notifi_messenger";
	public static final String PARAM_NOTIFY_TYPE = "notifi_type";
	public static final String PARAM_FROM = "from";
	public static final String PARAM_TO = "to";
	// profile item
	public static final String PARAM_PROFILE_ID = "profile_id";
	public static final String PARAM_PROFILE_NAME = "profile_name";
	public static final String PARAM_PROFILE_DISPLAY = "profile_display_order";
	public static final String PARAM_IS_INPUT = "is_input";
	
	public static final String PARAM_PROFILE_ITEMS = "profile_items";
	public static final String PARAM_PROFILE_ITEM_ID = "profile_item_id";
	public static final String PARAM_PROFILE_ITEM_NAME = "profile_item_name";
	public static final String PARAM_PROFILE_ITEM_DISPLAY = "profile_item_display_order";
	public static final String PARAM_USER_DATA = "user_data";
	public static final String PARAM_USER_DATA_TEXT ="user_data_text";
	public static final String PARAM_AVATAR_FULL_URL = "full_url";
	public static final String PARAM_AVATAR_ID = "id";
	// profile user
	public static final String PARAM_ID = "id";
	public static final String PARAM_NAME = "name";
	public static final String PARAM_USERNAME = "username";
	public static final String PARAM_PASSWORD = "password";
	public static final String PARAM_REQUIRE_PASSWORD = "require_password";
	public static final String PARAM_CURRENT_PASSWORD = "current_password";
	public static final String PARAM_NEW_PASSWORD = "new_password";
	public static final String PARAM_FB = "facebook_id";
	public static final String PARAM_EMAIL = "email";
	public static final String PARAM_SEX = "sex";
	public static final String PARAM_BIRTHDAY = "birthday";
	public static final String PARAM_IS_LOGIN = "is_login";
	public static final String PARAM_WALL_STATUS = "wall_status";
	public static final String PARAM_INTERESTED_PARTNER = "interested_partner";
	public static final String PARAM_HEIGHT = "heigh";
	public static final String PARAM_BODY_FIGURE = "body_figure";
	public static final String PARAM_GPS_LNG = "gps_long";
	public static final String PARAM_GPS_LAT = "gps_lat";
	public static final String PARAM_GPS_LNG_ = "_gps_long";
	public static final String PARAM_GPS_LAT_ = "_gps_lat";
	public static final String PARAM_IMAGE = "image";
	public static final String PARAM_IMAGE_ID = "image_id";
	public static final String PARAM_POSSESS_POINT = "possess_point";
	
	// information api
	public static final String LAYOUT_ID = "layout_id";
	public static final String REQUEST = "request";
	public static final String PAGE = "page";
	public static final String LIMIT = "limit";
	public static final String QA = "qa";
	public static final String POLICY = "policy";
	public static final String TERM = "term";
	public static final String PARAM_CONTENT = "content";
	public static final String PARAM_ORDER = "order";
	public static final String PARAM_TYPE = "type";
	public static final String PARAM_TITLE = "title";
	public static final String PARAM_CREATED_DATETIME = "created_datetime";
	public static final String PARAM_UPDATED_DATETIME = "updated_datetime";
	public static final String FILTERS = "filters";
	public static final String VIEW_TERM_OR_POLICY_FROM_LOGIN = "VIEW_TERM_OR_POLICY_FROM_LOGIN";
	
	// chat param
	public static final String PARAM_PRESENT_POINT = "present_point";
	public static final String PARAM_STAMP_ID = "stamp_id";
	public static final String CHAT_IMAGE = "image_chat";
	public static final String CHAT_IMAGE_UPLOAD = "chat/upload_image";
	public static final String PARAM_FILE_NAME = "file_name";
	public static final String PARAM_FULL_PATH = "full_path";
	
	// block param
	public static final String PARAM_BLOCK = "block";
	
	// group_id define
	public static final String GROUP_ID = "group_id";
	public static final int MEMBER_NOT_ACTIVE = 1;
	public static final int MEMBER = 2;
	public static final int PATROL_USER = 3;
	public static final int MEMBER_NOT_COMPLETE_INFO = 4;
	public static final int OPERATOR = 5;
	public static final int ADMIN = 6;
	public static final int BLOCKED = 8;

	// error code define
	public static int INVALID_TOKEN = 10000;
	public static int INVALID_ANONYMOUS_NOT_REGISTER = 10043;
	public static int LIMIT_DISTANCE = 200000000;//200000000km
    public static String AUTHENTICATE = "authenticate";
    public static String STOP_SERVICE = "stop_service";
    public static long SLEEP_TIME = 1000;

    // broadcast
    public static final String PARAM_RECONNECT_CHATSERVER = "PARAM_RECONNECT_CHATSERVER";
    public static final String PARAM_RECONNECT_INTERNET = "PARAM_RECONNECT_INTERNET";
    public static final String PARAM_RECONNECT_INTERNET_CHECK = "PARAM_RECONNECT_INTERNET_CHECK";
    public static final String PARAM_BROADCAST = "broadcast";
    public static final String PARAM_PEOPLE = "people";
    public static final String PARAM_POINT = "point";
    public static final String PARAM_SETTINGS = "settings";
    public static final String PARAM_MESSAGES = "messages";
    public static final String PARAM_GIF_POINT = "point";
    
    // delete message, conversation
    public static final String PARAM_CHAT = "chat";
    public static final String PARAM_DELETE_MESSAGE = "delete_message";
    public static final String PARAM_DELETE_CONVERSATION = "delete_conversation";
    public static final String MESSAGE 	= "message";
    public static final String MSG 	= "msg";
    public static final String PARAM_CONVESATION_ID = "conversation_id";
    public static final String PARAM_MESSAGE_ID = "message_id";
    //user report
    public static final String PARAM_REPORT = "report";
    
    //footprint
    public static final String PARAM_GET_FOOTPRINT = "get_footprint";
    public static final String PARAM_FOOTPRINT = "footprint";
    public static final String PARAM_REMOVE_FOOTPRINT = "remove_footprint";
    public static final String PARAM_UPDATE_FOOTPRINT = "update_footprint";
    public static final String PARAM_COUNT = "count";
    public static final String PARAM_UPDATE_CHAT_LIST = "UPDATE_CHAT_LIST_ACTIVITIES";
    public static final String PARAM_UPDATE_CHAT_ACTIVITIES = "UPDATE_CHAT_ACTIVITIES";
    public static final String START_FROM_LOGIN ="START_FROM_LOGIN";
    //broastcast
    public static final String PARAM_MESSAGE_MODEL = "message_model";
    
    public static final String PREF_USER_INFO = "USER_INFO_PREF";
    public static final String PREF_BAD_WORD = "bad_word";
    public static final String PREF_LIST_USER_BLOCK = "block_list";
    public static final String PREF_LIST_USER_BLOCK_OR_BEBLOCK = "block_OR_BEBLOCK_list";
    public static final String PREF_LIST_FAVORITE_ID = "list_favorite_id";
    public static final String PREF_STAMP = "PREF_STAMP";
    
    
    public static final String GOOGLE_REGISTER_GCM = "GOOGLE_REGISTER_GCM";
    public static final String PROPERTY_APP_VERSION="PROPERTY_APP_VERSION";
}
