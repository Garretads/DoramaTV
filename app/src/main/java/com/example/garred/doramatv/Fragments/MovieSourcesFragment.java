package com.example.garred.doramatv.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.garred.doramatv.ProgressBottomSheet;
import com.example.garred.doramatv.Tools.PageDownloader;
import com.example.garred.doramatv.R;
import com.example.garred.doramatv.Settings;
import com.example.garred.doramatv.Tools.VKRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MovieSourcesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MovieSourcesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MovieSourcesFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "info";

    // TODO: Rename and change types of parameters
    private String mInfo;
    static String TRAGUS_URL = "http://grass.tragus.ru/internal/videoCode/";
    private ArrayAdapter arrayAdapter;
    JSONArray seriesList;
    ArrayList listViewList;
    ListView listView;
    JSONArray sourcesArray;
    JSONObject sourcesInfo;
    String URL;
    String accessToken;
    Boolean isSerial;
    private Boolean seriesSelected = false;
    ProgressBottomSheet progressBottomSheet;
    String initialSeries;

    private OnFragmentInteractionListener mListener;

    public MovieSourcesFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static MovieSourcesFragment newInstance(JSONObject sourcesInfo) {
        MovieSourcesFragment fragment = new MovieSourcesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, sourcesInfo.toString());
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            try {
                progressBottomSheet = new ProgressBottomSheet();
                sourcesInfo = new JSONObject(getArguments().getString(ARG_PARAM1));
                URL = sourcesInfo.getString("URL");
                accessToken = sourcesInfo.getString("access_token");
                isSerial = sourcesInfo.getBoolean("isSerial");
                initialSeries = sourcesInfo.getString("initial_series");
                seriesList = formSeriesList(URL,initialSeries);
                listViewList = new ArrayList();

                for (int i=0;i<seriesList.length();i++) {
                    listViewList.add(((JSONObject)seriesList.get(i)).getString("name"));
                }

                arrayAdapter = new ArrayAdapter(getContext(),android.R.layout.simple_list_item_1, listViewList);
                arrayAdapter.setNotifyOnChange(true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_movie_sources, container, false);
        listView = view.findViewById(R.id.sourcesListView);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    progressBottomSheet.show(getFragmentManager(),"progressBar");
                    if (!seriesSelected) {
                            sourcesArray = getSources(URL, i);

                        ArrayList funSubList = new ArrayList();
                        funSubList.add(((JSONObject)seriesList.get(i)).getString("name"));
                        for (int index=0; index<sourcesArray.length();index++) {

                            JSONObject jsonObject = (JSONObject)sourcesArray.get(index);
                            funSubList.add(jsonObject.getString("sub_unit"));
                        }
                        arrayAdapter.clear();
                        arrayAdapter.addAll(funSubList);
                        arrayAdapter.notifyDataSetChanged();
                        seriesSelected = true;
                    }
                    else {
                        switch (i) {
                            case 0: {
                                arrayAdapter.clear();

                                for (int index=0;index<seriesList.length();index++) {
                                    arrayAdapter.add(((JSONObject)seriesList.get(index)).getString("name"));
                                }

                                arrayAdapter.notifyDataSetChanged();
                                seriesSelected = false;
                                break;
                            }
                            default: {
                                //Выбор качества, воспроизведение
                                /*
                                * Формируем запрос в vk api
                                * https://api.vk.com/method/video.get?videos=-66384560_456239143&access_token=d053e5de82599c59b61a8a138cfe732d462a245623f8807ee3a4bf5a9dad3e22f1179377b0499001932f0&v=5.92
                                *
                                * Парсим ответ в JSONObject. Выцепляем оттуда
                                *
                                *
                                *
                                * */
                                JSONObject jsonObject = (JSONObject) sourcesArray.get(i-1);
                                String METHOD_NAME = "video.get";
                                Uri.Builder builder = new Uri.Builder();
                                builder.scheme("https")
                                        .authority("api.vk.com")
                                        .appendPath("method")
                                        .appendPath(METHOD_NAME)
                                        .appendQueryParameter("videos",jsonObject.getString("movie_id"))
                                        .appendQueryParameter("access_token",accessToken)
                                        .appendQueryParameter("v", Settings.version());
                                builder.build();

                                VKRequest vkRequest = new VKRequest();
                                JSONObject object = new JSONObject(vkRequest.execute(builder.toString()).get());
                                object = (JSONObject) object.get("response");
                                object = (JSONObject) ((JSONArray) object.get("items")).get(0);
                                JSONObject fileLink = (JSONObject) object.get("files");

                                SelectQualityFragment selectQualityFragment = SelectQualityFragment.newInstance(fileLink);

                                selectQualityFragment.show(getFragmentManager(), "Выберите качество");

                            }
                        }
                    }

                    progressBottomSheet.dismiss();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        return view;
    }


    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    static JSONArray formSeriesList(String URL, String initialSeries) {
        JSONArray seriesList = new JSONArray();

        PageDownloader pageDownloader = new PageDownloader();
        Document pageContent;
        try {
            pageContent = pageDownloader.execute(URL+initialSeries).get();
            Element element = pageContent.getElementById("chapterSelectorSelect");
            Elements elements = element.getElementsByTag("option");
            int index = 0;
            for (Element element1 : elements) {
                JSONObject object = new JSONObject();
                object.put("name",element1.text());
                String link = element1.attr("value");
                link = link.substring(link.lastIndexOf("/"));
                object.put("link",link);
                seriesList.put(index,object);
                index++;
            }


        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return seriesList;
    }

    JSONArray getSources(String URL, int seriesIndex) throws InterruptedException,ExecutionException {
        Pattern vkPattern = Pattern.compile("oid=(.?[\\d]+).+id=([\\d]+).+hash=(.+)\" a");
        Matcher matcher;

        PageDownloader pageDownloader;
        Document pageContent;

        JSONArray oneSeriesSources = new JSONArray();
        pageDownloader = new PageDownloader();

        try {

            pageContent = pageDownloader.execute(URL + ((JSONObject) seriesList.get(seriesIndex)).getString("link")).get();
            Elements elements = pageContent.getElementsByClass("chapter-link");

            for (Element element1 : elements) {
                JSONObject jsonObject = new JSONObject();
                String subUnit;
                String seriesID;
                String oid;
                String id;
                String hash;

                if (element1.getElementsByClass("person-link").first() != null)
                    subUnit = "Фансаб "+element1.getElementsByClass("person-link").first().text();
                else
                    subUnit = "Оригинал";

                seriesID = element1.getElementsByAttribute("data-sid").first().attr("data-sid");

                pageDownloader = new PageDownloader();
                pageContent = pageDownloader.execute(TRAGUS_URL + seriesID).get();

                String tempURL = pageContent.getElementsByTag("iframe").first().toString();

                if (tempURL.contains("vk.com")) {
                    matcher = vkPattern.matcher(tempURL);
                    if (matcher.find()) {
                        oid = matcher.group(1);
                        id = matcher.group(2);
                        hash = matcher.group(3);

                        jsonObject.put("sub_unit", subUnit);
                        jsonObject.put("movie_id", oid + "_" + id);
                        jsonObject.put("hash", hash);
                        oneSeriesSources.put(jsonObject);
                    }
                } else
                    continue;

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return oneSeriesSources;
    }
}
