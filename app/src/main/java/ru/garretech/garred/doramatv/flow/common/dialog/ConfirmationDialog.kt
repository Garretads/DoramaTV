package ru.garretech.garred.doramatv.flow.common.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import ru.garretech.garred.doramatv.R


class ConfirmationDialog : DialogFragment() {
    private var titleText: String? = null
    private var contentText: String? = null
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            titleText = it.getString(TITLE_ARG)
            contentText = it.getString(CONTENT_ARG)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_confirmation, container, false)
        val cancelButton = view.findViewById<Button>(R.id.confirmationCancelButton)
        val acceptButton = view.findViewById<Button>(R.id.confirmationAcceptButton)
        val messageContentText = view.findViewById<TextView>(R.id.messageContent)

        dialog?.setTitle(titleText)
        messageContentText.text = contentText

        acceptButton.setOnClickListener {
            listener?.onAcceptPressed()
            dismiss()
        }

        cancelButton.setOnClickListener {
            listener?.onCancelPressed()
            dismiss()
        }

        return view
    }


    fun setConfirmationListener(listener : OnFragmentInteractionListener) {
        this.listener = listener
    }



    override fun onDetach() {
        super.onDetach()
        listener = null
    }


    interface OnFragmentInteractionListener {
        fun onAcceptPressed()
        fun onCancelPressed()
    }

    companion object {

        private const val TITLE_ARG = "title"
        private const val CONTENT_ARG = "content"

        @JvmStatic
        fun newInstance(title: String, content: String) =
            ConfirmationDialog().apply {
                arguments = Bundle().apply {
                    putString(TITLE_ARG, title)
                    putString(CONTENT_ARG, content)
                }
            }
    }
}
