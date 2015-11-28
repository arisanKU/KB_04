package com.example.ohta.mirrorhack01;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener{

    // ダミーの識別子
    private static final int REQUEST_CODE = 0;

    // 音声合成用
    TextToSpeech tts = null;

    private TextView message;
    private int count = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ActionBar actionBar = getActionBar();
        // actionBar.hide();

        message = (TextView)findViewById(R.id.message);
        tts = new TextToSpeech(this, this);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_UP) {
            try {
                // "android.speech.action.RECOGNIZE_SPEECH" を引数にインテント作成
                Intent intent = new Intent(
                        RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

                // 「お話しください」の画面で表示される文字列
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "音声認識中です");

                // 音声入力開始
                startActivityForResult(intent, REQUEST_CODE);
            } catch (ActivityNotFoundException e) {
                // 非対応の場合
                Toast.makeText(this, "音声入力に非対応です。", Toast.LENGTH_LONG).show();
            }
        }
            return super.onTouchEvent(event);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // インテントの発行元を限定
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {

            // 音声入力の結果の最上位のみを取得
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String s = results.get(0);

            // 表示
            Toast.makeText(this, s, Toast.LENGTH_LONG).show();

            // 音声合成して発音
            if(tts.isSpeaking()) {
                tts.stop();
            }
            tts.speak(s, TextToSpeech.QUEUE_FLUSH, null);

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS) {
            // 音声合成の設定を行う

            float pitch = 1.0f; // 音の高低
            float rate = 1.0f; // 話すスピード
            Locale locale = Locale.US; // 対象言語のロケール
            // ※ロケールの一覧表
            //   http://docs.oracle.com/javase/jp/1.5.0/api/java/util/Locale.html

            tts.setPitch(pitch);
            tts.setSpeechRate(rate);
            tts.setLanguage(locale);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if( tts != null )
        {
            // 破棄
            tts.shutdown();
        }
    }
}
