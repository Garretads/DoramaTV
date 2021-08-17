package ru.garretech.garred.doramatv.fragments

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AlertDialog
import android.widget.Toast

import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList
import java.util.regex.Pattern

class SelectQualityFragment : androidx.fragment.app.DialogFragment(), DialogInterface.OnClickListener {
    internal lateinit var sourcesObject: JSONObject
    internal lateinit var sourcesNames: ArrayList<String>
    internal lateinit var sourcesLinks: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            try {

                sourcesObject = JSONObject(requireArguments().getString(ARG_PARAM1))
                sourcesLinks = ArrayList()
                sourcesNames = ArrayList()
                val sourcesOrigNames = ArrayList<String>()

                for (index in 0 until sourcesObject.names().length())
                    sourcesObject.names().getString(index).let {
                        when {
                            it.contains("mp4")    -> {
                                sourcesLinks.add(sourcesObject.getString(it))
                                sourcesOrigNames.add(it)
                                sourcesNames.add(it.substring(4).plus("P"))
                            }
                            it.contains("hls")    -> {
                                sourcesLinks.add(sourcesObject.getString(it))
                                sourcesOrigNames.add(it)
                                sourcesNames.add("HLS")
                            }
                            else    -> Unit
                        }
                    }


//                for (index in 0 until sourcesObject.names().length()) {
//                    if (sourcesObject.names().getString(index).contains("mp4"))
//                        sourcesNames.add(sourcesObject.names().getString(index).substring(4)+"P")
//                    else if (sourcesObject.names().getString(index).contains("hls"))
//                        sourcesNames.add("HLS")
//                    else
//                        sourcesNames.add(sourcesObject.names().getString(index))
//                }
//
//                for (name in sourcesOrigNames) {
//                    sourcesLinks.add(sourcesObject.getString(name))
//                }

            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val namesArray = arrayOfNulls<String>(sourcesNames.size)
        builder.setTitle("Выберите качество")
                .setItems(sourcesNames.toTypedArray<String>(), this)

        return builder.create()
    }

    override fun onClick(dialogInterface: DialogInterface, i: Int) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(Uri.parse(sourcesLinks[i]), "video/mp4")
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        } else
            Toast.makeText(context, "Не найдено приложений, способных открыть файл", Toast.LENGTH_LONG).show()
    }

    companion object {
        private val ARG_PARAM1 = "sources"

        fun newInstance(sources: JSONObject): SelectQualityFragment {
            val fragment = SelectQualityFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, sources.toString())
            fragment.arguments = args
            return fragment
        }
    }
}
