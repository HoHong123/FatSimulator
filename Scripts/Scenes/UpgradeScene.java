package com.example.user.fatsim.Scenes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.fatsim.DBGameManager;
import com.example.user.fatsim.DBManager;
import com.example.user.fatsim.R;

public class UpgradeScene extends Activity implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    Button btn_click, btn_auto, btn_return;
    TextView click, auto, exp;

    String [] bg;
    long target;
    int tmpPosition;

    DBManager manager;
    DBGameManager gameManager;

    static String USER;
    Intent getValue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upgrade_scene);

        getValue = getIntent(); // GameScene의 값 받기
        USER = getValue.getExtras().getString("user");
        Log.i("UP : ", USER);

        // 데이터 베이스 가져오기
        manager = new DBManager(getApplicationContext(), "FOH.db",null, 1); // 유저 데이터 베이스
        gameManager = new DBGameManager(getApplicationContext(), "GAME.db",null, 1); // 게임 데이터 베이스

        // 각 버튼 입력
        btn_click = (Button)findViewById(R.id.up_click);
        btn_click.setOnClickListener(this);
        btn_auto = (Button)findViewById(R.id.up_auto);
        btn_auto.setOnClickListener(this);
        btn_return = (Button)findViewById(R.id.backTo);
        btn_return.setOnClickListener(this);

        // 각 텍스트에 비용을 보여줌
        click = (TextView)findViewById(R.id.up_click_text);
        click.setText("강화 비용 : " + gameManager.getGameByKey(gameManager.GAME_KEY_CLICK, USER));

        auto = (TextView)findViewById(R.id.up_auto_text);
        auto.setText("강화 비용 : " + gameManager.getGameByKey(gameManager.GAME_KEY_AUTO, USER));

        exp = (TextView)findViewById(R.id.exp);
        exp.setText(manager.getUserByKey(manager.USER_KEY_EXP, USER) + " $FOIN");

        bg = getResources().getStringArray(R.array.BackGround); // 스피너의 리스트 받음

        // 스피너
        Spinner spin = (Spinner) findViewById(R.id.spinner);
        spin.setOnItemSelectedListener(this);
        ArrayAdapter<String> aa = new ArrayAdapter<String>(this, R.layout.bg_spinner_list, bg);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(aa);

        spin.setSelection(Integer.parseInt(manager.getUserByKey(manager.USER_KEY_BG,USER))-1); // 선택되어 있는 배경을 디폴트 값으로 입력


        // 1초단위로 exp에 유저의 exp값을 입력
        Thread thread = new Thread() {
            @Override
            public void run(){
                while(!isInterrupted()){
                    try{
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                exp.setText(manager.getUserByKey(manager.USER_KEY_EXP,USER) + " $FOIN");
                            }
                        });
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.start();

    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, GameScene.class);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id){

        int gbg = Integer.parseInt(gameManager.getGameByKey(gameManager.GAME_KEY_BG, USER)); // 확장된 배경 값을 받아옴
        Log.i("Position : ", Integer.toString(position) + ", " + gbg);

        // 허가된 배경 안에 있으면 바꿈
        if(position < gbg){ // 이미 구매한 배경이면
            manager.updateUser(manager.USER_KEY_BG,"=",Integer.toString(position+1), USER); // 배경변경
        } else if(position == gbg){ // 다음 구매 가능한 배경을 선택하면

            target = (position * position) * 1000000; // 다음 배경의 가격을 확인

            if(target > Integer.parseInt(manager.getUserByKey(manager.USER_KEY_EXP, USER))){ // 비용이 더 크면
                Toast.makeText(UpgradeScene.this, "비용이 부족하여 구매가 불가합니다.", Toast.LENGTH_SHORT).show(); // 실패 문구 출력
            } else { // 살수 있으면

                tmpPosition = position;

                // 다이어로그를 출력하여 구매의사를 마지막으로 확인
                new AlertDialog.Builder(UpgradeScene.this)
                        .setTitle("배경 구매!")
                        .setMessage("배경을 " + target + "$FOIN에 구매하시겠습니까?")
                        .setIcon(R.drawable.main_foh)
                        .setPositiveButton("구매하기",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        if(target <= Long.parseLong(manager.getUserByKey(manager.USER_KEY_EXP, USER)))
                                            buyBG();
                                        else
                                            Toast.makeText(UpgradeScene.this,"$FOIN이 모자랍니다",Toast.LENGTH_SHORT).show();
                                        dialog.cancel();
                                    }
                                })
                        .setNegativeButton("취소하기", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }).show();
            }
        } else if(position > gbg){
            Toast.makeText(UpgradeScene.this, Long.toString(target) + "$FOIN 필요. 아직 구매가 불가합니다.", Toast.LENGTH_SHORT).show();
        }

    }
    @Override
    public void onNothingSelected(AdapterView<?> parent){}

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.up_click : // 클릭 강화 버튼
                // 플레이어의 EXP가 게임이 요구하는 클릭 업그레이드 보다 많거나 같으면 실행
                Log.i("Up : ", gameManager.getGameByKey(gameManager.GAME_KEY_CLICK, USER) + ", " + USER);
                Log.i("Up : ", gameManager.getGameByKey(gameManager.GAME_KEY_CLICK, USER) + ", " + manager.getUserByKey(manager.USER_KEY_EXP, USER));
                if(Integer.parseInt(gameManager.getGameByKey(gameManager.GAME_KEY_CLICK, USER)) <= Integer.parseInt(manager.getUserByKey(manager.USER_KEY_EXP, USER))) {
                    Log.i("Up : ", gameManager.getGameByKey(gameManager.GAME_KEY_CLICK, USER) + ", " + manager.getUserByKey(manager.USER_KEY_EXP, USER));

                    manager.updateUser(manager.USER_KEY_CLICK, "*", "2", USER); // 플레이어 클릭 능력치 2배로 상승
                    manager.updateUser(manager.USER_KEY_EXP, "-", gameManager.getGameByKey(gameManager.GAME_KEY_CLICK, USER), USER); // EXP 감소
                    gameManager.updateGame(gameManager.GAME_KEY_CLICK, "*", "3", USER); // 다음 클릭 업글 요구치 상승

                    click.setText( "강화 비용 : " + gameManager.getGameByKey(gameManager.GAME_KEY_CLICK, USER));
                    exp.setText(manager.getUserByKey(manager.USER_KEY_EXP, USER) + " $FOIN");
                }
                break;
            case R.id.up_auto : // AUTO 강화 버튼
                // 플레이어의 EXP가 게임이 요구하는 AUTO 업그레이드 보다 많거나 같으면 실행
                if(Integer.parseInt(gameManager.getGameByKey(gameManager.GAME_KEY_AUTO, USER)) <= Integer.parseInt(manager.getUserByKey(manager.USER_KEY_EXP, USER))){
                    manager.updateUser(manager.USER_KEY_AUTO, "+","100", USER); // 플레이어 AUTO 능력치 2배로 상승
                    manager.updateUser(manager.USER_KEY_EXP, "-", gameManager.getGameByKey(gameManager.GAME_KEY_AUTO, USER), USER); // EXP 감소
                    gameManager.updateGame(gameManager.GAME_KEY_AUTO, "*", "3", USER); // 다음 AUTO 업글 요구치 상승

                    auto.setText("강화 비용 : " + gameManager.getGameByKey(gameManager.GAME_KEY_AUTO, USER));
                    exp.setText(manager.getUserByKey(manager.USER_KEY_EXP, USER) + " $FOIN");
                }
                break;
            case R.id.backTo: // 돌아가기 버튼
                Intent intent = new Intent(this, GameScene.class);
                setResult(Activity.RESULT_OK, intent);
                finish();
                break;
        }
    }

    private void buyBG(){ // 구매시
        manager.updateUser(manager.USER_KEY_EXP, "-", Long.toString(target),USER);
        manager.updateUser(manager.USER_KEY_BG, "=", Integer.toString(tmpPosition+1),USER);

        gameManager.updateGame(gameManager.GAME_KEY_BG, "+", "1", USER);
    }
}
