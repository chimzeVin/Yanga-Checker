package com.chimzeart.yangachecker.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Selection
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.chimzeart.yangachecker.MainViewModel
import com.chimzeart.yangachecker.R
import com.chimzeart.yangachecker.database.YangaDatabase
import com.chimzeart.yangachecker.databinding.FragmentVerifyNumberBinding
import com.chimzeart.yangachecker.network.Api
import com.chimzeart.yangachecker.network.ErrorBody
import com.chimzeart.yangachecker.network.Repository
import com.chimzeart.yangachecker.network.VerifyRequest
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.HttpException


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class VerifyNumberFragment : Fragment() {

    private var _binding: FragmentVerifyNumberBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val mainViewModel: MainViewModel by activityViewModels()
    private var token : String? = null
    private var number: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPref = requireActivity().getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        token = sharedPref.getString(getString(R.string.saved_token_key), null)
        number = sharedPref.getString(getString(R.string.saved_msisdn_key), "")

        token?.let {
            number?.let { it1 -> mainViewModel.setMsisdn(it1) }
//            findNavController().navigate(R.id.action_FirstFragment_to_homeFragment)
//            findNavController().popBackStack(R.id.action_FirstFragment_to_homeFragment, true)
        }

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentVerifyNumberBinding.inflate(inflater, container, false)

        token?.let {
//            Toast.makeText(context, "Token Already Available", Toast.LENGTH_SHORT).show()
//            binding.phoneNumberButton.isEnabled = false
            binding.editTextPhone.setText(number)
        }
        binding.lifecycleOwner = viewLifecycleOwner


        binding.editTextPhone.addCountryCode()
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.phoneNumberButton.setOnClickListener {
                    lifecycleScope.launch{
                        val application = requireNotNull(this@VerifyNumberFragment.activity).application
                        val repo = Repository(YangaDatabase.getInstance(application))
                        try {

                            val userNumber = binding.editTextPhone.text.toString()
                            val body = VerifyRequest(userNumber)
                            //265885437025
                            val result = repo.verifyNumber(body)
                            if (result.status == "1"){
                                mainViewModel.setMsisdn(body.msisdn)
                                findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)

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


                                    val errorBody = errorConverter.convert(body)

                                    Log.d("USSD","Http failure: $errorBody")





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
    fun requestPermissions(context: Context){
        val permissions = arrayOf(
            Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE)

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(requireActivity(), permissions, 1)
        }

    }
}

private fun EditText.addCountryCode() {

    this.setText("265")
    Selection.setSelection(this.text, this.text.length)

    this.addTextChangedListener {
        if(!it.toString().startsWith("265")){
            this.setText("265")
            Selection.setSelection(this.text, this.text.length)

        }
    }
}
