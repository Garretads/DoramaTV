package ru.garretech.garred.doramatv;

/**
 * Created by garred on 14.03.17.
 */

public class Settings {
    private static String client_id = "6863889";
    private static String api_scope = "video,offline";
    private static String api_display = "mobile";
    private static String api_redirect_uri = "ttp://api.vk.com/blank.html";
    private static String api_secret_key = "HMIqzRPUL4Shf9eOnjan";
    private static String api_response_type = "token";
    private static String version = "5.92";
    private static String access_token = "30338145c309b0358ad0d22022ba3a4c5005b6d541281a9ef7390cf4c1a194c97fd1fc88ebae4cf49b531";
    private static int max_loaded_in_screen = 15;

    public static String vk_api_id() {
        return client_id;
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
    public static String version() {
        return version;
    }
    public static String access_token() {
        return access_token;
    }
    public static String api_secret_key() {
        return api_secret_key;
    }

    public static int max_loaded_in_screen() {
        return max_loaded_in_screen;
    }
}
