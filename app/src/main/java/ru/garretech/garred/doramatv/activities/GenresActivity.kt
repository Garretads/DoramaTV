package ru.garretech.garred.doramatv.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.util.ArrayList

import butterknife.BindView
import butterknife.ButterKnife
import ru.garretech.garred.doramatv.R

class GenresActivity : AppCompatActivity() {
    private var genresArray: JSONArray? = null
    @BindView(R.id.genresListView)
    internal var listView: ListView? = null
    @BindView(R.id.toolbar_actionbar)
    internal var toolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_genres)
        ButterKnife.bind(this)
        title = getString(R.string.genres_title)

        if (toolbar != null) {
            setSupportActionBar(toolbar)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        val intent = intent
        val genresNameList = ArrayList<String>()
        try {
            genresArray = JSONArray(intent.getStringExtra("genres"))

            for (i in 0 until genresArray!!.length()) {
                val name = (genresArray!!.get(i) as JSONObject).getString("name")
                genresNameList.add(name.substring(0, 1).toUpperCase() + name.substring(1))
            }

            val arrayAdapter = object : ArrayAdapter<String>(applicationContext, android.R.layout.simple_list_item_1,genresNameList) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent)
                    val text : TextView = view.findViewById(android.R.id.text1)
                    text.setTextColor(resources.getColor(android.R.color.white))
                    return view
                }
            };

           /* val arrayAdapter = object : ArrayAdapter(applicationContext, android.R.layout.simple_list_item_1, genresNameList) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent)
                    val text = view.findViewById(android.R.id.text1)
                    text.setTextColor(resources.getColor(android.R.color.white))
                    return view
                }
            }*/

            listView!!.setAdapter(arrayAdapter)
            listView!!.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
                try {
                    val data = Intent()
                    data.putExtra("link", (genresArray!!.get(i) as JSONObject).getString("link"))
                    data.putExtra("name", (genresArray!!.get(i) as JSONObject).getString("name"))
                    setResult(Activity.RESULT_OK, data)
                    finish()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_genres, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}
