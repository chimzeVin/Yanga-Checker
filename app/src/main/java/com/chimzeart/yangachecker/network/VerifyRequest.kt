package com.chimzeart.yangachecker.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class VerifyRequest(
    @Json(name = "msisdn")
    var msisdn: String
) {

    @Json(name = "extras")
    var extras = ExtraValues()

    @JsonClass(generateAdapter = true)
    class ExtraValues
}