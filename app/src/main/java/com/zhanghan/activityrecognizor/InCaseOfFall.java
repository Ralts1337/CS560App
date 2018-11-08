package com.zhanghan.activityrecognizor;

import android.Manifest;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class InCaseOfFall extends AppCompatActivity {

    private int PHONE_CALL_CODE =1;              //check if the phone call permission is granted


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_case_of_fall); //display the GUI frame
        Intent intent = getIntent();                      //catches intent from MainActivity;
        //create intent to dial number, to call the emergency
        final Intent dialEmergencyNumber = new Intent(Intent.ACTION_CALL);
        //message now equals to phone number, which is passed from previous activity
        final String emergencyNumber = intent.getStringExtra(MainActivity.APP_Name);
        dialEmergencyNumber.setData(Uri.parse("tel:" + emergencyNumber));
        //TextView to show the seconds left before an automatic dial to emergency
        final TextView countdowntext = findViewById(R.id.countdowntext);
        //Buttons asks if user did fall: Yes / No
        final Button confirm = findViewById(R.id.confirm);
        final Button deny    = findViewById(R.id.deny);

        //countdown timer, if no has not be clicked in 5 seconds
        //now 5 secs for testing. will be 1 minute in the end.
        final CountDownTimer timeBeforeDial = new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {
                //set and displays the test in GUI, which says the remaining time before
                //an automatic dial
                countdowntext.setText("" + millisUntilFinished / 1000);
            }
            //when the countdown ends, dial the emergency number:
            public void onFinish() {
                if (ContextCompat.checkSelfPermission(InCaseOfFall.this,
                        Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED){
                    requestCallPermission();//request permission
                }
                else{//else permission is granted, dial the number
                    startActivity(dialEmergencyNumber);}
            }
        }.start();

        //if user clicked confirmed a fall, dial emergency number immediately
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check for make phone call permission. If permission not granted: request permission
                if (ContextCompat.checkSelfPermission(InCaseOfFall.this,
                        Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED){
                    requestCallPermission();//request permission
                }
                else{//else permission is granted, dial the number
                    startActivity(dialEmergencyNumber);}
                    //cancel the timer
                    timeBeforeDial.cancel();
                    //quit activity
                     finish();
            }
        });
        //if user clicked no, return to the previous activity (main activity, front page)
        deny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeBeforeDial.cancel();
                finish();//activity started by startActivity so it can be finshed by finish()
            }
        });

//############################################

    }
    private void requestCallPermission(){
        //should I show why we request this permission?
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CALL_PHONE)){
            new AlertDialog.Builder(this)
                    .setTitle("Call Permission Needed")
                    .setMessage("This permission is needed to call the emrgency")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            ActivityCompat.requestPermissions(InCaseOfFall.this,new String[]
                                    {Manifest.permission.CALL_PHONE},PHONE_CALL_CODE);
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
        else{
            ActivityCompat.requestPermissions(InCaseOfFall.this,new String[]{Manifest.permission.CALL_PHONE},PHONE_CALL_CODE);
        }
    }
    /*
    Check if the permission is granted
     */
    @Override//check if the permission is granted
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PHONE_CALL_CODE){
            if(grantResults.length>0 && grantResults[0]==getPackageManager().PERMISSION_GRANTED){
                Toast.makeText(this,"Permission Granted", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this,"Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
