package com.example.bugs_android.logic

import com.example.bugs_android.R
import com.example.bugs_android.enumC.BugType
import com.example.bugs_android.model.Bug

fun createBug(type: BugType, id: Int, x: Float, y: Float): Bug{
    return when(type){
        BugType.NORMAL -> Bug(id, x, y, 100.0f, 80, type, 10, R.drawable.normal)
        BugType.FAST -> Bug(id, x, y, 200.0f, 60, type, 20, R.drawable.fast)
        BugType.RARE -> Bug(id, x, y, 150.0f, 40, type, 50, R.drawable.rare)
    }
}