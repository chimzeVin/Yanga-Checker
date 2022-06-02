package com.chimzeart.yangachecker.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.security.KeyStore
import java.security.SecureRandom
import javax.net.ssl.*
import javax.security.cert.CertificateException


private const val BASE_URL =
    "https://yangabundles.tnm.co.mw/ccc-p/"


private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .client(getUnsafeOkHttpClient())
    .build()

interface ApiService {

    @GET("dt/subscriber/balancesandusage")
    suspend fun getBalanceAndUsage(@Query("token") token:String):UsageResponse

    @GET("dt/bundles/categorised")
    suspend fun getBundles(@Query("token") token:String):BundlesResponse

    @POST("dt/bundles")
    suspend fun buyBundle(@Query("token") token:String,
                          @Body body: BuyBundleRequest): BuyBundleResponse

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )

    @POST("st/verify")
    suspend fun verifyNumber(@Body body: VerifyRequest): VerifyResponse


    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @POST("st/confirm")
    suspend fun confirmOTP(@Body body: OtpConfirmRequest): ConfirmResponse
}
private fun getUnsafeOkHttpClient(): OkHttpClient {
    return try {
        // Create a trust manager that does not validate certificate chains
        val trustAllCerts = arrayOf<TrustManager>(
            object : X509TrustManager {
                @Throws(CertificateException::class)
                override fun checkClientTrusted(
                    chain: Array<out java.security.cert.X509Certificate>?,
                    authType: String?
                ) {
                }

                @Throws(CertificateException::class)
                override fun checkServerTrusted(
                    chain: Array<out java.security.cert.X509Certificate>?,
                    authType: String?
                ) {
                }

                override fun getAcceptedIssuers(): Array<out java.security.cert.X509Certificate>? {
                    return arrayOf()
                }
            }
        )

        // Install the all-trusting trust manager
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        // Create an ssl socket factory with our all-trusting manager
        val sslSocketFactory = sslContext.socketFactory
        val trustManagerFactory: TrustManagerFactory =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(null as KeyStore?)
        val trustManagers: Array<TrustManager> =
            trustManagerFactory.trustManagers
        check(!(trustManagers.size != 1 || trustManagers[0] !is X509TrustManager)) {
            "Unexpected default trust managers:" + trustManagers.contentToString()
        }

        val trustManager =
            trustManagers[0] as X509TrustManager


        val builder = OkHttpClient.Builder()
        builder.sslSocketFactory(sslSocketFactory, trustManager)
        builder.hostnameVerifier(HostnameVerifier { _, _ -> true })
        builder.build()
    } catch (e: Exception) {
        throw RuntimeException(e)
    }
}
object Api {
    val retrofitService : ApiService by lazy {
        retrofit.create(ApiService::class.java) }
    val retro: Retrofit by lazy { retrofit }
}