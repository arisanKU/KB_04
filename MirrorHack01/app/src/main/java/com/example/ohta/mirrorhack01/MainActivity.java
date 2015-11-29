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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.format.Time;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Random;

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

    Random r = new Random();

    Handler handler = new Handler();
    TextView textView;
    StringBuilder src = new StringBuilder();

    SampleHandler sampleHandler = new SampleHandler();

    ImageView droid ;
    int ImgX=700;
    int ImgY=700;
    int speed=10;
    int radian=0;
    int mvCnt=0;
    boolean mv=true;


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
        droid = (ImageView)findViewById(R.id.droid01);
        droid.setImageResource(R.drawable.droid01);
        /*ViewGroup.LayoutParams params = droid.getLayoutParams();
        params.width =droid.getWidth()/3;
        params.height =droid.getHeight()/3;
        droid.setLayoutParams(params);*/
        //droid.setMaxHeight(droid.getHeight()/2);
        //droid.setMaxWidth(droid.getWidth()/2);
        droid.setScaleX(0.05f);
        droid.setScaleY(0.05f);
       // droid.setTranslationX(1);
        //droid.setTranslationY(1);
        droid.setX(ImgX);
        droid.setY(ImgY);//-700~700
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if(r.nextBoolean()) {
                droid.setImageResource(R.drawable.droid_speak);
            }else{
                droid.setImageResource(R.drawable.droid_hear);
            }
            droid.setScaleX(0.1f);
            droid.setScaleY(0.1f);
            droid.invalidate();
            mv=false;
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
        };
        return super.onTouchEvent(event);

    }

    public class SampleHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
           /* droid.setX(r.nextInt(1400)-700);
            droid.setY(r.nextInt(1400)-700);*/
            if(mv) {
                ImgX += speed * Math.cos(radian);
                ImgY += speed * Math.sin(radian);
                if (ImgX > 700 || ImgX < -700 || ImgY > 700 || ImgY < -700) {
                    radian = radian * (-1) + r.nextInt(90);
                    mvCnt = 0;
                }
                if (mvCnt > 12) {
                    radian = radian + r.nextInt(90);
                    mvCnt = 0;
                }
                droid.setX((float) ImgX);
                droid.setY((float) ImgY);
                mvCnt += 1;
                droid.invalidate();  //2.
                if (sampleHandler != null) sampleHandler.sleep(400);  //3//
            }// .
        }

        //スリープメソッド
        public void sleep(long delayMills) {
            //使用済みメッセージの削除
            removeMessages(0);
            sendMessageDelayed(obtainMessage(0),delayMills);  //4.
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        //定期処理ハンドラの生成と実行
        sampleHandler=new SampleHandler();
        sampleHandler.sleep(0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            droid.setImageResource(R.drawable.droid_speak);
        droid.invalidate();
        // インテントの発行元を限定
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {

            // 音声入力の結果の最上位のみを取得
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String s = results.get(0);

            message.setTextSize(64.0f);
            // 表示
            //Toast.makeText(this, s, Toast.LENGTH_LONG).show();
            if (s.indexOf("天気")!=-1) {
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

            }else if(s.indexOf("時間")!=-1){
                time.setToNow();
                String date = time.year + "年" + (time.month+1) + "月" + time.monthDay + "日　"+
                time.hour + "時" + time.minute + "分" + time.second + "秒";
                message.setText("現在の時間は\n"+date + "\nです");
            }else if(s.indexOf("占い")!=-1){
                message.setText(s);
            }else if(s.indexOf("消去")!=-1){
                message.setText("");
            }else if(s.indexOf("伝言登録")!=-1){
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
            }else if(s.indexOf("伝言参照")!=-1) {
                message.setText(memo);
            }else if(s.indexOf("きれい")!=-1){
                    s="You are the most beautiful !!";
            }else {
                if(firstRec) {
                    message.setText("コマンドが登録されていません：" + s);
                }else{
                    memo=memo+s+"\n\n";
                    firstRec=true;
                }
            }
            message.setTextSize(36.0f);
            mv=true;
            droid.setScaleX(0.05f);
            droid.setScaleY(0.05f);
            droid.setImageResource(R.drawable.droid02);
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
