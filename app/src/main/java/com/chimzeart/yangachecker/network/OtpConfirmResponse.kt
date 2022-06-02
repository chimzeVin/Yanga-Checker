package com.chimzeart.yangachecker.network

data class ConfirmResponse (
    val status:String,
    val data: DataResponse = DataResponse()
        )

data class DataResponse(
    val token:String = "",
    val bundleAllocated: Boolean = false,
    val bundleAllocatedPreviously: Boolean = false
)