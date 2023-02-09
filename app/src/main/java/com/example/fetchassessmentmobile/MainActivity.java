package com.example.fetchassessmentmobile;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.fetchassessmentmobile.databinding.ActivityMainBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private String SOURCEURL = "https://fetch-hiring.s3.amazonaws.com/hiring.json";
    private String JSONString = null;
    private ActivityMainBinding binding;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        //setContentView(R.layout.activity_main);

        JSONArray jsonArray = GETJSON(SOURCEURL);
        HashMap<Integer, ArrayList<String>> listIdGroups = parseJSON(jsonArray);


        Spinner dropdown = binding.listIdSpinner;
        StringBuilder sb = new StringBuilder("All");
        for (Integer key : listIdGroups.keySet()) {
            listIdGroups.get(key).sort(new listIdComparator());
            sb.append(" " + Integer.toString(key));
        }
        String[] listIds = sb.toString().split(" ");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listIds);
        dropdown.setAdapter(adapter);

        ArrayList<String> outputStrings = new ArrayList<>();
        outputStrings.add("");  //This will become all once individuals are populated

        for (int i = 1; i < listIds.length; i++) {
            sb = new StringBuilder();
            for (String name : listIdGroups.get(Integer.parseInt(listIds[i]))) {
                sb.append(name).append("\t");
            }

            outputStrings.add(sb.toString());
            outputStrings.set(0, new StringBuilder(outputStrings.get(0)).append("\n\nlistId: ").append(listIds[i])
                    .append("\n").append(sb.toString()).toString());
        }

        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                binding.output.setText(outputStrings.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });



    }

    private class listIdComparator implements Comparator<String> {
        @Override
        public int compare(String s1, String s2) {

            return Integer.parseInt(s1.substring(5)) - Integer.parseInt(s2.substring(5));
        }
    }



    private HashMap<Integer, ArrayList<String>> parseJSON(JSONArray JSONArrayIn) {
        HashMap<Integer, ArrayList<String>> output = new HashMap<>();

        try {
            for (int i = 0; i < JSONArrayIn.length(); i++) {
                JSONObject curr = JSONArrayIn.getJSONObject(i);
                if (curr.isNull("name") || curr.get("name").equals("")) { continue; }

                int listId = (Integer) curr.get("listId");
                String name = (String) curr.get("name");

                if (output.containsKey(listId)) {
                    output.get(listId).add(name);
                } else {
                    output.put(listId, new ArrayList<String>());
                    output.get(listId).add(name);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RuntimeException(e); //TODO throw exception or return empty and let unit test deal w/?
        }

        return output;
    }

    private JSONArray GETJSON(String url) {
        new GETJSONStringTask().execute(url);
        while (JSONString == null) { }

        try {
            return new JSONArray(JSONString);
        } catch (JSONException e) {
            throw new RuntimeException(e); //TODO throw exception or return empty and let unit test deal w/?
        }
    }



    class GETJSONStringTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            HttpURLConnection conn = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                InputStream inStream = conn.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inStream == null) { return null; }

                reader = new BufferedReader(new InputStreamReader(inStream));
                String line;

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }

                JSONString = buffer.toString();
                return null;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                if (conn != null) { conn.disconnect(); }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }


    }




}