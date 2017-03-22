package com.example.myst.mychatapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.*;
import java.net.*;

public class ClientActivity extends BaseActivity {

    private TextView chatWindow;
    private EditText userText;
    private String name;
    private Button sendButton;

    Socket connection;
    String outMs;
    String serverIP;
    String message = "";
    ObjectOutputStream output;
    ObjectInputStream input;
    ClientTask t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

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
               t =  new ClientTask();
                t.execute();
            }
        });

        builder.show();

       AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setTitle("Enter Host Name or Host's IP");

        final EditText input2 = new EditText(this);

        input2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        builder2.setView(input2);

        builder2.setPositiveButton("OK",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog,int which){
                serverIP = input2.getText().toString();
            }
        });

        builder2.show();
    }

    @Override
    public void onBackPressed(){

        try{
            output.close();
            input.close();
            connection.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        if(t!=null && t.getStatus()==AsyncTask.Status.RUNNING){
            t.cancel(true);
        }
        this.finish();
    }

    class ClientTask extends AsyncTask<Void,String,Void>{



        @Override
        protected Void doInBackground(Void... params) {

             startRunning();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            String msg = values[values.length-1];

            chatWindow.append(msg);
        }

        public void startRunning(){

            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessage(userText.getText().toString());
                    setWriteMessage(name+" - "+userText.getText().toString());
                    userText.setText("");
                }
            });

            try{
                connectToServer();
                setupStreams();
                whileChatting();
            } catch(EOFException eofException){
                showMessage("\n Client terminated connection");
            } catch(IOException ioException){
                ioException.printStackTrace();
            }finally{
                closeCrap();
            }
        }

        //connect to server
        private void connectToServer() throws IOException{
            showMessage("Attempting connection......\n");
            connection = new Socket(InetAddress.getByName(serverIP),6789);
            showMessage("Connected to:"+ connection.getInetAddress().getHostName()) ;
        }

        //set up streams and receive messages
        private void setupStreams() throws IOException{
            output = new ObjectOutputStream(connection.getOutputStream());
            output.flush();
            input = new ObjectInputStream(connection.getInputStream());
            showMessage("\nYour Streams Are Good To Go!\n");
        }

        // while chatting with server
        private void whileChatting() throws IOException{
            do{
                try{
                    message = (String) input.readObject();
                    //  if(str.size()>0 && !message.equals(str.get(str.size()-1)))
                    if(message.equals(getWriteMessage()))
                    {
                        System.out.println("Same message");
                    }
                    else
                        showMessage("\n"+message);

                }catch(ClassNotFoundException classNotfoundException){
                    showMessage("\n I don't know that");
                }
            }while(!message.equals("SERVER - END"));
        }

        //set write message
        private void setWriteMessage(String mm){
            outMs = mm;
        }


        //get write message
        private String getWriteMessage(){
            return outMs;
        }


        //close streams and sockets


        private void closeCrap(){
            showMessage("\n Closing Connection...");
            try{
                output.close();
                input.close();
                connection.close();
            }catch(IOException ioException){
                ioException.printStackTrace();
            }
        }
        //sending messages to server

        private void sendMessage(String message){
            try{
                output.writeObject(name+" - "+message);
                output.flush();
                showMessage("\n"+name+" - "+message);
            }catch(IOException ioException){
                chatWindow.append("\nSomething went wrong...");
            }
        }

        //showMessage update chatWindow

        private void showMessage(final String m){

            publishProgress(m);
        }

    }
}
