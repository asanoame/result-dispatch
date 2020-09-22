package com.xiaoyu.activityresultdispatcher

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main2.*

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        back.setOnClickListener {
            setResult(20)
            finish()
        }
        backIntent.setOnClickListener {
            setResult(30, Intent().apply {
                putExtra("text", "我是从MainActivity2带回来的信息")
            })
            finish()
        }
    }
}