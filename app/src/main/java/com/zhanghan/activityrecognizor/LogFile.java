package com.zhanghan.activityrecognizor;

import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;

import java.util.ArrayList;

public class LogFile extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_file);

        TextView log = findViewById(R.id.Log);

        ArrayList<String> convertedDatas = (ArrayList<String>)getIntent().getSerializableExtra(MainActivity.APP_Name);
        String everything = new String();
        for (String convertedData:convertedDatas){
            everything += convertedData+"\n";
        }
        log.setText(everything);
    }

}
