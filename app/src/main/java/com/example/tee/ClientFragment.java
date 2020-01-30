package com.example.tee;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientFragment extends Fragment {
    TextView textReponse;
    EditText address, textMes;
    Button btnConnect, btnClear;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_client, container, false);

        textReponse=(TextView)view.findViewById(R.id.reponse);
        address=(EditText)view.findViewById(R.id.address);
        textMes=(EditText)view.findViewById(R.id.sendMessage);
        btnClear=(Button)view.findViewById(R.id.clear);
        btnConnect=(Button)view.findViewById(R.id.connect);

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textReponse.setText("");
            }
        });
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tMess=textMes.getText().toString();

                ClientFragment.MyClientTask myClientTask=new ClientFragment.MyClientTask(
                        address.getText().toString(),
                        8080, tMess);
                myClientTask.execute();
            }
        });

        return view;
    }

    public interface OnFragmentInteractionListener {
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
