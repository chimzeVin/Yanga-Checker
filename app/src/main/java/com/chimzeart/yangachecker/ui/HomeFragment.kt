package com.chimzeart.yangachecker.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.chimzeart.yangachecker.MainViewModel
import com.chimzeart.yangachecker.R
import com.chimzeart.yangachecker.TAG
import com.chimzeart.yangachecker.database.YangaDatabase
import com.chimzeart.yangachecker.databinding.FragmentHomeBinding
import com.chimzeart.yangachecker.workers.RoutineCheckWorker
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

enum class DISCOUNT_RATE(val price:Int){
    Yanga90(300),
    Yanga85(450),
    Yanga80(600),
    Yanga75(750)
}
class HomeFragment : Fragment(), View.OnClickListener {



    private lateinit var viewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var token : String? = null
    private var number: String? = ""
    private var yangaDiscount = DISCOUNT_RATE.Yanga90
    private val mainViewModel: MainViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPref = requireActivity().getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        token = sharedPref.getString(getString(R.string.saved_token_key), null)
        number = sharedPref.getString(getString(R.string.saved_msisdn_key), "")
//        findNavController().backst
//        val kasd = find

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: created")
        _binding = FragmentHomeBinding.inflate(inflater)

        val application = requireNotNull(this.activity).application
        val dataSource = YangaDatabase.getInstance(application)

        val viewModelFactory = HomeViewModelFactory(dataSource,token!! )
        viewModel = ViewModelProvider(this, viewModelFactory)[HomeViewModel::class.java]

        viewModel.checkBalanceAndUsage(token!!)


//        binding.discountSpinner.onItemSelectedListener = this
        binding.button90.setOnClickListener(this)
        binding.button85.setOnClickListener(this)
        binding.button80.setOnClickListener(this)
        binding.button75.setOnClickListener(this)

        var phoneNumber = "0" + mainViewModel.msisdn.substring(3,12)
        binding.numberText.text = phoneNumber
        isWorkRunning()

//        updateUI(isWorkRunning())
        binding.startCheckerButton.setOnClickListener {
//            val workInfos = WorkManager.getInstance().getWorkInfosForUniqueWork(RoutineCheckWorker.WORK_NAME)
            val button = it as Button
            if (button.text == "Start Checker")
                HomeViewModel.startYangaChecker(token!!)
            else if (button.text == "Stop Checker")
                WorkManager.getInstance().cancelUniqueWork(RoutineCheckWorker.WORK_NAME)


        }


        viewModel.bundleDiscount.observe(viewLifecycleOwner){
            val dateformat = SimpleDateFormat("HH:mm:ss")

            val time = dateformat.format(Date())
            val stringA = it.title.substringBefore("discount").trim()
            val size = stringA.substringBefore(",")
            val discount = stringA.substringAfter(",")
            val price = it.title.substringAfter("@").substringBefore(",")

            binding.discountValueText.text = "$size - $discount"
            binding.bundleTextView.text = "$price checked at $time"
//            binding.bundleTextView.text = "${it.title} checked at $time"
        }

        viewModel.balanceNusage.observe(viewLifecycleOwner){
            binding.balanceTextView.text = it.accountBalances.accountBalanceList[0].displayBalance

            var totalBundle = 0.0
            var usedBundle = 0.0
            for (bundle in it.bundleUsage){
                totalBundle += bundle.thresholdValue
                usedBundle += bundle.usedValue
//                remainingBundle += bundle.remainingValue
            }
            var remainingBundle: Double = totalBundle - usedBundle

            //265887053883
            val megabyte = 1024 * 1024

            totalBundle /= megabyte
            totalBundle= (totalBundle * 100.0).roundToInt() / 100.0

            usedBundle /= megabyte
            usedBundle = (usedBundle * 100.0).roundToInt() / 100.0
            remainingBundle /= megabyte
            remainingBundle= (remainingBundle * 100.0).roundToInt() / 100.0

            val balances =
                        "BUNDLE USAGE\n" +
                        "Remaining: $remainingBundle MB\n" +
                        "Used: $usedBundle MB\n" +
                        "Total: $totalBundle MB"

            val numOfBundles = it.bundleUsage.size
            val elapsedTime = DateUtils.formatElapsedTime(it.bundleUsage[numOfBundles-1].secondsRemaining.toLong())


            binding.timeRemainingText.text = "$elapsedTime hrs remaining"
            binding.remainingVolumeText.text = "${remainingBundle.toInt()}MB"
            binding.usedVolumeText.text = "${usedBundle.toInt()}MB"
            binding.totalVolumeText.text = "${totalBundle.toInt()}MB"

            binding.circularProgressBar.progressMax = totalBundle.toFloat()
            binding.circularProgressBar.setProgressWithAnimation(usedBundle.toFloat(), 500)
        }

        binding.autobuyButton.setOnClickListener {
            val buyFrequency = binding.frequencyEditText.text.toString()
            viewModel.startYangaCheckerAutoBuy(token!!, yangaDiscount, buyFrequency)

        }

        viewModel.yangaBundles.observe(viewLifecycleOwner){
            yangaBundles ->



            val size = yangaBundles.size
            if (size>0){
                val currentBundle = yangaBundles[size-1]

                Toast.makeText(
                    context,
                    "Buy: ${currentBundle.title} ${currentBundle.shouldAutoBuy}",
                    Toast.LENGTH_SHORT
                ).show()


            }
        }
        return binding.root
    }



    private fun isWorkRunning() {
        val workInfos = WorkManager.getInstance().getWorkInfosForUniqueWorkLiveData(RoutineCheckWorker.WORK_NAME)
        workInfos.observe(viewLifecycleOwner){
            if (it.size>0){
                val state = it[0].state

                val tags: MutableSet<String> = it[0].tags
                var frequency = ""
                var price = ""
                for (tag in tags){
                    if (tag.contains(FREQUENCY))
                    {
                        frequency = tag.substringAfter("-")
                    }

                    if (tag.contains(PRICE)){
                        price = tag.substringAfter("-")
                    }
                }


                if (frequency.isNotEmpty() && price.isNotEmpty()) {
                    binding.autobuyMessageText.visibility = View.VISIBLE
                    binding.autobuyMessageText.text =
                        "AutoBuy is running. $frequency bundle(s) will be bought when the discount price is at K$price or lower"
                }else {
                    binding.autobuyMessageText.text = ""
                    binding.autobuyMessageText.visibility = View.GONE
                }

                Log.d(TAG, "isWorkRunning: $tags")

                if (state == WorkInfo.State.ENQUEUED) {
                    binding.statusText.text = "The auto-checker is running every 15 minutes"
                    binding.startCheckerButton.text = "Stop Checker"
                } else if (state == WorkInfo.State.CANCELLED) {
                    binding.statusText.text = "Yanga checker is not running"
                    binding.startCheckerButton.text = "Start Checker"
                    binding.autobuyMessageText.text = ""

                }
            }

        }

//        val workInfosByTag = WorkManager.getInstance().getWorkInfosByTagLiveData(RoutineCheckWorker.WORK_NAME)
//        workInfosByTag.observe(viewLifecycleOwner){
//            it[0].tags
//        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


    override fun onClick(v: View?) {
        val highlightedButton = v as MaterialButton
        val priceString = highlightedButton.text.toString().substringAfter("K")
        val price = priceString.toInt()

        val discountButtons: List<MaterialButton> = listOf(binding.button90, binding.button85,binding.button80,binding.button75)

        for (item in discountButtons){

            val itemPriceString = item.text.toString().substringAfter("K")
            val itemPrice = itemPriceString.toInt()

            //
            if (itemPrice <= price){
                val highlightColor =
                    ContextCompat.getColor(requireContext(), R.color.primaryDarkColor)
                item.strokeColor = ColorStateList.valueOf(highlightColor)
                item.setTextColor(highlightColor)
                item.typeface = Typeface.DEFAULT_BOLD
            }else{
                val disabledColor = ContextCompat.getColor(requireContext(), R.color.disabled)
                item.strokeColor = ColorStateList.valueOf(disabledColor)
                item.setTextColor(disabledColor)
                item.typeface = Typeface.DEFAULT

            }

            when (highlightedButton.id){
                R.id.button90 ->{yangaDiscount = DISCOUNT_RATE.Yanga90}
                R.id.button85 ->{yangaDiscount = DISCOUNT_RATE.Yanga85}
                R.id.button80 ->{yangaDiscount = DISCOUNT_RATE.Yanga80}
                R.id.button75 ->{yangaDiscount = DISCOUNT_RATE.Yanga75}
            }



        }




    }

    override fun onStart() {
        super.onStart()
        viewModel.checkYanga(token!!)
        viewModel.checkBalanceAndUsage(token!!)
    }


}