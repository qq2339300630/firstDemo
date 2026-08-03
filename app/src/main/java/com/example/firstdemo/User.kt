package com.example.firstdemo

import android.util.Log
import kotlin.properties.Delegates

class User {
    val name:String by Delegates.observable("<未命名>") {
        property, oldValue, newValue ->
        Log.e("User","属性 ${property.name}: '$oldValue'-> '$newValue'")
    }
}

 class Person {
     var age : Int by Delegates.vetoable(0) {
         property,oldValue,newValue ->
         newValue >= 0
     }
     init {
         val config = Config()
         config.host
     }
 }

class Config {
    val host: String by Delegates.notNull()
}

class MapUser(map : Map<String,Any?>) {
    val name:String by map
    val age: Int by map
}