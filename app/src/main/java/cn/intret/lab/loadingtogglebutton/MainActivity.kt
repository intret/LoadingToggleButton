package cn.intret.lab.loadingtogglebutton

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cn.intret.lab.library.LoadingToggleButton
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

        // toggle2 toggle3
        toggle2.setOnCheckChangeListener { buttonView, isChecked -> toggle_round1.isChecked = isChecked }

        // stop flicking
        updateFlickTextButton(toggle_round3)
        btn_stop_flick.setOnClickListener { v ->
            if (toggle_round3.isRunning) {
                toggle_round3.stop()
            } else {
                toggle_round3.start()
            }
            updateFlickTextButton(toggle_round3)
        }
    }

    private fun getCheckDesc(isChecked: Boolean): String =
            (if (isChecked) "It's checked ( click me! )" else "It's unchecked")

    fun updateFlickTextButton(view: LoadingToggleButton) {
        if (!view.isRunning) btn_stop_flick.setText("START FLICK") else btn_stop_flick.setText("STOP FLICK")
    }
}
