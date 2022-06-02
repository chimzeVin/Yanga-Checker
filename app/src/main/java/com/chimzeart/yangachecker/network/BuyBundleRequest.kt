package com.chimzeart.yangachecker.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class BuyBundleRequest(
    @Json(name = "bundleCacheId")
    var bundleCacheId: String,

    @Json(name = "preBuyable")
    var preBuyable: Boolean = false,

    @Json(name = "price")
    var price: Int,

    @Json(name = "size")
    var size: Int,

    @Json(name = "token")
    var token: String,

    @Json(name = "paysrc")
    var paysrc: String = "CS"
)