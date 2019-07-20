package ru.garretech.garred.doramatv.fragments


import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import ru.garretech.garred.doramatv.R


class DisclaimerFragment : androidx.fragment.app.DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_disclaimer, container, false)
        val button = view.findViewById<Button>(R.id.dissmissDisclaimerButton)

        button.setOnClickListener { dismiss() }
        return view
    }
}
