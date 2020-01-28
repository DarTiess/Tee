package com.example.tee;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientActivity extends AppCompatActivity {
TextView textReponse;
EditText address;
//textMes;
Button btnConnect, btnClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        
        textReponse=(TextView)findViewById(R.id.reponse);
        address=(EditText)findViewById(R.id.address);
      //  textMes=(EditText)findViewById(R.id.textMes);
        btnClear=(Button)findViewById(R.id.clear);
        btnConnect=(Button)findViewById(R.id.connect);
        
        
    }

    public void onClickConnect(View view) {
        MyClientTask myClientTask=new MyClientTask(
                address.getText().toString(),
                8080);
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
        String textMes;

        public MyClientTask(String addr, int prt) {
            adress=addr;
            port=prt;

        }

        @Override
        protected Void doInBackground(Void... args) {
            Socket socket=null;

            try{
                socket=new Socket(adress, port);
                ByteArrayOutputStream byteArrayOutputStream=
                        new ByteArrayOutputStream(1024);
                byte[] buffer=new byte[1024];

                int bytesRead;
                InputStream inputStream=socket.getInputStream();

                while ((bytesRead=inputStream.read(buffer))!=-1){
                    byteArrayOutputStream.write(buffer,0,bytesRead);
                    reponse+=byteArrayOutputStream.toString("UTF-8");
                }

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
            }

            return null;
        }
        protected  void onPostExecute(Void result){
            textReponse.setText(reponse);
            super.onPostExecute(result);
        }
    }
}