package cn.intret.lab.loadingtogglebutton

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
    }

    private fun initViews() {
        // toggle1
        log1.text = getCheckDesc(toggle1.isChecked)
        toggle1.setOnCheckChangeListener { buttonView, isChecked -> log1.text = getCheckDesc(isChecked) }
        log1.setOnClickListener { toggle1.toggle() }

        // toggle2

    }

    private fun getCheckDesc(isChecked: Boolean) : String =
            (if (isChecked) "It's checked ( click me! )" else "It's unchecked")
}
