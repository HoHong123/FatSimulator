package com.example.user.fatsim.Scenes;

import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.fatsim.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainActivity";
    private GoogleApiClient mGoogleApiClient;

    public static final String PACKGE_NAME = "Foh.data";
    public static final String VOLUM = "Volum";

    TextView title;
    ImageView tGuy, bGuy;
    MediaPlayer bgm;

    Person currentPerson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 지난번 저장해놨던 사용자 입력값을 꺼내서 보여주기
        SharedPreferences sf = getSharedPreferences(PACKGE_NAME, MODE_PRIVATE);

        // 폰트 변경
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/BMJUA.ttf");
        // 이미지 뷰의 애니메이션 적용
        Animation rotate = AnimationUtils.loadAnimation(this, R.anim.rotate);
        Animation move = AnimationUtils.loadAnimation(this, R.anim.slide_repeat);

        title = (TextView)findViewById(R.id.title);
        title.setTypeface(typeface);
        title.setAnimation(move);

        tGuy = (ImageView)findViewById(R.id.topGuy);
        bGuy = (ImageView)findViewById(R.id.bottomGuy);
        tGuy.setAnimation(rotate);
        bGuy.setAnimation(rotate);

        bgm = new MediaPlayer();
        bgm = MediaPlayer.create(this,R.raw.foh_bgm);
        bgm.setLooping(true);
        // 볼륨을 불러오면 적용, 실패시 8로 고정
        bgm.setVolume(sf.getFloat(VOLUM, 5), sf.getFloat(VOLUM, 6));
        bgm.start();

    }

    public void mOnClick(View view){
        switch (view.getId()){
            case R.id.sign:
                Toast.makeText(this, "접속합니다", Toast.LENGTH_SHORT).show();

                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(MainActivity.this)
                        .addOnConnectionFailedListener(this)
                        .addApi(Plus.API)
                        .addScope(Plus.SCOPE_PLUS_PROFILE)
                        .build();

                mGoogleApiClient.connect();

                break;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "구글 플레이 연결이 되었습니다.");

        if (!mGoogleApiClient.isConnected() || Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) == null) {

            Log.d(TAG, "onConnected 연결 실패");
            callGame("Fat");

        } else {
            Log.d(TAG, "onConnected 연결 성공");

            Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
            callGame(currentPerson.getId());

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "연결 에러 " + connectionResult);

        if (connectionResult.hasResolution()) {

            Log.e(TAG,
                    String.format(
                            "Connection to Play Services Failed, error: %d, reason: %s",
                            connectionResult.getErrorCode(),
                            connectionResult.toString()));
            try {
                connectionResult.startResolutionForResult(this, 0);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, e.toString(), e);
            }
        }else{
            Toast.makeText(getApplicationContext(), "이미 로그인 중", Toast.LENGTH_SHORT).show();
        }
    }

    private void callGame(String USER){
        Intent intent = new Intent(this, GameScene.class);
        intent.putExtra("user",USER);
        startActivity(intent);
        //액티비티 전환 애니메이션
        overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_in_right);
        bgm.stop();

        finish();
    }

    /*
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startActivity(new Intent(this, GameScene.class));
                //액티비티 전환 애니메이션
                overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_in_right);
                bgm.stop();

                finish();
                break;
        }
        return false;
    }
*/
}
