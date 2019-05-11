package com.example.user.fatsim;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class AutoService extends Service {
    public AutoService() {}

    DBManager manager;
    boolean isRun = true;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        manager = new DBManager(getApplicationContext(), "FOH.db",null, 1);
    }

    @Override
    public void onDestroy() {
        isRun = false;
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 매초마다 실행하는 쓰레드 생성 생성
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    manager.addAutoFat();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();
        return super.onStartCommand(intent, flags, startId);
    }
}
