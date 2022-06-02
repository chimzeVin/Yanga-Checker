package com.chimzeart.yangachecker.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BuyBundleResponse (
    @Json(name = "status")
    val status:String,

    @Json(name = "error_message")
    val error:String = "",



        )


//{"status":0,"error_message":"INSUFFICIENT_FUNDS"}