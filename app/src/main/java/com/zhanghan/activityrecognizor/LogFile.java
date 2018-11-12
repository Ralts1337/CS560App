package com.zhanghan.activityrecognizor;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class LogFile extends Activity {

    private static final String USGS_REQUEST_URL =
            "localhost:8000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //bind buttons and textviews to variables
        setContentView(R.layout.activity_log_file);

        final TextView log = findViewById(R.id.Log);
        Button send = findViewById(R.id.Send);



        //--------bind button, add onClickListener for "Send"-------
        //--------Send button is intended to: when clicked, send data to local server
        //按Send这个按键，把上面显示log里面的data送到localhost
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadToServerTask task = new UploadToServerTask();
                task.execute();
            }
        });
        //--------display database data------------------
        ArrayList<String> convertedDatas = (ArrayList<String>) getIntent().getSerializableExtra(MainActivity.APP_Name);
        String everything = new String();
        for (String convertedData : convertedDatas) {
            everything += convertedData + "\n";
        }
        log.setText(everything);
        }
        //----------BUG! need work from here and below: --------------------------
        //我们想上传database数据到Local server, 然而不会写HTTP request和API
    private class UploadToServerTask extends AsyncTask<URL, Void, Void> {
        String json = formatDataAsJSON();
        @Override
        protected Void doInBackground(URL... urls) {
            // Create URL object
            URL url = new URL(USGS_REQUEST_URL);

            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);

            } catch (IOException e) {
                // TODO Handle the IOException
            }
            return null;
        }
        /**
         * Make an HTTP request to the given URL and return a String as the response.
         */
        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.connect();


            } catch (IOException e) {
                // TODO: Handle the exception
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    // function must handle java.io.IOException here
                    inputStream.close();
                }
            }
            return jsonResponse;
        }



    }



    //-----------make a JSON message -----------------
    private String formatDataAsJSON () {
        final JSONObject root = new JSONObject();
        try {
            root.put("message", "message");
            return root.toString(1);
        } catch (JSONException e1) {
            Log.d("JWP", "Can't format");
        }
        return null;
    }



}