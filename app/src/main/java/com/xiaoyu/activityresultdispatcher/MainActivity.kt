package com.xiaoyu.activityresultdispatcher

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.xiaoyu.annotation.ResultDispatch
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button.setOnClickListener {
            startActivityForResult(
                Intent(this, MainActivity2::class.java),
                10
            )
        }
    }

    @ResultDispatch(10, 20)
    fun testDispatcher() {
        Log.d("Main", "回来了，没有Intent")
    }

    @ResultDispatch(10, 30)
    fun testDispatcher(intent: Intent?) {
        Log.d("Main", "wow,这是传递过来的信息${intent?.getStringExtra("text")}")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        MainActivityDispatcher.dispatch(this, requestCode, resultCode, data)
    }
}