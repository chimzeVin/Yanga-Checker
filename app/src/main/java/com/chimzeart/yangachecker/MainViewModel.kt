package com.chimzeart.yangachecker

import androidx.lifecycle.ViewModel

class MainViewModel:ViewModel() {

    private var _msisdn = ""
    val msisdn:String get() = _msisdn
    fun setMsisdn(msisdn: String) {
        _msisdn = msisdn

    }
}