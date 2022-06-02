package com.chimzeart.yangachecker.network

import com.chimzeart.yangachecker.database.YangaBundle
import com.chimzeart.yangachecker.database.YangaDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Repository(private val database: YangaDatabase) {
    private val api =Api.retrofitService
    val allBundles = database.yangaBundleDao.getAllLiveData()


    suspend fun verifyNumber(body: VerifyRequest):VerifyResponse= withContext(Dispatchers.IO){
        api.verifyNumber(body)
    }

    suspend fun confirmOTP(otpConfirmRequest: OtpConfirmRequest):ConfirmResponse= withContext(Dispatchers.IO){
        api.confirmOTP(otpConfirmRequest)
    }

    suspend fun getBundles(token: String):BundlesResponse= withContext(Dispatchers.IO){
        api.getBundles(token)
    }

    suspend fun getBalanceAndUsage(token: String):UsageResponse= withContext(Dispatchers.IO){
        api.getBalanceAndUsage(token)
    }

    suspend fun insertYangaBundle(yangaBundle: YangaBundle) =
        database.yangaBundleDao.insert(yangaBundle)

    suspend fun buyBundle(token: String, buyBundleRequest: BuyBundleRequest):BuyBundleResponse = withContext(Dispatchers.IO){
        api.buyBundle(token, buyBundleRequest)
    }

}