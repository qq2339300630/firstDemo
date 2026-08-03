package com.example.firstdemo

interface Repository {
    fun getData(): String
    fun saveData(data: String)
}