package com.sillyapps.meantime.data

import androidx.databinding.BaseObservable
import androidx.databinding.Observable
import androidx.lifecycle.MutableLiveData

class PropertyAwareMutableLiveData<T: BaseObservable?>(baseValue: T? = null): MutableLiveData<T>() {

    private val callback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            value = value!!
        }
    }

    init {
        if (baseValue != null)
            value = baseValue
    }

    override fun setValue(value: T?) {
        super.setValue(value)

        value?.addOnPropertyChangedCallback(callback)
    }
}