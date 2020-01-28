package com.example.tee;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity implements SoundPool.OnLoadCompleteListener {

    TextView info, infoip,msg;
    String message="";
   ServerSocket serverSocket;

    final String LOG_TAG="myLogs";
    final int MAX_STREAMS=5;

    SoundPool soundPool;
    int soundIdShot;

    int streamIDshot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        soundPool=new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC,0);
        soundPool.setOnLoadCompleteListener(this);

        info=(TextView)findViewById(R.id.info);
        infoip=(TextView)findViewById(R.id.infoip);
        msg=(TextView)findViewById(R.id.msg);
        soundIdShot=soundPool.load(this, R.raw.shot, 1);

        infoip.setText(getIpAddress());

        Thread sockerServerThread=new Thread(new SocketServerThread());
        sockerServerThread.start();
    }

    private String getIpAddress() {
String ip="";
try{
    Enumeration<NetworkInterface> enumNetwork=NetworkInterface.getNetworkInterfaces();
    while (enumNetwork.hasMoreElements()){
        NetworkInterface networkInterface=enumNetwork.nextElement();
        Enumeration<InetAddress> enumInetAddress=networkInterface.getInetAddresses();
        while (enumInetAddress.hasMoreElements()){
            InetAddress inetAddress=enumInetAddress.nextElement();

            if(inetAddress.isSiteLocalAddress()){
                ip+="LocalAdress: "+inetAddress.getHostAddress()+"\n";
            }
        }
    }

} catch (SocketException e) {
    e.printStackTrace();
    ip+="Error!!! " +e.toString()+"\n";
}

        return ip;
    }

    public void clickToClient(View view) {
        Intent intent=new Intent(this, ClientActivity.class);
        startActivity(intent);

    }

    @Override
    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
        Log.d(LOG_TAG, "onLoadComplete, sampled= "+ sampleId +", staus "+status);

    }

    private class SocketServerThread extends Thread {
        static final int socketPORT=8080;
        int count=0;

        public void run(){
            try{
                serverSocket=new ServerSocket(socketPORT);


                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        info.setText("I'm listninig.. "+serverSocket.getLocalPort());
                    }
                });

                while (true){
                    Socket socket=serverSocket.accept();
                    count++;
                    message+="#"+count+" from "+socket.getInetAddress()+" : "+socket.getPort()+"\n";
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            msg.setText(message);
                            soundPool.play(soundIdShot,1,1,0,0,1);
                        }
                    });

                    SocketServerReplyThread socketServerReplyThread=new SocketServerReplyThread(socket, count);
                    socketServerReplyThread.run();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    private class SocketServerReplyThread extends Thread {

        private  Socket hostThreadSocket;
        int cnt;

        SocketServerReplyThread(Socket socket, int c){
            hostThreadSocket=socket;
            cnt=c;
        }

        public void run() {
            OutputStream outputStream;
            String msgReplay="One Tee please #"+ cnt+" ask \n";

            try {
                outputStream=hostThreadSocket.getOutputStream();
                PrintStream printStream=new PrintStream(outputStream);
                printStream.print(msgReplay);
                printStream.close();

                message+=" Pardonnez moi: "+msgReplay+"\n";

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        msg.setText(message);

                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                message+=" Error!"+ e.toString()+"\n";

            }
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    msg.setText(message);
                }
            });

        }
    }



}
