package com.senjuid.location

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class GeolocationViewModelFactory(private val appContext: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GeolocationViewModel::class.java)) {
            return GeolocationViewModel(appContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}