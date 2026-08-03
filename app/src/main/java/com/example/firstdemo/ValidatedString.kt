package com.example.firstdemo

import kotlin.reflect.KProperty

class ValidatedString(private val minLength:Int) {
    private var value: String = ""

    operator fun getValue(thisRef: Any?,process: KProperty<*>): String {
        return value
    }

    operator fun setValue(thisRef:Any?,property: KProperty<*>,value:String) {
        this.value = value
    }
}

class Account {
    val password: String by ValidatedString(8)
}