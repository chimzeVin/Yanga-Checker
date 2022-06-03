package com.chimzeart.yangachecker.network

data class UsageResponse (

    val status: String,
    val data: UsageDataResponse
    )

data class UsageDataResponse(
    val accountBalances: AccountBalances,
    val bundleUsage: List<BundleUsage>

)

data class BundleUsage (
    val usedValue: Double,
    val thresholdValue: Double,
    val remainingValue: Double,
    val expiryDateTime: String,
    val secondsRemaining: Int = 0

        )

data class AccountBalances(
    val msisdn: String,
    val accountBalanceList: List<AccountBalanceList>
)

data class AccountBalanceList (
    val balance: Float,
    val displayBalance: String,
        )
