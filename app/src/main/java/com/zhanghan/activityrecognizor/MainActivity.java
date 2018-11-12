
package com.zhanghan.activityrecognizor;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;           //used to request permission
import android.support.v4.content.ContextCompat;        //used to request permission
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;        //used in MainActivity
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
//SQL
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
//import sensors
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener {
    //App_name just in case this app calls other apps
    public static final String APP_Name = "com.zhanghan.activityrecognizor";
    private AppDatabase db = new AppDatabase(MainActivity.this);
    public boolean fallDown = false;   //did you fall down on the ground?
    private SensorManager sManager;                         //create sensor manager  sManager
    private Sensor mSensorAccelerometer;                //create accelerometer sensor
    private TextView tv_step, tv_accum, tv_rec;        //create
    //btn_create: create database
    //btn_insert: save
    //btn_start : start
    //btn_dial  : dials emergency number on next screen
    private Button btn_create, btn_insert, btn_start, btn_dial, btn_fall, btn_getData;
    private EditText emergency_number;
    private int step = 0;                               //count steps
    private int counter = 0;
    private double frequency;
    private long startTime = 0;
    private long endTime = 0;
    private double lstValue = 0;                        //previous value
    private double curValue = 0;                        //current value
    private boolean motiveState = true;                 //if the sensor is in movement state
    private boolean processState = false;               //if count steps in the moment
    private int PHONE_CALL_CODE = 1;                     //check if the phone call permission is granted
    //txt file to record the phone number will be saved here:(Function to be added)
    public String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "LocalEmergencyNumber";
    private static final String PHONE_NUMBER = "LocalEmergencyNumber";  //file name of txt
    public String emergencyNumber = "997";
    /*
    Creates everthing needed to run this APP
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //EditText for entering phone number and countdown
        final Intent dialEmergencyNumber = new Intent(Intent.ACTION_CALL);
        final EditText numberInBlock = (EditText) findViewById(R.id.numberBlock);
        // call sensor
        sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorAccelerometer = sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sManager.registerListener(this, mSensorAccelerometer, SensorManager.SENSOR_DELAY_UI);
        // create database

        //buttons
        btn_create = (Button) findViewById(R.id.btn_create);
        btn_dial = (Button) findViewById(R.id.btn_dial);  //Button for dial number
        btn_fall = findViewById(R.id.btn_fall);
        btn_getData = findViewById(R.id.SQL);// Press this button to show SQL data

        //press create, shows "Database created successfully!". However it does nothing. Just placebo effect.
        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Database created successfully!", Toast.LENGTH_SHORT).show();
            }
        });

        //Dial Emergency when pressed button "Dial"
        btn_dial.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                emergencyNumber = numberInBlock.getText().toString();
                //Make phone call
                if (emergencyNumber.isEmpty() || emergency_number == null) {
                    emergencyNumber = "997";
                }
                dialEmergencyNumber.setData(Uri.parse("tel:" + emergencyNumber));
                //check for make phone call permission. If permission not granted: request permission
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestCallPermission();//request permission
                } else {//else permission is granted, dial the number
                    startActivity(dialEmergencyNumber);
                }
            }
        });
        //press getData to get data
        // 点击"Data"按键（ID是getdata), 显示database的数据到Logfile.java
        // 请看logfile.java （Bug in logfile.java)
        btn_getData.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                List<RowOfData> datas = db.getAllData();
                List<String> convertedDatas = new ArrayList<String>();
                Intent getData = new Intent(MainActivity.this, LogFile.class);
                for (RowOfData data : datas) {
                    String dataToDisplay = "Time: " + data.getTime() + " Frequency: " + data.getFrequency();
                    convertedDatas.add(dataToDisplay);
                }
                //we cannot pass a whole list of data..fuck it
                getData.putExtra(APP_Name, (Serializable) convertedDatas);
                startActivity(getData);
            }
        });

        btn_fall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fellDown();
            }
        });

        bindViews();
    }//onCreate finishes


    //Starts the "InCaseOfFall" activity if fall is detected
    //timer and auto dial emergency in InCaseOfFall.java
    public void fellDown() {
        Intent felldown = new Intent(this, InCaseOfFall.class);
        EditText phoneNumber = (EditText) findViewById(R.id.numberBlock);//phone number = entered emergency number in the block
        String message = phoneNumber.getText().toString();
        if (message.isEmpty() || message == null) {
            message = "997";
        }
        felldown.putExtra(APP_Name, message);
        startActivity(felldown);
    }

    //Do we really need this...?
    private void bindViews() {

        tv_step = (TextView) findViewById(R.id.tv_step);
        tv_accum = (TextView) findViewById(R.id.tv_accum);
        tv_rec = (TextView) findViewById(R.id.tv_rec);
        btn_start = (Button) findViewById(R.id.btn_start);
        btn_insert = (Button) findViewById(R.id.btn_insert);
        btn_start.setOnClickListener(this);
        btn_insert.setOnClickListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        double range = 1;                                         //set accuracy
        float[] value = event.values;
        curValue = magnitude(value[0], value[1], value[2]);  //calculate current module of axis x, y, z

        if (motiveState == true) {
            if (curValue >= lstValue) lstValue = curValue;
            else {

                if (Math.abs(curValue - lstValue) > range) {
                    motiveState = false;
                }
            }
        }

        if (motiveState == false) {
            if (curValue <= lstValue) lstValue = curValue;
            else {
                if (Math.abs(curValue - lstValue) > range) {
                    //detect a peak value
                    if (processState == true) {
                        step++;                                             //update steps
                        counter++;
                        if (processState == true) {
                            tv_step.setText(step + "");                   //shown in display
                            tv_accum.setText(counter + " ");
                        }
                    }
                    motiveState = true;
                }
            }
        }
    }

    //for database, start, count and everything else. Default on click with no method name
    @Override
    public void onClick(View view) {
        tv_step.setText("0");
        Calendar calendar = Calendar.getInstance();
        tv_rec.setText(" You are...");

        if (processState == true) {
            btn_start.setText("START");
            processState = false;
            endTime = calendar.getTimeInMillis();               // get system time in  millisecond
            frequency = (double) step / (endTime - startTime);
            //insert data to database if and only if clicked "Save" button
            btn_insert.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    db.insertData(new RowOfData(Long.toString(endTime), Double.toString(frequency)));
                    /*
                    ContentValues values = new ContentValues();
                    values.put("username","ZhangHan");
                    values.put("time",endTime);
                    values.put("frequency",frequency);
                    db.insert("userInformation",null,values);//insert values to DB
                */
                }
            });

            //If frequency is too high, the phone holder must have fallen down
            if ((frequency >= 0.00221)) {
                tv_rec.setText("FELL");
                fellDown();//method that calls emergency. see the method above
            } else {
                tv_rec.setText("WALKING");
            }
            step = 0;

        } else {
            btn_start.setText("STOP!");
            processState = true;
            startTime = calendar.getTimeInMillis();

        }

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    //input float x,y,z, return x^2+y^2+z^2
    public double magnitude(float x, float y, float z) {
        double magnitude = 0;
        magnitude = Math.sqrt(x * x + y * y + z * z);
        return magnitude;
    }

    //#############################################################################################
    //method to request permission for phone call
    /*
    This method requests the permission to use phone and calls onRequestPermissionsResult
    */
    private void requestCallPermission() {
        //should I show why we request this permission?
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Call Permission Needed")
                    .setMessage("This permission is needed to call the emrgency")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]
                                    {Manifest.permission.CALL_PHONE}, PHONE_CALL_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create()
                    .show();
        }
        //no I don't need to show why. Just ask for it
        else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, PHONE_CALL_CODE);
        }
    }

    /*
    Check if the permission is granted
     */
    @Override//check if the permission is granted
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PHONE_CALL_CODE) {
            if (grantResults.length > 0 && grantResults[0] == getPackageManager().PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    /*
    save the phone number into a txt file
     */


    @Override
    protected void onDestroy() {
        super.onDestroy();
        sManager.unregisterListener(this);
    }


}

