package com.example.firstdemo

import android.util.Log

class LoggingRepository(val repository: Repository): Repository by repository {
    override fun getData(): String {
        Log.e("LoggingRepository", "getData: ")
        return repository.getData()
    }
}