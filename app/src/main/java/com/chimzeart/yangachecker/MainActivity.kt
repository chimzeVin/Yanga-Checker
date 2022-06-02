package com.chimzeart.yangachecker

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.chimzeart.yangachecker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels()
    private var token : String? = null
    private var number: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        startService(Intent(this, UssdReceiver::class.java))
//        isMyServiceRunning(UssdReceiver::class.java)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val sharedPref = getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        token = sharedPref.getString(getString(R.string.saved_token_key), null)
        number = sharedPref.getString(getString(R.string.saved_msisdn_key), "")


            number?.let { it1 -> mainViewModel.setMsisdn(it1) }
//            findNavController().navigate(R.id.action_FirstFragment_to_homeFragment)
//            findNavController().popBackStack(R.id.action_FirstFragment_to_homeFragment, true)




        val navController = findNavController(R.id.nav_host_fragment_content_main)
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)


        if (token.isNullOrEmpty()){
            navGraph.setStartDestination(R.id.FirstFragment)
        }else{
            navGraph.setStartDestination(R.id.homeFragment)

        }
        navController.graph = navGraph
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

//        NavigationUI.setupActionBarWithNavController(this, navController)


    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun isMyServiceRunning(serviceClass : Class<*> ) : Boolean{
        var manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name.equals(service.service.className)) {
                Log.d("USSD", "The service is running")
                return true
            }
        }
        return false
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}