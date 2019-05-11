package com.example.user.fatsim.Scenes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.user.fatsim.AutoService;
import com.example.user.fatsim.DBGameManager;
import com.example.user.fatsim.DBManager;
import com.example.user.fatsim.R;

import java.util.ArrayList;

public class GameScene extends Activity implements View.OnClickListener {

    int CALL_REQUEST = 11;
    public static String USER;

    public static final String PACKGE_NAME = "Foh.data";
    public static final String VOLUM = "Volum";

    ImageButton foh;
    ImageView bg;
    TextView score, autoS, total;
    Button upgrade, option, auto_stop;

    DBManager manager;
    DBGameManager gameManager;
    Intent service;
    static boolean isServed = true;

    MediaPlayer eat,bgm;

    Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_scene);

        Intent getID = getIntent();
        USER = getID.getExtras().getString("user");

        if(USER == "")
            USER = "Fat";

        SharedPreferences sf = getSharedPreferences(PACKGE_NAME, MODE_PRIVATE);
        eat = new MediaPlayer();
        eat = MediaPlayer.create(this,R.raw.eating);
        // 볼륨을 불러오면 적용, 실패시 8로 고정
        eat.setVolume(sf.getFloat(VOLUM, 8), sf.getFloat(VOLUM, 8));


        manager = new DBManager(getApplicationContext(), "FOH.db",null, 1);
        gameManager = new DBGameManager(getApplicationContext(), "GAME.db",null, 1);

        SQLiteDatabase db = manager.getReadableDatabase();
        if(db == null)
            manager.onCreate(db); // 테이블이 없으면 생성

        db = gameManager.getReadableDatabase();
        if(db == null)
            gameManager.onCreate(db); // 테이블이 없으면 생성

        manager.checkUser(USER); // 없으면 해당 아이디로 생성
        gameManager.checkGame(USER); // 없으면 해당 아이디로 생성
        //manager.printData(manager.USER_TABLE);
        //gameManager.printData(gameManager.GAME_TABLE);

        bg = (ImageView)findViewById(R.id.bg);
        switch(Integer.parseInt(manager.getUserByKey(manager.USER_KEY_BG, USER))){
            case 1:
                bg.setImageResource(R.drawable.living_room);
                break;
            case 2:
                bg.setImageResource(R.drawable.wcdoland);
                break;
            case 3:
                bg.setImageResource(R.drawable.barbecue);
                break;
            default:
                bg.setImageResource(R.drawable.living_room);
        }

        // 매초마다 AUTO만큼 코인을 도는 서비스 실행
        service = new Intent(this, AutoService.class);
        startService(service);

        score = (TextView)findViewById(R.id.score_per_click);
        autoS = (TextView)findViewById(R.id.auto_per_sec);
        total = (TextView)findViewById(R.id.total_score);

        // 클릭, 오토, 전체 변수를 텍스트뷰에 입력
        score.setText(manager.getUserByKey(manager.USER_KEY_CLICK, USER) + "/per");
        autoS.setText(manager.getUserByKey(manager.USER_KEY_AUTO, USER) + "/sec");
        total.setText(manager.getUserByKey(manager.USER_KEY_EXP, USER) + " $FOIN");

        // 이미지 버튼 설정
        foh = (ImageButton)findViewById(R.id.foh);
        foh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // id, 이름, 클릭당, auto, exp, bg
                eat.start();
                manager.addFat(); // 클릭시 클릭변수만큼 상승
                total.setText(manager.getUserByKey(manager.USER_KEY_EXP, USER) + " $FOIN"); // 매번 EXP 변환
            }
        });

        auto_stop = (Button)findViewById(R.id.auto_stop);
        auto_stop.setOnClickListener(this);

        upgrade = (Button)findViewById(R.id.btn_upgrade);
        upgrade.setOnClickListener(this);

        option = (Button)findViewById(R.id.btn_option);
        option.setOnClickListener(this);

        // 매초마다 코인 텍스트 초기화
        thread = new Thread() {
            @Override
            public void run(){
                while(!isInterrupted()){
                    try{
                        Thread.sleep(1000);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                total.setText(manager.getUserByKey(manager.USER_KEY_EXP, USER) + " $FOIN");
                            }
                        });
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.start();


        ArrayList<Integer> playlist = new ArrayList<>();
        playlist.add(R.raw.foh_bgm);
        playlist.add(R.raw.imfat);

        bgm = new MediaPlayer();
        bgm = MediaPlayer.create(this,R.raw.imfat);
        bgm.setLooping(true);
        // 볼륨을 불러오면 적용, 실패시 8로 고정
        bgm.setVolume(sf.getFloat(VOLUM, 5), sf.getFloat(VOLUM, 6));
        bgm.start();

    }

    @Override
    protected void onResume() {
        super.onResume();
        bgm.start();
    }
    @Override
    protected void onPause() {
        super.onPause();
        bgm.pause();
    }
    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed(){
        stopService(service); // 백 버튼시 서비스 중단
        bgm.stop();
        finish(); // 액티비티 종료
    }

    @Override
    public void onClick(View v){
        Intent intent;
        switch(v.getId()){
            case R.id.btn_upgrade:
                intent = new Intent(this, UpgradeScene.class); // 인텐드 값을 입력
                intent.putExtra("click", score.getText()); // 현재 클릭 값을 입력
                intent.putExtra("auto", autoS.getText()); // 현재 오토 값을 입력
                intent.putExtra("exp", total.getText()); // 현재 코인 값을 입력
                intent.putExtra("user", USER); // 현재 코인 값을 입력

                startActivityForResult(intent, CALL_REQUEST); // upgrade에 보내기
                break;
            case R.id.btn_option:
                intent = new Intent(this, OptionScene.class);
                intent.putExtra("user", USER); // 현재 코인 값을 입력
                startActivity(intent); // upgrade에 보내기
                break;
            case R.id.auto_stop:
                if(isServed) {
                    stopService(service); // 백 버튼시 서비스 중단
                    auto_stop.setText("서비스 시작");
                    isServed = false;
                } else {
                    startService(service);
                    auto_stop.setText("서비스 중단");
                    isServed = true;
                }
                break;
        }
    }

    // 돌아오는 인텐트 값을 받기
    @SuppressLint("WrongConstant")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CALL_REQUEST){
            if(resultCode == RESULT_OK){
                score.setText(manager.getUserByKey(manager.USER_KEY_CLICK, USER) + "/per");
                autoS.setText(manager.getUserByKey(manager.USER_KEY_AUTO, USER) + "/sec");
                total.setText(manager.getUserByKey(manager.USER_KEY_EXP, USER) + " $FOIN");

                switch(Integer.parseInt(manager.getUserByKey(manager.USER_KEY_BG, USER))){
                    case 1:
                        bg.setImageResource(R.drawable.living_room);
                        break;
                    case 2:
                        bg.setImageResource(R.drawable.wcdoland);
                        break;
                    case 3:
                        bg.setImageResource(R.drawable.barbecue);
                        break;

                        default:
                            bg.setImageResource(R.drawable.living_room);
                            break;
                }
            }
        }
    }
}
