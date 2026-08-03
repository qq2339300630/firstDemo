package com.example.firstdemo

import android.util.Log

class RealRepository: Repository {
    override fun getData(): String {
        return "real data"
    }

    override fun saveData(data: String) {
      Log.e("RealRepository", "saveData: $data")
    }


}