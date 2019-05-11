package com.example.user.fatsim.Scenes;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.user.fatsim.DBGameManager;
import com.example.user.fatsim.DBManager;
import com.example.user.fatsim.R;
import com.kakao.kakaolink.KakaoLink;
import com.kakao.kakaolink.KakaoTalkLinkMessageBuilder;
import com.kakao.util.KakaoParameterException;

public class OptionScene extends Activity implements View.OnClickListener {

    DBManager manager;
    DBGameManager gameManager;

    Button kakao, backTo;

    String USER;
    Intent getValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.option_scene);

        getValue = getIntent(); // GameScene의 값 받기
        USER = getValue.getExtras().getString("user");

        // 데이터 베이스 가져오기
        manager = new DBManager(getApplicationContext(), "FOH.db",null, 1); // 유저 데이터 베이스
        gameManager = new DBGameManager(getApplicationContext(), "GAME.db",null, 1); // 게임 데이터 베이스

        kakao = (Button)findViewById(R.id.kakao);
        kakao.setOnClickListener(this);
        backTo = (Button)findViewById(R.id.backTo);
        backTo.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.kakao:
                shareLink();
                break;
            case R.id.backTo:
                finish();
                break;
        }
    }

    private void shareLink(){
        try{
            KakaoLink kakaoLink = KakaoLink.getKakaoLink(getApplicationContext());
            KakaoTalkLinkMessageBuilder messageBuilder = kakaoLink.createKakaoTalkLinkMessageBuilder();

            messageBuilder.addText("우리 오늘부터 1일이야~ <파오후 키우기> \n 나의 점수 : " + manager.getUserByKey(manager.USER_KEY_EXP, USER)); // 스코어도 보내기

            kakaoLink.sendMessage(messageBuilder,this);

        } catch (KakaoParameterException e) {
            Toast.makeText(this, "예외", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
