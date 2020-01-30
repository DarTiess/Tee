package com.example.tee;

import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

class DialogFragment extends Fragment {
    TextView info, infoip, msg;
    String message = "";
    ServerSocket serverSocket;

    final String LOG_TAG = "myLogs";
    final int MAX_STREAMS = 5;

    SoundPool soundPool;
    int soundIdShot;

    int streamIDshot;
    Intent serviceIntent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_dialog, container, false);



       info = (TextView) view.findViewById(R.id.info);
        infoip = (TextView)view. findViewById(R.id.infoip);
        msg = (TextView)view. findViewById(R.id.msg);
        infoip.setText(getIpAddress());

        Thread sockerServerThread = new Thread(new SocketServerThread());
        sockerServerThread.start();

        return view;
    }

    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetwork = NetworkInterface.getNetworkInterfaces();
            while (enumNetwork.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetwork.nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "LocalAdress: " + inetAddress.getHostAddress() + "\n";
                    }
                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
            ip += "Error!!! " + e.toString() + "\n";
        }

        return ip;
    }



    public interface OnFragmentInteractionListener {


    }

    private class SocketServerThread extends Thread {
        static final int socketPORT = 8080;
        int count = 0;

        public void run() {
            Socket socket = null;
            DataInputStream dataInputStream = null;
            DataOutputStream dataOutputStream = null;


            try {
                serverSocket = new ServerSocket(socketPORT);

                    info.setText("I'm listninig.. " + serverSocket.getLocalPort());



                while (true) {
                    socket = serverSocket.accept();
                    dataInputStream = new DataInputStream(socket.getInputStream());
                    dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    String messageFromClient = "";
                    messageFromClient = dataInputStream.readUTF();

                    count++;
                    message += "#" + count + " from " + socket.getInetAddress()
                            + " : " + socket.getPort() + "\n"
                            + "\t\tMessage from client: " + messageFromClient + "\n\n";



                            msg.setText(message);
                            soundPool.play(soundIdShot, 1, 1, 0, 0, 1);



                    String msgReplay = "Hello, maybe some tea?...#" + count;
                    dataOutputStream.writeUTF(msgReplay);

                    //    SocketServerReplyThread socketServerReplyThread=new SocketServerReplyThread(socket, count);
                    //    socketServerReplyThread.run();
                }
            } catch (IOException e) {
                e.printStackTrace();

                final String errMag = e.toString();

                        msg.setText(errMag);


            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (dataInputStream != null) {
                    try {
                        dataInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (dataOutputStream != null) {
                    try {
                        dataOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }


    }
}