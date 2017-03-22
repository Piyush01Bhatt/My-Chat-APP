package com.example.myst.mychatapplication;

import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * Created by Myst on 9/28/2016.
 */
public class BaseActivity extends AppCompatActivity {
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menu){
        int id=menu.getItemId();
        if(id==R.id.about){
            String abt = "An Offline Chatting App\n"+"Version 1.0";
            Toast.makeText(getApplicationContext(),abt,Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(menu);
    }

}
