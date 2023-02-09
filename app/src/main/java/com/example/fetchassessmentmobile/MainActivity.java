package com.example.fetchassessmentmobile;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

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
    }


    private HashMap<Integer, ArrayList<String>> parseJSON(JSONArray JSONArrayIn) {
        HashMap<Integer, ArrayList<String>> output = new HashMap<>();

        try {
            for (int i = 0; i < JSONArrayIn.length(); i++) {
                JSONObject curr = JSONArrayIn.getJSONObject(i);
                int listId = (Integer) curr.get("listId");      //TODO don't shoot yourself in the foot w/ typecasting
                String name = (String) curr.get("name");

                if (name.equals("") || name == null) { continue; }

                if (output.containsKey(listId)) {
                    output.get(listId).add(name);
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e); //TODO throw exception or return empty and let unit test deal w/?
        }

        return null; //TODO
    }

    private JSONArray GETJSON(String url) {
        new GETJSONStringTask().execute(url);
        while (JSONString == null) {

        }

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





/*
    //TODO put below in function (setOnItemSelectedListener?)
    Spinner dropdown = binding.listIdSpinner;
    String[] listIds = {"All"}; //TODO add hashmap keys
    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listIds);
    dropdown.setAdapter(adapter); //TODO broken or just needs build?
*/
}