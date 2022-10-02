package ru.garretech.garred.doramatv

object Settings {
    const val APP_PREFERENCES = "mysettings"
    const val client_id = "6863889"
    const val api_scope = "video,offline"
    const val api_display = "mobile"
    const val api_redirect_uri = "http://api.vk.com/blank.html"
    const val api_secret_key = "HMIqzRPUL4Shf9eOnjan"
    const val api_response_type = "token"
    const val version = "5.131"
    const val access_token = "vk1.a.f7gKKjFNmYArFwdMYG8AOx-bb0AeDCdGpwUxpx5nCDrVcbTVSVeBGq2mHhKnTtSWt0QzoiBkZFRk0oc0lHmEbLmP79glRmEivvzFK34eGgO13c6klJgDx6TNx_8LpDSwOetI_W3XFdEvXU4glZf63Hb1ez0nrIaWRpt6KgQhha6OC-kSliVCrevqfMNlQ-Fg"
    const val access_token1 = "abbce273a0891ea348553beb19d85a9432bbcb87ed757158ad581347d3bd620ceb8f8263c63cbeadd3493"
    var max_loaded_in_screen = 15
    const val BLOCK_ID = "adf-304149/999174"

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
