package com.example.garred.doramatv;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class SelectQualityFragment extends DialogFragment implements DialogInterface.OnClickListener {
    final String LOG_TAG = "myLogs";
    private static final String ARG_PARAM1 = "sources";
    JSONObject sourcesObject;
    ArrayList<String> sourcesNames;
    ArrayList<String> sourcesLinks;

    public static SelectQualityFragment newInstance(JSONObject sources) {
        SelectQualityFragment fragment = new SelectQualityFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, sources.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            try {
                sourcesObject = new JSONObject(getArguments().getString(ARG_PARAM1));
                sourcesLinks = new ArrayList<>();
                sourcesNames = new ArrayList<>();

                for (int index=0; index<sourcesObject.names().length();index++)
                    sourcesNames.add(sourcesObject.names().getString(index));

                for (String name: sourcesNames) {
                    sourcesLinks.add(sourcesObject.getString(name));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        String[] namesArray = new String[sourcesNames.size()];
        builder.setTitle("Выберите качество")
                .setItems(sourcesNames.toArray(namesArray), this);

        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(sourcesLinks.get(i)), "video/mp4");
        startActivity(intent);
    }
}
