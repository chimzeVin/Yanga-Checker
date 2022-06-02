package com.chimzeart.yangachecker.network

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class BundlesResponse (
    val status: String,
    val data: List<DataSubClass> = listOf()
        )

data class DataSubClass (
    val groupDescription: String,
    val bundleList: List<Bundle>
        )

data class Bundle(
    val bundleCacheId: String,
    val cacheExpiry: String,
    val offerId: String,
    val name:String,
    val label: String,
    val validity:Int,
    val title:String,
    val services:List<String>,
    val type: String,
    val price: Int,
    val size:Int,
    val unit: String,
    val imageMini: String,
    val imageLarge: String,
//    val flexibleFields: FlexibleFields


)

data class FlexibleFields(
    val guiValidPaysources:String,
    val guiDisplayMomoPurchase:String
)
