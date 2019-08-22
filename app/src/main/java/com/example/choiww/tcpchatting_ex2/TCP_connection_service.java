package com.example.choiww.tcpchatting_ex2;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class TCP_connection_service extends Service {

    Handler msgHandler; // android.os.handler
    SocketClient client; // 만들쓰레드
    ReceiveThread receive;// 만들쓰레드
    SendThread send;// 만들쓰레드
    Socket socket;

    //    LinkedList<SocketClient> threadList;
    Context context;
    String mac;
    String nickName;
    String sendMessage;
//    ArrayList<File> file_array;
    ArrayList<Uri> filePathArray;
    int a=0;

    public TCP_connection_service() {
    }
    String TAG = "find";
    private final IBinder mBinder = new TCP_conn_serv_binder();
    private ICallback mCallback;
    ICallback mCallback_chat1;
    int callback_divider;

    @SuppressLint("HandlerLeak")
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: 서비스 생성");
        //  서버와 소켓 연결
        client = new SocketClient("yeji","172.16.100.95","5001" );
        client.start();
//        Log.d(TAG, "onCreate: 여기까지 왔다는 건 서비스가 생성되면서 서버와 소켓연결하는 쓰레드가 동작했다는것");
//        //  서버에서 데이터 받을 수 있는 쓰레드 열기
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
                 *  handler.sendMessage 를 하면 이 메서드(handleMessage)가 실행된다.
                 *
                 * */
                
                super.handleMessage(msg);
                Log.d(TAG, "handleMessage: !!!!!!!!!!!!!!!!!!!!");

                if(msg.what == 111){
                    // 채팅서버로부터 수신한 메시지를 텍스트뷰에 추가
//                    txtMessage.append((msg.obj.toString()+"\n"));// 이건 에러가 난다.
                    mCallback_chat1.remoteCall(msg);
                }
            }

        };

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: 스타트 커멘드");
//        client = new SocketClient("yeji", "192.168.0.76","5001");
//        client.start();
//        return super.onStartCommand(intent, flags, startId);
//        Thread bt = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while(true){
//                    try {
//                        Thread.sleep(2000);
//                    }catch(InterruptedException e){
//                        e.printStackTrace();
//                    }
////                        if(!MainActivity.TFLAG){
////                            Log.i("background-counter","---백스레드 중지---");
////                            break;
////                        }
//
//                    Log.i("background-counter",String.valueOf(++a));
//                }
//            }
//        });
//        bt.setName("백그라운드스레드");
//        bt.start();
//
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {

//        throw new UnsupportedOperationException("Not yet implemented");
        Log.d(TAG, "onBind: ");
        return mBinder;
    }


    public interface ICallback{
        public void remoteCall();
        public void remoteCall(Message message);
    }

    public void registerCallback(String activity_name, ICallback cb){
        // 서비서에 (엑티비티로)callback 하는 메서드를 등록시긴다.
        //cb 객체를 통해서 엑티비티의 remoteCall 메서드를 실행시켜
        // 엑티비티로 데이터를 보낼 수 있다.`
        if (activity_name.equals("main")){
            mCallback = cb;
            callback_divider = 1;
//            여기서 서버와 통신하는 쓰레드를 실행시켜야 겠지
//            client = new SocketClient("yeji", "172.16.100.95","5001");
//            client.start();
        }else {
            mCallback_chat1 = cb;
            Log.d(TAG, "resigterCallback: chat1 등록됨");
            callback_divider = 2;
        }

//        mCallback = cb;

    }
    public void myServiceFunc(String str){
        Log.d(TAG, "myServiceFunc: 엑티비티에서 서비스의 메서드를 사용(엑티비티에서 서비스로 메시지(strng)전달 메시지 : "+str);
//        mCallback.remoteCall();
        if (callback_divider == 1){
            mCallback.remoteCall();
        }else if (callback_divider == 2){
            mCallback_chat1.remoteCall();
        }
    }
    public void sendMessage(String sendMessage){
        Log.d(TAG, "sendMessage: 서비스의 메시지 보내기 메서드 발동 ");
        this.sendMessage = sendMessage;
        send = new SendThread(socket);
        send.start();

    }
    public void sendMessage(ArrayList arrayList){
        Log.d(TAG, "sendMessage: 서비스의 메시지 보내기 메서드 발동 - 이미지 ");
//        this.sendMessage = sendMessage;
//        this.file_array = arrayList;
        this.filePathArray = arrayList;
        send = new SendThread(socket);
        send.start();

    }


    public class TCP_conn_serv_binder extends Binder {
        TCP_connection_service getService(){
            return TCP_connection_service.this;
        }
    }

    class SocketClient extends Thread{
        boolean threadAlive;

        String ip;
        String port;

        OutputStream outputStream = null;
        DataOutputStream output = null;
        public SocketClient(String nickname, String ip, String port){
            threadAlive = true;
            nickName = nickname;
            this.ip =ip;
            this.port = port;

        }
        public void run(){
            try{
                socket = new Socket(ip, Integer.parseInt(port));
                Log.d(TAG, "run: 서버와 소켓연결 성공 : "+socket.toString());
                output = new DataOutputStream(socket.getOutputStream());
                receive = new ReceiveThread(socket); // 쓰레드에서 반복문을 통해서 서버가 보내는 메시지를 받아온다.
                receive.start();

//                WifiManager mng = (WifiManager) context.getSystemService( WIFI_SERVICE);
//                WifiInfo info = mng.getConnectionInfo();
//                mac = info.getMacAddress();
                output.writeUTF(nickName);
                Log.d(TAG, "run: 서버로 클라의 닉네임 전송");
            }catch(Exception e){
                e.printStackTrace();
            }

        }
    }

    class ReceiveThread extends Thread {
        Socket socket = null;
        DataInputStream input = null;
        public ReceiveThread(Socket socket){
            this.socket = socket;
            try {
                input = new DataInputStream(socket.getInputStream());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        public void run(){
            try{
                while(input != null){
                    Log.d(TAG, "ReceiveThread- Run: inputStream을 통해 데이터 받기 스타트");
                    // 채팅 서버로부터 받은 메시지
                    String msg = input.readUTF();
                    Log.d(TAG, "run: 받은 메시지 : "+msg);
                    if(msg != null){
//                        핸들러에게 전달할 메시지 객체호출(이미 새성되어 있는 객체 재활용)
                        Message hdmsg = msgHandler.obtainMessage();
                        hdmsg.what = 111;//메시지의 식별자
                        hdmsg.obj=msg; // 메시지의 본문
                        //핸들러에게 메시지 전달(화면 변경 요청)
//                        msgHandler.sendMessage(hdmsg);
//                        mCallback_chat1.remoteCall(hdmsg);// 서비스의 메시지 받는 쓰레드가 받은 메시지를 콜백 메서드를 통해서 엑티비티로 보내준다.
                        if (mCallback_chat1 == null) {
                            Log.d(TAG, "run: 채팅 서버의 환영 메시지 : "+msg);
                        }else {
                            msgHandler.sendMessage(hdmsg);
//                            mCallback_chat1.remoteCall(hdmsg);
                            // ###스레드에서 바로 콜백의 remoteCall 메서드로 화면의 변경하니 에러가 떴고(다른쓰레드에서 ui에 접근 못한다는 에러)
                            // ###핸들러-handlemessage 를 통해 서비스로 데이터를 넘겨서 서비스가 콜백의 remoteCall 메서드를 실행하게 하니 화면에 보낸 메시지가 보였다.

                        }
                        // 여기서 엑티비티로 받은 메시지를 보내야한다.
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //end of ReceiveThread
    //내부ㅡㄹ래스
    class SendThread extends Thread{
        Socket socket;
//        String sendmsg = editMessage.getText().toString();
        String sendmsg = " test1 "; // 여기에 엑티비티에서 받아온 보낼 메시지를 넣어야한다.
        DataOutputStream output;
        BufferedOutputStream bos;
        PrintWriter out;
        public SendThread(Socket socket){
            this.socket = socket;
            try{
                //채팅서버로 메시지를 보내기 위한 스트림 생성
                bos = new BufferedOutputStream(socket.getOutputStream());
                output = new DataOutputStream(socket.getOutputStream());
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        public void run(){
            try{
                if(output != null){
                    if(sendMessage != null){ // 문자를 보낼때
//                        output.writeUTF(mac+":"+sendmsg);
                        // 여기서 메시지 데이터를 json화해서 보내야것네
                        output.writeUTF(nickName+">> :"+sendMessage);
                        sendMessage = null;

                    }else if (filePathArray != null) {// 이미지를 보낼때
                        Log.d(TAG, "run:서비스에서 쓰레드를 통해 이미지를 보냅니다.");
                        // arraylist안의 이미지 file을
                        // byte어레이로 바꿔서
                        // 서버로 보내야하낟.
                        for (int i = 0; filePathArray.size() > i; i++){
                            InputStream imamgeStream = getContentResolver().openInputStream(filePathArray.get(i));
                            Bitmap selectedImage = BitmapFactory.decodeStream(imamgeStream);

                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            selectedImage.compress(Bitmap.CompressFormat.PNG, 0, bos);
                            byte[] array = bos.toByteArray();

                            output.writeInt(array.length);
                            output.write(array, 0, array.length);
                            Log.d(TAG, "run: image send ");

//                            out.println("test"+i); // 파일의 이름을 보내고
//                            out.flush();
//                            // 파일의 절대 경로를 받아서 이걸 변환해 서버로 보내는 코드
//                            Log.d(TAG, "run: 이미지 path를 바이트 어레이로 바꿔 소켓을 통해서 보내는 반복문 시작");
//                            DataInputStream dis = new DataInputStream(new
//                                    FileInputStream(new File(Environment.getExternalStorageDirectory(), file_array.get(i).getPath()))); //읽을 파일 경로 적어 주시면 됩니다.

//                            DataOutputStream dos = new
//                                    DataOutputStream(sock.getOutputStream());


//                            byte[] buf = new byte[2048];
//                            long totalReadBytes = 0;
//                            int readBytes;
//                            while ((readBytes = dis.read(buf)) > 0) { //길이 정해주고 딱 맞게 서버로 보냅니다.
//                                output.write(buf, 0, readBytes); // 파일을 보낸다.
//                                totalReadBytes += readBytes;
//                            }
                            Log.d(TAG, "run: 전송완료");
//                            output.close();

//                            // 여기서 부턴 이전 코드
//
////                            Image img = ImageIO.read(new File(<file_array.get(i)>));
//                            Log.d(TAG, "run: file name = "+file_array.get(i).getName());
//                            output.writeUTF(file_array.get(i).getName());
////                            System.out.println("파일개수?" +files[i].getName()+files[i].length());
//                            Log.d(TAG, "run: 파일개수 "+file_array.get(i).getName());
//                            output.writeUTF(file_array.get(i).getName());// dataOutputStream으로 이름을 보낸다.
//
////                            FileInputStream fis = new FileInputStream(file_array.get(i));// 파일을 스트림에 담는다.
//
//                            FileInputStream fis = new FileInputStream(file_array.get(i));
//                            BufferedInputStream bis = new BufferedInputStream(fis);
//                            byte[] buf = new byte[4096]; //buf 생성합니다.
//                            int theByte = 0;
//                            while ((theByte = bis.read(buf)) != -1) // BufferedInputStream으로
//                            // 클라이언트에 보내기 위해 write합니다.
//                            {
//                                bos.write(buf,0,theByte);
//                            }
//                            Log.d(TAG, "run: "+i+" 전송완료");
                        }
                        bos.flush();
//                        bos.close();
                        output.flush();
//                        output.close();
//                        Log.d(TAG, "");

                    }else {
                        Log.d(TAG, "run: 보낼 메시지도 이미지도  없습니다.");
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }
}
/*
class fileServer extends Thread {   //소켓 연결할 Thread 생성합니다.
    int fileportNumber;
    ServerSocket fileServerSocket;
    String TAG = "find";
    public void run() {
        fileportNumber = 11002;
        try {
            fileServerSocket=new ServerSocket(fileportNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            File[] files = new File(path).listFiles(); // path경로에있는 파일 모두 읽어들임니다.
            try {
                for(int i=0;i<files.length;i++){    //파일 개수 만큼 Socket 돕니다.
                    Socket fileSocket = fileServerSocket.accept();
                    // 클라와 연결된 소켓을 생성
                    BufferedOutputStream bos = new BufferedOutputStream(fileSocket.getOutputStream());
                    // 데이터를 내보낸다(전송?저장?)
                    DataOutputStream dos = new DataOutputStream(bos);
                    // 데이터를 내보낸다.
                    System.out.println("파일개수?" +files[i].getName()+files[i].length());

                    dos.writeUTF(files[i].getName());   //파일 이름 받아옵니다.
                    // 데이터의 이름을 받아와 보낸다.
                    FileInputStream fis = new FileInputStream(files[i]);
                    // 보낼 파일을 읽어서 fis에 담는다.
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    //fis의 버퍼(테이터를 담은 그릇)을 읽어온다.
                    byte[] buf = new byte[4096]; //buf 생성합니다.
                    int theByte = 0;
                    while ((theByte = bis.read(buf)) != -1) // BufferedInputStream으로
                    // 클라이언트에 보내기 위해 write합니다.
                    // theByte에 버퍼에 있는 데이터를 바이트어레이[4096]형으로 담는다.
                    {
                        Log.d(TAG, "run: ");
                       bos.write(buf,0,theByte);
                       // 데이터를 담은 bytearray를 보낸다.
                    }
                    System.out.println("마지막값"+theByte);
                    System.out.println("송신완료");
                    dos.close();
                    bos.close();
                    fileSocket.close();   //socket 닫아줌
                }
                fileServerSocket.close();  //파일을 다 전송했으면 ServerSocket 닫아줌니다.
            } catch (Exception e) {
               e.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

*/
