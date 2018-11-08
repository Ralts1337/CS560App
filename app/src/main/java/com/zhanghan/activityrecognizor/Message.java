package com.zhanghan.activityrecognizor;
import android.content.Context;
import android.widget.Toast;
//Show the toast message so I don't need to write the long TOAST code every single fucking time
public class Message {
    public static void message(Context context, String message){
        Toast.makeText(context,message,Toast.LENGTH_LONG).show();
    }
}
