package com.chimzeart.yangachecker.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chimzeart.yangachecker.R
import com.chimzeart.yangachecker.TAG
import com.chimzeart.yangachecker.database.YangaBundle
import com.chimzeart.yangachecker.database.YangaDatabase
import com.chimzeart.yangachecker.network.Api
import com.chimzeart.yangachecker.network.ErrorBody
import com.chimzeart.yangachecker.network.Repository
import com.chimzeart.yangachecker.ui.TOKEN
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.HttpException
import java.io.IOException

const val KEY_RESULT = "result"

class RoutineCheckWorker(
    private val appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    companion object {
        const val WORK_NAME = "com.chimzeart.yangachecker.routinechecker"
    }
    override suspend fun doWork(): Result {
        Log.d(TAG,"Periodic work check...")
        val repo = Repository(YangaDatabase.getInstance(applicationContext))
        val token = inputData.getString(TOKEN)!!

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel =  NotificationChannel("Yanga Notification", "Yanga Notification", IMPORTANCE_HIGH)
            val manager = appContext.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(notificationChannel)
        }
        try {

            val result = repo.getBundles(token)

            if (result.status == "1"){
                val bundle4Gb = result.data[0].bundleList[0]
//                Toast.makeText(appContext, bundle4Gb.title, Toast.LENGTH_SHORT).show()

                if (bundle4Gb.price <= 750)
                    displayNotification(bundle4Gb.title)

                val yangaBundle = YangaBundle(
                    bundleCacheId =  bundle4Gb.bundleCacheId,
                    cacheExpiry = bundle4Gb.cacheExpiry,
                    validity = bundle4Gb.validity,
                    title = bundle4Gb.title,
                    price = bundle4Gb.price,
                    name = bundle4Gb.name,
                    size = bundle4Gb.size,
                    shouldAutoBuy = false)

                repo.insertYangaBundle(yangaBundle)

            }else{
//                    Toast.makeText(context, "Oops. Some error was encountered, try again in a bit", Toast.LENGTH_SHORT).show()
                Log.d("USSD", "Oops. Some error was encountered, try again in a bit")

            }

//            val output: Data = workDataOf(KEY_RESULT to result.data)


            return Result.success()

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

    private fun displayNotification(title: String) {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NotificationCompat.Builder(appContext,"Yanga Notification")
                .setContentTitle("4GB Yanga Bundle")
                .setContentText(title)
                .setPriority(IMPORTANCE_DEFAULT)
                .setSmallIcon(R.drawable.ic_launcher_foreground)

        } else {
            NotificationCompat.Builder(appContext,"Yanga Notification")
                .setContentTitle("4GB Yanga Bundle")
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