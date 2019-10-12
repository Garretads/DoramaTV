package ru.garretech.garred.doramatv

object Settings {
    const val APP_PREFERENCES = "mysettings"
    const val client_id = "6863889"
    const val api_scope = "video,offline"
    const val api_display = "mobile"
    const val api_redirect_uri = "http://api.vk.com/blank.html"
    const val api_secret_key = "HMIqzRPUL4Shf9eOnjan"
    const val api_response_type = "token"
    const val version = "5.101"
    const val access_token = "30338145c309b0358ad0d22022ba3a4c5005b6d541281a9ef7390cf4c1a194c97fd1fc88ebae4cf49b531"
    var max_loaded_in_screen = 15
    const val BLOCK_ID = "adf-304149/991383"
    const val BLOCK_ID1 = "adf-304149/999174"

    const val SITE_URL = "https://doramatv.live"
    const val SITE_URL1 = "doramatv.live"

    const val APP_FIRST_RUN = "first_run_check_new"
    const val VERSION_CODE = "version_code"


    fun version(): String {
        return version
    }

    fun access_token(): String {
        return access_token
    }

    fun api_secret_key(): String {
        return api_secret_key
    }

    fun max_loaded_in_screen(): Int {
        return max_loaded_in_screen
    }

    fun block_id(): String {
        return BLOCK_ID
    }
}
