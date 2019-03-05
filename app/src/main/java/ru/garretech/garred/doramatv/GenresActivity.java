package ru.garretech.garred.doramatv;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GenresActivity extends AppCompatActivity{
    ArrayList<String> genresNameList;
    ArrayAdapter arrayAdapter;
    ListView listView;
    JSONArray genresArray = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genres);
        setTitle("Жанры");
        listView = findViewById(R.id.genresListView);
        Toolbar toolbar = findViewById(R.id.toolbar_actionbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final Intent intent = getIntent();
        genresNameList = new ArrayList<>();
        try {
            genresArray = new JSONArray(intent.getStringExtra("genres"));

            for (int i = 0; i < genresArray.length(); i++) {
                String name = ((JSONObject) genresArray.get(i)).getString("name");
                genresNameList.add(name.substring(0,1).toUpperCase()+name.substring(1));
            }
            arrayAdapter = new ArrayAdapter(getApplicationContext(),android.R.layout.simple_list_item_1,genresNameList);
            listView.setAdapter(arrayAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    try {
                        Intent data = new Intent();
                        data.putExtra("genrePrefix",((JSONObject) genresArray.get(i)).getString("link"));
                        data.putExtra("genreName",((JSONObject) genresArray.get(i)).getString("name"));
                        setResult(RESULT_OK,data);
                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
