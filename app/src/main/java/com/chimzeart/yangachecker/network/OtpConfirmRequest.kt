package com.chimzeart.yangachecker.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class OtpConfirmRequest(
    @Json(name = "msisdn")
    var msisdn: String,
    @Json(name = "code")
    var code: String
)