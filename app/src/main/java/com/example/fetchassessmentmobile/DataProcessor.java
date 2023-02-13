package com.example.fetchassessmentmobile;

import android.os.AsyncTask;

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

public class DataProcessor {
    private final String SOURCEURL;
    private String JSONString = null;
    private String[] listIds = null;
    private ArrayList<String> formattedListIdStrings = null;

    public DataProcessor() {
        SOURCEURL = "https://fetch-hiring.s3.amazonaws.com/hiring.json";
        refreshData();
    }

    public DataProcessor(String url) {
        SOURCEURL = url;
        refreshData();
    }

    public String[] getListIds() {
        if (listIds == null) { refreshData(); }

        return listIds;
    }

    public ArrayList<String> getFormattedListIdStrings() {
        if (formattedListIdStrings == null) { refreshData(); }

        return formattedListIdStrings;
    }

    private void refreshData() {
        JSONArray jsonArray = GETJSON(SOURCEURL);
        HashMap<Integer, ArrayList<String>> listIdGroups = parseJSON(jsonArray);

        StringBuilder sb = new StringBuilder("All");
        for (Integer key : listIdGroups.keySet()) {
            sb.append(" " + Integer.toString(key));
            listIdGroups.get(key).sort(new listIdComparator());
        }
        listIds = sb.toString().split(" ");

        formattedListIdStrings = new ArrayList<>();
        formattedListIdStrings.add("");  //This will become all once individuals are populated

        for (int i = 1; i < listIds.length; i++) {
            sb = new StringBuilder();
            for (String name : listIdGroups.get(Integer.parseInt(listIds[i]))) {
                sb.append(name).append("\t");
            }

            formattedListIdStrings.add(sb.toString());
            formattedListIdStrings.set(0, new StringBuilder(formattedListIdStrings.get(0)).append("\n\nlistId: ").append(listIds[i])
                    .append("\n").append(sb.toString()).toString());
        }
    }

    private JSONArray GETJSON(String url) {
        new GETJSONStringTask().execute(url);
        //This should probably be some form of await, but I'm not familiar with best practices in this area.
        //I'll put a timeout in the unit test and it should suffice
        while (JSONString == null) { }

        try {
            return new JSONArray(JSONString);
        } catch (JSONException e) {
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
        }

        return output;
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

    private class listIdComparator implements Comparator<String> {
        @Override
        public int compare(String s1, String s2) {

            return Integer.parseInt(s1.substring(5)) - Integer.parseInt(s2.substring(5));
        }
    }
}
