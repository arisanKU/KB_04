package com.example.ohta.mirrorhack01;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView message;
    private int count = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ActionBar actionBar = getActionBar();
        // actionBar.hide();

        message = (TextView)findViewById(R.id.message);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_UP){
            count++;
            // Toast.makeText(this,"START SPEECH RECOGNITION",Toast.LENGTH_SHORT).show();
            message.setText("START SPEECH RECOGNITION"+count);
            message.setTextSize(36.0f);
        }

        return super.onTouchEvent(event);
    }
}
