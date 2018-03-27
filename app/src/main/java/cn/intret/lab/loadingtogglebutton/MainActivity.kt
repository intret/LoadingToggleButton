package cn.intret.lab.loadingtogglebutton

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import cn.intret.lab.library.LoadingToggleButton
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
    }

    private fun initViews() {
        // ------------------------------------------------
        // A rectangle toggle
        // ------------------------------------------------
        log1.text = getCheckDesc(toggle_rect1.isChecked)
        toggle_rect1.setOnCheckChangeListener { buttonView, isChecked -> log1.text = getCheckDesc(isChecked) }
        log1.setOnClickListener { toggle_rect1.toggle() }


        toggle_rect2.setOnCheckChangeListener { buttonView, isChecked -> toggle_round1.isChecked = isChecked }

        // ------------------------------------------------
        // A toggle with flicking animation
        // ------------------------------------------------

        // handle toggle checked event
        toggle_flicking.setOnCheckChangeListener { toggleButton, isChecked ->
            updateLoadingStatus(toggle_flicking, tv_flicking_status)
            updateLoadingTextButton(toggle_flicking, btn_stop_flick)
        }

        // handle toggle loading event
        toggle_flicking.setOnLoadingChangeListener { toggleButton, loading ->
            updateLoadingStatus(toggle_flicking, tv_flicking_status)
            updateLoadingTextButton(toggle_flicking, btn_stop_flick)
        }

        // set toggle initial status
        updateLoadingTextButton(toggle_flicking, btn_stop_flick)

        // interacted with Loading Toggle Button
        btn_stop_flick.setOnClickListener { v ->
            if (toggle_flicking.isRunning) {
                toggle_flicking.stop()
            } else {
                toggle_flicking.start()
            }
        }

        // ------------------------------------------------
        // A toggle with loading animation
        // ------------------------------------------------

        toggle_line_spinner.setOnCheckChangeListener { toggleButton, isChecked ->
            updateLoadingTextButton(toggle_line_spinner, btn_stop_loading)
            updateLoadingStatus(toggle_line_spinner, tv_loading_status)
        }
        toggle_line_spinner.setOnLoadingChangeListener { toggleButton, loading ->
            updateLoadingTextButton(toggle_line_spinner, btn_stop_loading)
            updateLoadingStatus(toggle_line_spinner, tv_loading_status)
        }

        updateLoadingTextButton(toggle_line_spinner, btn_stop_loading)

        btn_stop_loading.setOnClickListener { v ->
            if (toggle_line_spinner.isRunning) {
                toggle_line_spinner.stop()
            } else {
                toggle_line_spinner.start()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateLoadingStatus(loadingToggleButton: LoadingToggleButton, textView: TextView) {
        textView.text =
                "isLoading = " + loadingToggleButton.isLoading + "\r\n" +
                "isChecked = " + loadingToggleButton.isChecked
    }

    private fun getCheckDesc(isChecked: Boolean): String =
            (if (isChecked) "It's checked ( click me! )" else "It's unchecked")

    @SuppressLint("SetTextI18n")
    private fun updateLoadingTextButton(toggle_line_spinner: LoadingToggleButton, textView: TextView) {
        when {
            !toggle_line_spinner.isRunning -> textView.text = "START"
            else -> textView.text = "STOP"
        }
    }

}
