package com.chimzeart.yangachecker

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.chimzeart.yangachecker.workers.RoutineCheckWorker
import java.util.concurrent.TimeUnit


const val TAG = "USSD"
class UssdReceiver: AccessibilityService() {

    /**
     * Callback for [android.view.accessibility.AccessibilityEvent]s.
     *
     * @param event The new event. This event is owned by the caller and cannot be used after
     * this method returns. Services wishing to use the event after this method returns should
     * make a copy.
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        Log.d(TAG, "onAccessibilityEvent")

        event?.let{
//        String text = event.getText().toString();
            val text = it.text.toString()

        if (it.className.equals("android.app.AlertDialog")){
            val discount = text.substringAfter("1.").substringBefore("2").trim()

            Log.d(TAG, discount )
            val nodeInfo = it.source
            when {
                text.contains("REGISTERED") -> {
                    selectOption(nodeInfo, "2")
                }
                text.contains("NEW") -> {
                    selectOption(nodeInfo, "1")
                }
                text.contains("4GB") -> {
                    Toast.makeText(applicationContext, discount, Toast.LENGTH_SHORT).show()
                    val list: List<AccessibilityNodeInfo> =
                        nodeInfo.findAccessibilityNodeInfosByText("Cancel")


                    for (node in list) {
                        node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    }
                }
            }


        }


        if(it.className.equals("android.widget.Button")){
            when{
                text.contains("start",true)->{
                    scheduleWork()
                }
                text.contains("stop",true)->{
                    Toast.makeText(applicationContext, "Stopped yanga checker service", Toast.LENGTH_SHORT).show()
                    WorkManager.getInstance().cancelUniqueWork(RoutineCheckWorker.WORK_NAME)

                }
            }

        }
    }

    }

    private fun selectOption(nodeInfo: AccessibilityNodeInfo, selection: String) {
        val nodeInput = nodeInfo.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        val bundle = Bundle()
        bundle.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, selection)
        nodeInput.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, bundle)
        nodeInput.refresh()

        val list: List<AccessibilityNodeInfo> =
            nodeInfo.findAccessibilityNodeInfosByText("Send")


        for (node in list) {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
    }

    /**
     * Callback for interrupting the accessibility feedback.
     */
    override fun onInterrupt() {
        Log.d(TAG, "onInterrupt")

    }

    override fun onServiceConnected() {
        Log.d(TAG, "onServiceConnected")

    }

    private fun scheduleWork() {
        val repeatingCheckerRequest = PeriodicWorkRequestBuilder<RoutineCheckWorker>(15, TimeUnit.MINUTES)
            .build()

        Toast.makeText(applicationContext, "Started yanga checker service", Toast.LENGTH_SHORT).show()

        Log.d("USSD","Periodic Work request for yanga checker set")

        WorkManager.getInstance().enqueueUniquePeriodicWork(
            RoutineCheckWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingCheckerRequest)
    }
}