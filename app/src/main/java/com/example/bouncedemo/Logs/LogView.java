package com.example.bouncedemo.Logs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

@SuppressLint("AppCompatCustomView")
public class LogView extends TextView {


    /*1. this formats the log data and prints out to the LogView
    * 2. the msg param = the actual messaged to be logged*/
    public void echo(final String msg){
        /* ensure that the update occurs within the UI thread */
        ((Activity) getContext()).runOnUiThread(new Thread(new Runnable() {
            @Override
            public void run() {
                // show text in the logView
                castToLog(msg);
            }
        }));
    }

    public LogView(Context context) {
        super(context);
    }

    public LogView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void castToLog(String s){ append("\n" + s); }

    public LogView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
    }
}
