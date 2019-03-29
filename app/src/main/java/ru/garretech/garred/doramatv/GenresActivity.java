package ru.garretech.garred.doramatv;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GenresActivity extends AppCompatActivity{
    private JSONArray genresArray = null;
    @BindView(R.id.genresListView) ListView listView;
    @BindView(R.id.toolbar_actionbar) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genres);
        ButterKnife.bind(this);
        setTitle(getString(R.string.genres_title));

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final Intent intent = getIntent();
        ArrayList<String> genresNameList = new ArrayList<>();
        try {
            genresArray = new JSONArray(intent.getStringExtra("genres"));

            for (int i = 0; i < genresArray.length(); i++) {
                String name = ((JSONObject) genresArray.get(i)).getString("name");
                genresNameList.add(name.substring(0,1).toUpperCase()+name.substring(1));
            }
            ArrayAdapter arrayAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, genresNameList) {
                @Override
                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView text = view.findViewById(android.R.id.text1);
                    text.setTextColor(getResources().getColor(android.R.color.white));
                    return view;
                }
            };

            listView.setAdapter(arrayAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    try {
                        Intent data = new Intent();
                        data.putExtra("link",((JSONObject) genresArray.get(i)).getString("link"));
                        data.putExtra("name",((JSONObject) genresArray.get(i)).getString("name"));
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_genres, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
