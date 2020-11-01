package com.sillyapps.meantime.data

import androidx.databinding.BaseObservable
import androidx.databinding.Observable
import androidx.lifecycle.MutableLiveData

class PropertyAwareMutableLiveData<T: BaseObservable?>: MutableLiveData<T>() {

    override fun setValue(value: T) {
        super.setValue(value)

        value?.addOnPropertyChangedCallback(callback)
    }

    private val callback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            setValue(value!!)
        }
    }
}