package com.example.firstdemo

import android.util.Log

class HeavyResource {
    init {
        Log.e("HeavyResource","HeavyResource created!")
    }
}

class ViewModel {
    val resource: HeavyResource by lazy {
        Log.e("HeavyResource","Initializing")
        HeavyResource()
    }

    val fastResource: HeavyResource by lazy(LazyThreadSafetyMode.NONE) {
        HeavyResource()
    }
}