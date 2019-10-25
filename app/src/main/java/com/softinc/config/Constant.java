package com.softinc.config;

public class Constant {
    public static final String SP_PHONE_NUMBER = "phoneNumber";
    public static final String SP_UID = "UID";
    public static final String SP_TOKEN = "token";//登录返回的Token

    public static final Integer GENDER_MAN = 1;
    public static final Integer GENDER_WOMEN = 2;
    public static final String BASE = "http://123.57.217.223/youelink/";
    public static final String URL_BASE = BASE + "Api";
    public static final String URL_LOGIN = URL_BASE + "/Account/logonuser";

    public static final String URL_REGISTER = URL_BASE + "/Account/regist2";
    public static final String URL_SET_USER_INFO = URL_BASE + "/Account/updateUserInfo";
    public static final String URL_GET_USER_BY_ID = URL_BASE + "/Account/GetUserInfoByID";
    public static final String URL_UPLOAD_ICON = URL_BASE + "/Account/UploadMyIcon";
    public static final String URL_AREA_LIST = URL_BASE + "/Resource/AreaList";
    public static final String URL_JOB_LIST = URL_BASE + "/Resource/JobList";
    public static final String URL_QUERY_MEETINGS = URL_BASE + "/Events/EventList";
    public static final String URL_EVENT_INFO = URL_BASE + "/Events/EventInfo";
    public static final String URL_EVENT_LIST_BY_UID = URL_BASE + "/Events/EventListByUID";
    public static final String URL_SEARCH_MEETINGS = URL_BASE + "/Events/EventSearch";
    public static final String URL_COMMENT_OF_MEETING = URL_BASE + "/Events/EventTopicList";
    public static final String URL_CREATE_MEETING = URL_BASE + "/Events/CreateEvent";
    public static final String URL_ALL_FRIENDS = URL_BASE + "/Contact/BuddyList";
    public static final String URL_ADD_BUDDY = URL_BASE + "/Contact/AddBuddy";
    public static final String URL_CASE_JOIN = URL_BASE + "/Events/JoinEvent";
    public static final String URL_ACCEPT_JOIN_EVENT = URL_BASE + "/Events/AcceptJoinEvent";
    public static final String URL_CASE_ADD_COMMENT = URL_BASE + "/Events/AddEventTopic";
    public static final String URL_MEMBERS_OF_MEETING = URL_BASE + "/Events/EventMemberList";
    public static final String URL_MEMBERS_OF_ChatGroup = URL_BASE + "/Contact/UserListFromChatGroup";  //根据环信聊天组ID获取用户列表
    public static final String URL_Search = URL_BASE + "/Account/SearchUsers";
    public static final String URL_RemoveBuddy = URL_BASE + "/Contact/RemoveBuddy";
    public static final String URL_SyncContact = URL_BASE + "/Contact/SyncContactUsers";
    public static final String URL_RequestAgreeFriend = URL_BASE + "/Contact/AllowAddBuddy";
    public static final String URL_RequestDisAgreeFriend = URL_BASE + "/Contact/RejectAddBuddy";
    public static final String URL_RequestAgreelist = URL_BASE + "/Contact/BuddyRequestList";
    public static final String URL_USER_SETTING = URL_BASE + "/UserSetting/BuddyVerify";
    public static final String URL_USER_QR = URL_BASE + "/UserSetting/MyCode";

    public static final String URL_kYYL_GetUserInfoByHxUser = URL_BASE + "/Account/GetUserInfoByHXUser";

    public static final String URL_CASE_REPORT_EVENT = URL_BASE + "/Events/ReportEvent";
    public static final String URL_BUDDY_REQUEST_LIST = URL_BASE + "/Contact/BuddyRequestList";
    public static final String URL_new_FRIENDSCount = URL_BASE + "/Contact/BuddyRequestListCount";

    public static final String URL_ORDER_CREATE = URL_BASE + "/Order/CreateOrder";
    public static final String URL_ORDER_CREATE_CHARGE = URL_BASE + "/Order/CreateCharge";
    public static final String URL_FORGET_PASSWORD = URL_BASE + "/Account/forgetPassword";


    public static final int TYPE_HOT = 1;
    public static final int TYPE_NEAR = 2;
    public static final int TYPE_FRIENDS = 3;

    public static final String URL_ImagePATH = BASE + "upload/userpic/";
    public static final String NEW_FRIENDS_USERNAME = "item_new_friends";
    public static final String GROUP_USERNAME = "item_groups";
    public static final String MESSAGE_ATTR_IS_VOICE_CALL = "is_voice_call";
    public static final String MESSAGE_ATTR_IS_VIDEO_CALL = "is_video_call";
    public static final String ACCOUNT_REMOVED = "account_removed";

    public static final String URL_NearByPeople = URL_BASE + "/Account/NearUserList";

    public static final String CACHE_LAST_USER_ACCOUNT = "last_user_account";
    public static final String CACHE_LAST_USER_PWD = "last_user_pwd";

    public static final String CACHE_USER_ACCOUNT = "user_account_";
    public static final String CACHE_USER_PWD = "user_pwd_";
    public static final String CACHE_USER = "user_";
    public static final String CACHE_USER_QR = "user_qr_";
    public static final String CACHE_USER_PORTRAIT = "user_portrait_";
    public static final String CACHE_USER_JOIN_EVENTS = "user_join_events_id_";
    public static final String CACHE_USER_CREATE_EVENTS = "user_create_events_id_";

    public static final String CACHE_MEETINGS = "meetings_uid_";
    public static final String CACHE_MEETING = "meeting_id_";
    public static final String CACHE_MEETING_USERS = "meeting_users_id_";
    public static final String CACHE_MEETING_COMMENTS = "meeting_comments_id_";

    public static final String URL_GetPurchaseList = URL_BASE + "/Order/GetPurchaseList";
    public static final String URL_GET_MY_COINS = URL_BASE + "/UserSetting/GetMyCoins";
    public static final String URL_EVENT_TOP_LIST = URL_BASE + "/Events/EventTopList";
}

