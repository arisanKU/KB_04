package com.example.ohta.mirrorhack01;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.text.format.Time;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener, LocationListener {

    // ダミーの識別子
    private static final int REQUEST_CODE = 0;

    // 音声合成用
    TextToSpeech tts = null;

    private TextView message;
    private int count = 0;

    private Time time = new Time("Asia/Tokyo");

    private LocationManager locationManager;
    private LocationListener listener;

    private Double latitude;
    private Double longtitue;

    private String memo = "";
    boolean firstRec = true;

    Handler handler = new Handler();
    TextView textView;
    StringBuilder src = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ActionBar actionBar = getActionBar();
        // actionBar.hide();

        message = (TextView) findViewById(R.id.message);
        tts = new TextToSpeech(this, this);
        message.findViewById(R.id.message);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // インテントの発行元を限定
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {

            // 音声入力の結果の最上位のみを取得
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String s = results.get(0);

            message.setTextSize(64.0f);
            // 表示
            //Toast.makeText(this, s, Toast.LENGTH_LONG).show();
            if (s.equals("天気")) {
                //座標取得
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for Activity#requestPermissions for more details.
                        return;
                    }
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
                message.setText("NET");
                latitude = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLatitude();
                longtitue = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLongitude();

                message.setText(latitude.toString()+  longtitue);
                //OpenWeathermapAPI
                new Thread(new Runnable(){
                    public void run() {
                        URL url = null;
//        Toast.makeText(this,"INTO URL", Toast.LENGTH_LONG).show();
                        try {
                            url = new URL("http://api.openweathermap.org/data/2.5/forecast?lat="+latitude.toString()+"&lon="+longtitue.toString()+"&appid=2de143494c0b295cca9337e1e96b00e0&cnt=1&mode=json");
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                        HttpURLConnection connection = null;
                        try {
                            connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("GET");
                            connection.connect();
                            InputStream is = connection.getInputStream();

                            while (true) {
                                byte[] line = new byte[1024];
                                int size = is.read(line);
                                if (size <= 0)
                                    break;
                                src.append(new String(line, "euc-jp"));
                            }
                            handler.post(new Runnable() {
                                public void run() {
                                    //message.setText(src);
                                    //src.delete(0,src.length());

                //Parse JSON
                String result="ERROR";
                message.setText(src.toString());
                try {
                   // Log.e("SRC",src.toString());
                    JSONObject rootObj = new JSONObject(src.toString());
                    JSONArray listArray = rootObj.getJSONArray("list");

                    JSONObject obj = listArray.getJSONObject(0);

                    // 地点ID
                    //int id = obj.getInt("id");

                    // 地点名
                   // String cityName = obj.getString("name");

                    // 気温(Kから℃に変換)
                    JSONObject mainObj = obj.getJSONObject("main");
                    float currentTemp = (float) (mainObj.getDouble("temp") - 273.15f);

                    float minTemp = (float) (mainObj.getDouble("temp_min") - 273.15f);

                    float maxTemp = (float) (mainObj.getDouble("temp_max") - 273.15f);
                    int humidity=0;
                    // 湿度
                    if (mainObj.has("humidity")) {
                        humidity = mainObj.getInt("humidity");
                    }

                    // 取得時間
                   // long time = obj.getLong("dt");

                    // 天気
                    JSONArray weatherArray = obj.getJSONArray("weather");
                    JSONObject weatherObj = weatherArray.getJSONObject(0);
                    String weather = weatherObj.getString("main");
                    String iconId = weatherObj.getString("icon");

                    result ="天気　　:"+weather+"\n最高気温:"+maxTemp+"\n最低気温:"+minTemp+"\n湿度　　:"+humidity;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                message.setText(result);
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally{
                            connection.disconnect();
                        }
                    };
                }).start();

            }else if(s.equals("時間")){
                time.setToNow();
                String date = time.year + "年" + (time.month+1) + "月" + time.monthDay + "日　"+
                time.hour + "時" + time.minute + "分" + time.second + "秒";
                message.setText("現在の時間は\n"+date + "\nです");
            }else if(s.equals("占い")){
                message.setText(s);
            }else if(s.equals("コネクト")){
                message.setText(s);
            }else if(s.equals("伝言登録")){
                firstRec=false;
                try {
                    // "android.speech.action.RECOGNIZE_SPEECH" を引数にインテント作成
                    Intent intent = new Intent(
                            RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

                    // 「お話しください」の画面で表示される文字列
                    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "伝言をどうぞ");

                    // 音声入力開始
                    startActivityForResult(intent, REQUEST_CODE);
                } catch (ActivityNotFoundException e) {
                    // 非対応の場合
                    Toast.makeText(this, "音声入力に非対応です。", Toast.LENGTH_LONG).show();
                }
               // memo=memo+s+"\n\n";
            }else if(s.equals("伝言参照")){
                message.setText(memo);
            }else {
                if(firstRec) {
                    message.setText("コマンドが登録されていません：" + s);
                }else{
                    memo=memo+s+"\n\n";
                    firstRec=true;
                }
            }
            message.setTextSize(36.0f);

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

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        if (locationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for Activity#requestPermissions for more details.
                    return;
                }
            }
            locationManager.removeUpdates(this);
        }
        super.onPause();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
