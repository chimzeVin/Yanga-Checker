package com.chimzeart.yangachecker.ui

import android.util.Log
import androidx.lifecycle.*
import androidx.work.*
import com.chimzeart.yangachecker.RoutineCheckerAutoBuyWorker
import com.chimzeart.yangachecker.TAG
import com.chimzeart.yangachecker.database.YangaBundle
import com.chimzeart.yangachecker.database.YangaDatabase
import com.chimzeart.yangachecker.network.*
import com.chimzeart.yangachecker.workers.RoutineCheckWorker
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit

const val TOKEN = "token"
const val FREQUENCY = "frequency"
const val PRICE = "price"

class HomeViewModel(private val dataSource: YangaDatabase, token: String) : ViewModel() {

    private var _bundleDiscount = MutableLiveData<Bundle>()
    val bundleDiscount: LiveData<Bundle> get() = _bundleDiscount

    private var _onRefreshProgress: MutableLiveData<Int> = MutableLiveData(0)
    val onRefreshProgress: LiveData<Int> get() = _onRefreshProgress

    private val repository = Repository(dataSource)
    val yangaBundles: LiveData<List<YangaBundle>> = repository.allBundles
    private var _balanceNusage = MutableLiveData<UsageDataResponse>()
    val balanceNusage: LiveData<UsageDataResponse> get() = _balanceNusage

    init {

        checkYanga(token)
    }

    companion object {
        fun startYangaChecker(token: String) {

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val inputData: Data = Data.Builder()
                .putString(TOKEN, token)
                .build()

            val repeatingCheckerRequest =
                PeriodicWorkRequestBuilder<RoutineCheckWorker>(15, TimeUnit.MINUTES)
                    .setInputData(inputData)
                    .setConstraints(constraints)
                    .build()


            WorkManager.getInstance().enqueueUniquePeriodicWork(
                RoutineCheckWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                repeatingCheckerRequest
            )




        }
    }


    fun resetRefreshProgress() {
        _onRefreshProgress.value = 0
    }

    fun checkYanga(token: String) {
        Log.i(TAG, "checkYanga: $token")
        viewModelScope.launch {
            val repo = Repository(dataSource)
            try {

                val result = repo.getBundles(token)

                if (result.status == "1") {
                    val bundle4Gb = result.data[0].bundleList[0]
                    _bundleDiscount.value = bundle4Gb

                } else {
//                    Toast.makeText(context, "Oops. Some error was encountered, try again in a bit", Toast.LENGTH_SHORT).show()
                    Log.d("USSD", "Oops. Some error was encountered, try again in a bit")

                }
                _onRefreshProgress.value = _onRefreshProgress.value?.plus(1)
            } catch (e: Exception) {
                when (e) {

                    is HttpException -> {
                        val body: ResponseBody = e.response()!!.errorBody()!!

                        val errorConverter: Converter<ResponseBody, ErrorBody> =
                            Api.retro.responseBodyConverter(
                                ErrorBody::class.java, arrayOfNulls<Annotation>(0)
                            )


                        try {
                            val errorBody = errorConverter.convert(body)
                            Log.d("USSD", "HVM Http failure: $errorBody")

                        } catch (e: IOException) {
                            Log.d("USSD", "HVM Http failure x2: $e")

                        }

                        Log.d("USSD", "HVM Http failure: $e")


                    }
                    else -> {
                        Log.d("USSD", "HVM general failure:  $e")

                    }
                }
            }
        }
    }

    fun startYangaCheckerAutoBuy(
        token: String,
        yangaDiscount: DISCOUNT_RATE,
        buyFrequency: String
    ) {

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val inputData: Data = Data.Builder()
            .putString(TOKEN, token)
            .putString(FREQUENCY, buyFrequency)
            .putInt(PRICE, yangaDiscount.price)
            .build()

        val repeatingCheckerRequest =
            PeriodicWorkRequestBuilder<RoutineCheckerAutoBuyWorker>(15, TimeUnit.MINUTES)
                .setInputData(inputData)
                .setConstraints(constraints)
                .addTag(PRICE + "-" + yangaDiscount.price.toString())
                .addTag("$FREQUENCY-$buyFrequency")
                .build()



        WorkManager.getInstance().enqueueUniquePeriodicWork(
            RoutineCheckerAutoBuyWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            repeatingCheckerRequest
        )


    }

    fun checkBalanceAndUsage(token: String) {
        viewModelScope.launch {
            val repo = Repository(dataSource)
            try {

                val result = repo.getBalanceAndUsage(token)

                if (result.status == "1") {
//                    val bundle4Gb = result.data.accountBalanceList
                    _balanceNusage.value = result.data
//                    _bundle.value = bundle4Gb

                } else {
//                    Toast.makeText(context, "Oops. Some error was encountered, try again in a bit", Toast.LENGTH_SHORT).show()
                    Log.d("USSD", "Oops. Some error was encountered, try again in a bit")

                }
                _onRefreshProgress.value = _onRefreshProgress.value?.plus(1)


            } catch (e: Exception) {
                when (e) {

                    is HttpException -> {
                        val body: ResponseBody = e.response()!!.errorBody()!!

                        val errorConverter: Converter<ResponseBody, ErrorBody> =
                            Api.retro.responseBodyConverter(
                                ErrorBody::class.java, arrayOfNulls<Annotation>(0)
                            )


                        try {
                            val errorBody = errorConverter.convert(body)
                            Log.d("USSD", "HVM Http failure: $errorBody")

                        } catch (e: IOException) {
                            Log.d("USSD", "HVM Http failure x2: $e")

                        }

                        Log.d("USSD", "HVM Http failure: $e")


                    }
                    else -> {
                        Log.d("USSD", "HVM general failure:  $e")

                    }
                }
            }
        }

    }

    fun refreshAllBalances(token: String) {
        checkYanga(token)
        checkBalanceAndUsage(token)
    }
}


class HomeViewModelFactory(
    private val dataSource: YangaDatabase,
    private val token: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        @Suppress("unchecked_cast")
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(dataSource, token) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}