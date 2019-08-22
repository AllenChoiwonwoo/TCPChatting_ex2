package com.example.choiww.tcpchatting_ex2;

        import android.content.ComponentName;
        import android.content.Context;
        import android.content.Intent;
        import android.content.ServiceConnection;
        import android.os.IBinder;
        import android.os.Message;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import java.io.BufferedInputStream;
        import java.io.BufferedReader;
        import java.io.BufferedWriter;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.InputStreamReader;
        import java.io.OutputStreamWriter;
        import java.io.PrintWriter;
        import java.net.Socket;
        import android.os.Handler;
        import android.util.Log;
        import android.view.View;
        import android.view.View.OnClickListener;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.TextView;
        import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private String html = "";
    private Handler mHandler;
    private Socket socket = null;
    private String name;
    private BufferedReader networkReader;
    private BufferedWriter networkWriter;
    private String ip = "172.30.34.151"; // IP

    TCP_connection_service tcpConn;// 바인드된 서비스 객체
    String TAG = "find";
    Button startBindService_btn;
    Button callService;

    // 서비스와 연결하는 법 정의
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TCP_connection_service.TCP_conn_serv_binder binder = (TCP_connection_service.TCP_conn_serv_binder) service;
            tcpConn = binder.getService();
            tcpConn.registerCallback("main", mCallback);
            Log.d(TAG, "onServiceConnected: 서비스 시작되어연결됨");

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            tcpConn = null;
        }
    };
    private TCP_connection_service.ICallback mCallback=new TCP_connection_service.ICallback() {
        @Override
        public void remoteCall() {
            Log.d("find","mainActivity - called by service");
        }

        @Override
        public void remoteCall(Message message) {

        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new Handler();

        // 서비스를 바인드 시킨다.
//        Button bindServiceBtn = findViewById(R.id.startBindService_btn);
//        bindServiceBtn.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent Service = new Intent(this, TCP_connection_service.class);
//                bindService(Service,mConnection,Context.BIND_AUTO_CREATE);
//            }
//        });
        startBindService_btn = findViewById(R.id.startBindService_btn);
        startBindService_btn.setOnClickListener(this);

        callService = findViewById(R.id.CallService_btn);
        callService.setOnClickListener(this);
    // 서비스의 메서드를 작동시키는 버튼
//        Button callService = findViewById(R.id.CallService_btn);
//        callService.setOnClickListener(new View.OnClickListener(){
//
//            @Override
//            public void onClick(View v) {
////                tcpConn.myServiceFunc("hithere");
//            }
//        });

    }

    public void onClick(View v){
        Intent intent = null;
        switch (v.getId()){
            case R.id.button:
                intent = new Intent(this, chat1.class);
                startActivity(intent);
                break;
            case R.id.startBindService_btn:
                Log.d(TAG, "onClick: startBindService");
                Intent Service = new Intent(this, TCP_connection_service.class);
                bindService(Service,mConnection,Context.BIND_AUTO_CREATE);
                // 메인에서 바인드는 안하고 생성만 하면?
//                startService(Service);
                break;
            case R.id.CallService_btn:
                tcpConn.myServiceFunc("hi there");
        }
    }
}
