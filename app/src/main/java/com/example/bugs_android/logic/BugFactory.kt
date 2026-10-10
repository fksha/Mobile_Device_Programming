package com.example.bugs_android.logic

import com.example.bugs_android.R
import com.example.bugs_android.enumC.BugType
import com.example.bugs_android.model.Bug


fun createBug(type: BugType, id: Int, x: Float, y: Float): Bug{
    return when(type){
        BugType.NORMAL -> Bug(id, x, y, 200.0f, 300, type, 5, R.drawable.bug_normal, 0f )
        BugType.FAST -> Bug(id, x, y, 900.0f, 250, type, 10, R.drawable.bug_fast, 0f)
        BugType.RARE -> Bug(id, x, y, 450.0f, 200, type, 20, R.drawable.bug_rare, 0f)
    }
}

