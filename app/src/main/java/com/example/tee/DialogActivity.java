package com.example.tee;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;


public class DialogActivity extends AppCompatActivity implements SoundPool.OnLoadCompleteListener {

    TextView textReponse;
    EditText address, textMes;
    Button btnConnect, btnClear;

    TextView info, infoip,msg;
    String message="";
    ServerSocket serverSocket;

    final String LOG_TAG="myLogs";
    final int MAX_STREAMS=5;

    SoundPool soundPool;
    int soundIdShot;

    int streamIDshot;
    Intent serviceIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);

        textReponse=(TextView)findViewById(R.id.reponse);
        address=(EditText)findViewById(R.id.address);
        textMes=(EditText)findViewById(R.id.sendMessage);
        btnClear=(Button)findViewById(R.id.clear);
        btnConnect=(Button)findViewById(R.id.connect);

        soundPool=new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC,0);
        soundPool.setOnLoadCompleteListener(this);

        info=(TextView)findViewById(R.id.info);
        infoip=(TextView)findViewById(R.id.infoip);
        msg=(TextView)findViewById(R.id.msg);
        soundIdShot=soundPool.load(this, R.raw.shot, 1);
        serviceIntent=new Intent(this, MyService.class);
        infoip.setText(getIpAddress());

        Thread sockerServerThread=new Thread(new DialogActivity.SocketServerThread());
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



    @Override
    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
        Log.d(LOG_TAG, "onLoadComplete, sampled= "+ sampleId +", staus "+status);

    }


    private class SocketServerThread extends Thread {
        static final int socketPORT=8080;
        int count=0;

        public void run(){
            Socket socket=null;
            DataInputStream dataInputStream=null;
            DataOutputStream dataOutputStream=null;


            try{
                serverSocket=new ServerSocket(socketPORT);


                DialogActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        info.setText("I'm listninig.. "+serverSocket.getLocalPort());

                    }
                });

                while (true){
                    socket=serverSocket.accept();
                    dataInputStream = new DataInputStream(socket.getInputStream());
                    dataOutputStream=new DataOutputStream(socket.getOutputStream());
                    String messageFromClient="";
                    messageFromClient=dataInputStream.readUTF();

                    count++;
                    message+="#"+count+" from "+socket.getInetAddress()
                            +" : "+socket.getPort()+"\n"
                            +"\t\tMessage from client: "+messageFromClient+"\n\n";

                   DialogActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            msg.setText(message);
                            soundPool.play(soundIdShot,1,1,0,0,1);

                        }
                    });

                    String msgReplay="Hello, maybe some tea?...#"+count;
                    dataOutputStream.writeUTF(msgReplay);

                    //    SocketServerReplyThread socketServerReplyThread=new SocketServerReplyThread(socket, count);
                    //    socketServerReplyThread.run();
                }
            } catch (IOException e) {
                e.printStackTrace();

                final  String errMag=e.toString();
                DialogActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        msg.setText(errMag);
                    }
                });

            }finally {
                if(socket!=null){
                    try{
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(dataInputStream!=null){
                    try{
                        dataInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(dataOutputStream!=null){
                    try{
                        dataOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }


    }
    public void onClickConnect(View view) {
        String tMess=textMes.getText().toString();
        if(tMess.equals("")){
            tMess=null;
            Toast.makeText(DialogActivity.this, "No message sent", Toast.LENGTH_SHORT).show();
        }

      DialogActivity.MyClientTask myClientTask=new DialogActivity.MyClientTask(
                address.getText().toString(),
                8080, tMess);
        myClientTask.execute();


    }

    public void onClickClear(View view) {
        textReponse.setText("");
    }

    public void clickToServer(View view) {
        Intent intent=new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private class MyClientTask extends AsyncTask<Void, Void, Void> {
        String adress;
        int port;
        String reponse;
        String textMesToServer;

        public MyClientTask(String addr, int prt, String tMess) {
            adress=addr;
            port=prt;
            textMesToServer=tMess;

        }

        @Override
        protected Void doInBackground(Void... args) {
            Socket socket=null;
            DataOutputStream dataOutputStream=null;
            DataInputStream dataInputStream=null;

            try{
                socket=new Socket(adress, port);
                dataOutputStream= new DataOutputStream(socket.getOutputStream());
                dataInputStream=new DataInputStream(socket.getInputStream());
                if(textMesToServer!=null){
                    dataOutputStream.writeUTF(textMesToServer);
                }
                reponse=dataInputStream.readUTF();
             /*   ByteArrayOutputStream byteArrayOutputStream=
                        new ByteArrayOutputStream(1024);
                byte[] buffer=new byte[1024];

                int bytesRead;
                InputStream inputStream=socket.getInputStream();

                while ((bytesRead=inputStream.read(buffer))!=-1){
                    byteArrayOutputStream.write(buffer,0,bytesRead);
                    reponse+=byteArrayOutputStream.toString("UTF-8");
                }*/

            } catch (UnknownHostException e) {
                e.printStackTrace();
                reponse=" HostException "+e.toString();
            } catch (IOException e) {
                e.printStackTrace();
                reponse="IOExeption "+e.toString();
            }finally {
                if(socket!=null){
                    try{
                        socket.close();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
                if(dataOutputStream!=null){
                    try{
                        dataOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(dataInputStream!=null){
                    try{
                        dataInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }
        protected  void onPostExecute(Void result){
            textReponse.setText(reponse);
            super.onPostExecute(result);
        }
    }
}