package com.chimzeart.yangachecker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chimzeart.yangachecker.database.YangaBundle
import com.chimzeart.yangachecker.database.YangaDatabase
import com.chimzeart.yangachecker.network.*
import com.chimzeart.yangachecker.ui.FREQUENCY
import com.chimzeart.yangachecker.ui.HomeViewModel
import com.chimzeart.yangachecker.ui.PRICE
import com.chimzeart.yangachecker.ui.TOKEN
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.HttpException
import java.io.IOException

const val KEY_RESULT_AUTO_BUY = "result_auto_buyer"

class RoutineCheckerAutoBuyWorker(
    private val appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    companion object {
        const val WORK_NAME = "com.chimzeart.yangachecker.autoBuyer"
    }
    override suspend fun doWork(): Result {
        Log.d(TAG,"Periodic work check with autobuy...")

        val database = YangaDatabase.getInstance(applicationContext)
        val repo = Repository(database)
        val token = inputData.getString(TOKEN)!!
        val buyFrequency = inputData.getString(FREQUENCY)!!
        val price: Int = inputData.getInt(PRICE, 300)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel =  NotificationChannel("Yanga Notification", "Yanga Notification", IMPORTANCE_HIGH)
            val manager = appContext.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(notificationChannel)
        }
        try {

            val result: BundlesResponse = repo.getBundles(token)

            if (result.status == "1"){
                val bundle4Gb: Bundle = result.data[0].bundleList[0]
//                val bundle4Gb: Bundle = result.data[0].bundleList[3]
                Log.d("USSD", "Result from within worker ${bundle4Gb.title}")
//                Toast.makeText(appContext, bundle4Gb.title, Toast.LENGTH_SHORT).show()

                val yangaBundle = YangaBundle(
                    bundleCacheId =  bundle4Gb.bundleCacheId,
                    cacheExpiry = bundle4Gb.cacheExpiry,
                    validity = bundle4Gb.validity,
                    name = bundle4Gb.name,
                    title = bundle4Gb.title,
                    price = bundle4Gb.price,
                    size = bundle4Gb.size,
                    shouldAutoBuy = false)
                repo.insertYangaBundle(yangaBundle)
                Log.d("USSD", "${bundle4Gb.price} - $price")

                if (bundle4Gb.price <= price){
                    val buyBundleRequest = BuyBundleRequest(
                        bundleCacheId = yangaBundle.bundleCacheId,
                        price = yangaBundle.price,
                        size = yangaBundle.size,
                        token = token,

                    )
                    for (i in 1..buyFrequency.toInt()){

                        val buyResponse = repo.buyBundle(token,buyBundleRequest)
                        Log.d("USSD", "Result from within worker ${buyResponse}")

                        if (buyResponse.status == "1"){
                            displayNotification(bundle4Gb.title, "Successful")

                        }
                        else{
                            displayNotification("Insufficient Funds", "Failed")
                            break
                        }

                        Log.d(TAG,"Before sleep")
                        SystemClock.sleep(20000)
                        Log.d(TAG,"After sleep")

                        if(i == buyFrequency.toInt())
                            HomeViewModel.startYangaChecker(token)

                    }
//                    WorkManager.getInstance().cancelUniqueWork(RoutineCheckWorker.WORK_NAME)

                }



                return Result.success()

            }else{
//                    Toast.makeText(context, "Oops. Some error was encountered, try again in a bit", Toast.LENGTH_SHORT).show()
                Log.d("USSD", "Oops. Some error was encountered, try again in a bit")
                return Result.success()
            }



        }
        catch (e:Exception){
            when (e){

                is HttpException ->{
                    val body: ResponseBody = e.response()!!.errorBody()!!

                    val errorConverter: Converter<ResponseBody, ErrorBody> =
                        Api.retro.responseBodyConverter(
                            ErrorBody::class.java, arrayOfNulls<Annotation>(0))


                    try {
                        val errorBody = errorConverter.convert(body)
                        Log.d("USSD","Http failure: $errorBody")

                    }catch (e: IOException){
                        Log.d("USSD","Http failure x2: $e")

                    }

                    Log.d("USSD","Http failure: $e")





                }
                else-> {
                    Log.d("USSD","upload failure:  $e")

                }
            }
            return Result.failure()
        }

    }

    private fun displayNotification(title: String, successText:String) {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NotificationCompat.Builder(appContext,"Yanga Notification")
                .setContentTitle("4GB Yanga Bundle Purchase $successText")
                .setContentText(title)
                .setPriority(IMPORTANCE_HIGH)
                .setSmallIcon(R.drawable.ic_launcher_foreground)

        } else {
            NotificationCompat.Builder(appContext,"Yanga Notification")
                .setContentTitle("4GB Yanga Bundle Purchase $successText")
                .setContentText(title)
//                .setico(R.drawable.ic_launcher_foreground)
        }
//        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N){
//            Notification.Action(R.drawable.ic_launcher_foreground,title, PendingIntent.getActivity(appContext, 0, Intent(),0))
//        }

        val managerCompat = NotificationManagerCompat.from(appContext)
        managerCompat.notify(1,builder.build())
    }


}