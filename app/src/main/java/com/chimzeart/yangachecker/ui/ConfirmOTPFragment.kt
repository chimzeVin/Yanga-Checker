package com.chimzeart.yangachecker.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.chimzeart.yangachecker.MainViewModel
import com.chimzeart.yangachecker.R
import com.chimzeart.yangachecker.database.YangaDatabase
import com.chimzeart.yangachecker.databinding.FragmentConfirmOtpBinding
import com.chimzeart.yangachecker.network.Api
import com.chimzeart.yangachecker.network.ErrorBody
import com.chimzeart.yangachecker.network.OtpConfirmRequest
import com.chimzeart.yangachecker.network.Repository
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.HttpException
import java.io.IOException

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class ConfirmOTPFragment : Fragment() {

    private var _binding: FragmentConfirmOtpBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val mainViewModel: MainViewModel by activityViewModels()
private lateinit var sharedPref: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
         sharedPref = requireActivity().getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        _binding = FragmentConfirmOtpBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.confirmButton.setOnClickListener {
            lifecycleScope.launch{
                val application = requireNotNull(this@ConfirmOTPFragment.activity).application

                val repo = Repository(YangaDatabase.getInstance(application))
                try {

                    val otp = binding.otpText.text.toString()
                    val msisdn = mainViewModel.msisdn
                    val otpConfirmRequest = OtpConfirmRequest(msisdn, otp)

                    val result = repo.confirmOTP(otpConfirmRequest)



                    if (result.status == "1"){
                        Log.d("USSD", result.data.token)
                        requireActivity().getSharedPreferences(
                            getString(R.string.preference_file_key), Context.MODE_PRIVATE)
                        with(sharedPref.edit()){
                            putString(getString(R.string.saved_token_key), result.data.token)
                            putString(getString(R.string.saved_msisdn_key), msisdn)
                            apply()
                        }
                        findNavController().navigate(R.id.action_SecondFragment_to_homeFragment)


                    }else{
                        Toast.makeText(context, "Oops. Some error was encountered, try again in a bit", Toast.LENGTH_SHORT).show()
                    }
                    Log.d("USSD", result.status)

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
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}