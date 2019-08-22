package com.example.choiww.tcpchatting_ex2;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ComponentName;
        import android.content.Context;
        import android.content.Intent;
        import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiInfo;
        import android.net.wifi.WifiManager;
        import android.os.Handler;
        import android.os.IBinder;
        import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.DataInputStream;
        import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
        import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.SocketHandler;

public class chat1 extends AppCompatActivity {
    String TAG = "find";
    TextView txtMessage;
    Button btnConnect, btnSend ,sendImage_btn;
    EditText editIp, editPort, editMessage, editNickname;
    ImageView imageView_test_img;
    Handler msgHandler; // android.os.handler
//    SocketClient client; // 만들쓰레드
//    ReceiveThread receive;// 만들쓰레드
//    SendThread send;// 만들쓰레드
    Socket socket;
//    LinkedList<SocketClient> threadList;
    Context context;
    String mac;
    String nickName;
    int PICTURE_REQUEST_CODE = 100;
    File file;
    ArrayList<File> file_array = new ArrayList<>();
    ArrayList<Uri> filePathArray = new ArrayList<>();
    File tempFile;

   Button serviceMethod;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat1);

        context = this;
        editIp = findViewById(R.id.editIp);
        editPort = findViewById(R.id.editPort);
        editNickname = findViewById(R.id.editNickname);
        editMessage = findViewById(R.id.editMessage);
        btnConnect = findViewById(R.id.btnConnect);
        btnSend = findViewById(R.id.btnSend);
        txtMessage = findViewById(R.id.txtMessage);
        sendImage_btn = findViewById(R.id.sendImage_btn);
        imageView_test_img = findViewById(R.id.imageView_test_img);


        //핸들러 작성
        msgHandler = new Handler(){
            // 네트워크작업은 메인쓰레드에서 하면 안되서 핸들러사용
            @Override
            public void handleMessage(Message msg) {
                /*안드로이드에서 메인쓰레드에서는 네트워크작업을 할 수 없기때문에 thread를 사용해야하고
                 * thread를 사용하면 ui를 제어할 수 없기 때문에 handler를 사용해야한다.
                 * 그래서 handler를 통해 외부에서 받아온 데이터를 앱 화면에 표시해주는 일이 많이때문에
                 * handleMessage같은 메서드도 미리정의되어 있고, Message라는 클래스도 만들어져 있는거 같다.
                 * 라고 생각했지만 아니었고
                 * 진실은 Thread간의 통신을 하기위해서 만들어진 객체라고 한다.
                 *
                 * Message class는 메시지 네용 외에도 다양한 데이터를 담을 수 있다.(ex 구분자.)
                 * */
                Log.d(TAG, "handleMessage: ################################");
                super.handleMessage(msg);
                if(msg.what == 111){
                    // 채팅서버로부터 수신한 메시지를 텍스트뷰에 추가
//                    txtMessage.append((msg.obj.toString()+"\n"));

                }
            }
        };
        // 바인드 된 서비스 메소드 실행 테스트 버튼
        serviceMethod=findViewById(R.id.serviceMethod_btn);
        serviceMethod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                tcpConn.myServiceFunc("chat1 - 서비스 연결됨");
                // 메시지 내용과 보낼 방버호를 보낸다.
                tcpConn.sendMessage(editMessage.getText().toString());// 서비스의 메서드를 통해서 메시지를 보낸다.
            }
        });

        // 서버에 접속하는 버튼
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick:접속 버튼 클릭됨");
//                client = new SocketClient(editNickname.getText().toString(), editIp.getText().toString()
//                        ,editPort.getText().toString());
//                client.start();
                // 서비스를
            }
        });
        // 메시지 전송 버튼
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editMessage.getText().toString();
                if(message != null || message.equals("")){
//                    send = new SendThread(socket);
//                    send.start();
                    editMessage.setText("");
                }
            }
        });
        sendImage_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 권한설정 tedpremission
                try {
//                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//                    tedPermission();//@@@@@@@@@@@@@@
                } catch (IllegalStateException ignore) {

                }

                // 이미지 가져오기
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                //사진을 여러개 선택할수 있도록 한다
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"),  PICTURE_REQUEST_CODE);

            }
        });

        Intent Service = new Intent(this, TCP_connection_service.class);
        bindService(Service,mConnection,Context.BIND_AUTO_CREATE);



    }

//    @Override
////    public void startActivityForResult(Intent intent, int requestCode) {
////        super.startActivityForResult(intent, requestCode);
////        if (requestCode == PICTURE_REQUEST_CODE){
////            if ()
////        }
////    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        FileInputStream fileInputStream;
        DataInputStream dataInputStream;
        if(requestCode == PICTURE_REQUEST_CODE){
            if (resultCode == RESULT_OK){

                ClipData clipData = data.getClipData();
                Cursor cursor = null;

                for (int i=0;clipData.getItemCount()>i;i++){
                    Uri getUri = clipData.getItemAt(i).getUri();// 이미지 url
//                    clipData.getItemAt(i)
                    Log.d(TAG, "onActivityResult: 이미지 url"+i+" = "+getUri);
//                    try {
//                        InputStream imageStream = getContentResolver().openInputStream(getUri);
//                    } catch (FileNotFoundException e) {
//                    }

                    /*uri 스키마를 content:/// 에서 file:///로 변경한다.
                     * */
//                    String imagePath = getRealPathFromURI(getUri);
//                    Log.d(TAG, "onActivityResult: imagePath :"+imagePath);
//                    File file = new File(imagePath);
//                    sendMessage("Start"+file.length());

//                    Cursor c = getContentResolver().query(Uri.parse(getUri.toString()), null,null,null,null);
//                    c.moveToNext();
//                    String absolutePath = c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA));
                    // 절대 경로를 받아왔다.
//                    try {
//                        fileInputStream = new FileInputStream(absolutePath);
//                        dataInputStream = new DataInputStream(fileInputStream);
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    }

//                    String[] proj = {MediaStore.Images.Media.DATA};
//
//                    assert getUri != null;
//                    cursor = getContentResolver().query(getUri,proj,null,null,null);
//
//                    assert cursor != null;
//                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//
//                    cursor.moveToFirst();
//
//                    File afile = new File(cursor.getString(column_index));




//                    File afile = new File(getUri.getPath());
//                    Log.d(TAG, "onActivityResult: 이미지 이름(getName) : "+afile.getName());
//                    file_array.add(file);// 이미지uri를 파일로 바꿔주는 코드
                    filePathArray.add(getUri);
//                    imageView_test_img.setI
                    tcpConn.sendMessage(filePathArray);
//                    tcpConn.sendMessage(file_array);
                    /** 여기깢!!!!!!!!!!!!!!!!!!!!!!!!!!
                     지금 content 스키마를 url 스키마로 바꿔서 파일로만들어 넣으면 파일이 있다고 뜰지 테스트 코드를 만들던중*/
//                Uri uri = data.getData();
//
//                try{
//                    /*uri 스키마를 content:/// 에서 file:///로 변경한다.
//                    * */
//                    String[] proj_1 = {MediaStore.Images.Media.DATA};
//
//                    assert uri != null;
//                    cursor = getContentResolver().query(uri,proj_1,null,null,null);
//
//                    assert cursor != null;
//                    int column_index1 = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//
//                    cursor.moveToFirst();
//
//                    tempFile = new File(cursor.getString(column_index1));
//
//
//                }catch (Exception e){
//
//                }finally {
//                    if(cursor != null){
//                        cursor.close();
//                    }
//                }


                }
            }
        }
    }
    private String getRealPathFromURI(Uri contentUri) {// 이미지의 진짜 path를 가져오기 위한 메서드

        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        cursor.moveToFirst();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        return cursor.getString(column_index);
    }


    // 서비스에게 데이터를 전달하고 전달받기 위한 코드
    TCP_connection_service tcpConn;
//    private TCP_connection_service mBindService;
//    String TAG = "find";

    private ServiceConnection mConnection = new ServiceConnection() {
        //서비스가 바인드 되면 이 메서드가 발동된다.
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected: tcp con 생성 및 bind 함");
            TCP_connection_service.TCP_conn_serv_binder binder = (TCP_connection_service.TCP_conn_serv_binder) service;
            tcpConn = binder.getService();
            tcpConn.registerCallback("chat1", mCallback);
            // registerCallback 메서드가 발동되면 서버와 연결하는 소켓이 실행된다.
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            tcpConn = null;
        }
    };
    private TCP_connection_service.ICallback mCallback=new TCP_connection_service.ICallback() {
        @Override
        public void remoteCall() {
            Log.d("find","called by service");
        }
        public void remoteCall(Message message){
            // 이 메서드를 통해서 서비스가 서버에서 받은 메시지를 화면에 표시한다.
            String chat_message = message.obj.toString();
            Log.d(TAG, "chat1.activity - remoteCall: chat_message = "+chat_message);
            txtMessage.append(chat_message+"\n");
        }
    };
    private void tedPermission() {

        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                // 권한 요청 성공

            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                // 권한 요청 실패
            }
        };
        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage(getResources().getString(R.string.permission_2))
                .setDeniedMessage(getResources().getString(R.string.permission_1))
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();
    }

//    class SocketClient extends Thread{
//        boolean threadAlive;
//
//        String ip;
//        String port;
//
//        OutputStream outputStream = null;
//        DataOutputStream output = null;
//        public SocketClient(String nickname, String ip, String port){
//            threadAlive = true;
//            nickName = nickname;
//            this.ip =ip;
//            this.port = port;
//
//        }
//        public void run(){
//            try{
//                socket = new Socket(ip, Integer.parseInt(port));
//                output = new DataOutputStream(socket.getOutputStream());
//                receive = new ReceiveThread(socket); // 쓰레드에서 반복문을 통해서 서버가 보내는 메시지를 받아온다.
//                receive.start();
//
//                WifiManager mng = (WifiManager) context.getSystemService( WIFI_SERVICE);
//                WifiInfo info = mng.getConnectionInfo();
//                mac = info.getMacAddress();
//                output.writeUTF(nickName);
//            }catch(Exception e){
//                e.printStackTrace();
//            }
//        }
//    }
    // end of socketClient
//    class ReceiveThread extends Thread {
//        Socket socket = null;
//        DataInputStream input = null;
//        public ReceiveThread(Socket socket){
//            this.socket = socket;
//            try {
//                input = new DataInputStream(socket.getInputStream());
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//        }
//        public void run(){
//            try{
//                while(input != null){
//                    // 채팅 서버로부터 받은 메시지
//                    String msg = input.readUTF();
//                    if(msg != null){
//                        //핸들러에게 전달할 메시지 객체호출(이미 새성되어 있는 객체 재활용)
//                        Message hdmsg = msgHandler.obtainMessage();
//                        hdmsg.what = 111;//메시지의 식별자
//                        hdmsg.obj=msg; // 메시지의 본문
//                        //핸들러에게 메시지 전달(화면 변경 요청)
//                        msgHandler.sendMessage(hdmsg);
//                    }
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
    //end of ReceiveThread
    //내부ㅡㄹ래스
//    class SendThread extends Thread{
//        Socket socket;
//        String sendmsg = editMessage.getText().toString();
//        DataOutputStream output;
//        public SendThread(Socket socket){
//            this.socket = socket;
//            try{
//                //채팅서버로 메시지를 보내기 위한 스트림 생성
//                output = new DataOutputStream(socket.getOutputStream());
//            }catch(Exception e){
//                e.printStackTrace();
//            }
//        }
//        public void run(){
//            try{
//                if(output != null){
//                    if(sendmsg != null){
////                        output.writeUTF(mac+":"+sendmsg);
//                        // 여기서 메시지 데이터를 json화해서 보내야것네
//                        output.writeUTF(nickName+">> :"+sendmsg);
//                    }
//                }
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//        }
//    }
}
