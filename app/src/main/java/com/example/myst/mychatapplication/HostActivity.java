package com.example.myst.mychatapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.*;
import java.net.*;
import java.util.*;

public class HostActivity extends BaseActivity {

    private TextView chatWindow;
    private EditText userText;
    private ServerSocket server;
    private ArrayList<ObjectOutputStream> echoStream;
    private ArrayList<ObjectInputStream> inpStream;
    private String name;
    private Button sendButton;
    private HostingTask t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        sendButton = (Button) findViewById(R.id.sendButton);
        userText = (EditText)findViewById(R.id.userText);
        chatWindow = (TextView)findViewById(R.id.chatWindow);
        chatWindow.setMovementMethod(new ScrollingMovementMethod());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Your Name");

        final EditText input = new EditText(this);

        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        builder.setView(input);

        builder.setPositiveButton("OK",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog,int which){
                name = input.getText().toString();
                chatWindow.append("Your Name is: "+ name +"\n");
               t = new HostingTask();
                t.execute(name);
            }
        });

        builder.show();
    }

    @Override
    public void onBackPressed(){

        try{
            server.close();
            for(ObjectOutputStream c:echoStream){
                c.close();
            }
            for(ObjectInputStream i:inpStream){
                i.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        if(t!=null && t.getStatus()==AsyncTask.Status.RUNNING){
            t.cancel(true);
        }
        this.finish();
    }


    class HostingTask extends AsyncTask<String,String,Void>{

        String hostName;

        @Override
        protected Void doInBackground(String... params) {
            hostName = params[0];
            startRunning();
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            String msg = values[values.length-1];

            chatWindow.append(msg);
        }

        //Start running

        public void startRunning(){
            try{
                server = new ServerSocket(6789,100);
                echoStream = new ArrayList<ObjectOutputStream>();
                inpStream = new ArrayList<>();
                while(true){
                    try{
                        waitForConnection();

                    } catch(EOFException eofException){
                        showMessage("\n Server Ended The Connection!...");
                    }
                }
            } catch(IOException ioException){
                ioException.printStackTrace();
            }
        }

        //client handler
        class ClientHandler implements Runnable{
            private Socket connect;
            private ObjectOutputStream output;
            private ObjectInputStream input;

            ClientHandler(Socket soc){
                connect = soc;
            }

            @Override
            public void run() {
                try {

                    output = new ObjectOutputStream(connect.getOutputStream());
                    output.flush();
                    input = new ObjectInputStream(connect.getInputStream());
                    showMessage("\n Streams Are Now Setup!...\n");

                    echoStream.add(output);
                    inpStream.add(input);

                    sendButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendMessage(userText.getText().toString());
                            userText.setText("");
                            
                        }
                    });

                    whileChatting(connect,output,input);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }



        //during the chat conversation
        private void whileChatting(Socket ss,ObjectOutputStream out,ObjectInputStream input) throws IOException{
            String message = "You Are Now Connected!\n";
            sendMessage(message);
            do{
                try{
                    message = (String) input.readObject();
                    echoToAllClients(message);
                    showMessage("\n" + message);
                }catch(ClassNotFoundException classNotFoundException){
                    showMessage("\n IDK that user send...");
                }
            }while(!message.equals("CLIENT - END"));

            if(message.equals("CLIENT - END")){
                closeCrap(ss,out,input);
            }

        }

        //echo message to other clients

        private void echoToAllClients(final String msg){
                    try{
                        for(ObjectOutputStream out : echoStream){
                            out.writeObject(msg);
                        }
                    }catch(IOException e){
                        e.printStackTrace();
                    }

            }

        //wait for connection
        private void waitForConnection() throws IOException{
            Socket connection;
            showMessage("Waiting For Someone To Connect......\n");
            connection = server.accept();
            showMessage("Now connected to "+ connection.getInetAddress().getHostName());
            new Thread(new ClientHandler(connection)).start();
        }

        //send a message to client

        private void sendMessage(String message){
            try{
              //  output.writeObject(name+" - "+ message);
              //  output.flush();
                echoToAllClients(name+" - "+ message);
                showMessage("\n"+name+" - "+ message);
            } catch(Exception e){
                showMessage("\n ERROR: Msg Can't be Sent.....");
            }
        }

        //update chatWindow


        private void showMessage(final String text){

                    publishProgress(text);

        }
        //close crap

        private void closeCrap(Socket sok,ObjectOutputStream output,ObjectInputStream input){
            showMessage("\n Closing Connection....\n");
            try{
                output.close();
                input.close();
                sok.close();
            } catch(IOException ioException){
                ioException.printStackTrace();
            }
        }

    }
}
