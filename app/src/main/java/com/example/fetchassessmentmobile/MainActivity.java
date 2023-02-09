package com.example.fetchassessmentmobile;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private String SOURCEURL = "https://fetch-hiring.s3.amazonaws.com/hiring.json";
    private String JSONString;
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
        new JSONTask().execute(url);
        try {
            return new JSONArray(JSONString);
        } catch (JSONException e) {
            throw new RuntimeException(e); //TODO throw exception or return empty and let unit test deal w/?
        }
    }



    private class JSONTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()){
                pd.dismiss();
            }
            JSONString = result;    //TODO does this return the JSON string or a success message?
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