package com.example.garred.doramatv;

/**
 * Created by garred on 14.03.17.
 */

public class Settings {
    private static String api_id = "6863889";
    private static String api_scope = "videos,offline";
    private static String api_display = "mobile";
    private static String api_redirect_uri = "https://oauth.vk.com/blank.html";
    private static String api_response_type = "token";

    public static String vk_api_id() {
        return api_id;
    }
    public static String vk_api_scope() {
        return api_scope;
    }
    public static String vk_api_display() {
        return api_display;
    }
    public static String vk_api_redirect_uri() {
        return api_redirect_uri;
    }
    public static String vk_api_response_type() {
        return api_response_type;
    }
}
