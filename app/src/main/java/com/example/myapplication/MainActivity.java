package com.example.myapplication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.multidex.BuildConfig;
import com.example.myapplication.Inscription;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import androidx.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity implements IMyLocationConsumer {

    /**Elements généraux */
    private Context context;
    private MainActivity MA;
    private drawingView DV;

    private double latitude;
    private double longitude;
    private Location loc;
    private static String baseURL = "https://paint.antoine-rcbs.ovh/login";
    private File file;


    /**Elements d'interface */

    /**Elements géographiques*/
    private IMyLocationConsumer locationConsumer;

    private boolean backAvailable = false;


    /** Elements du serveur */
    private Socket mSocket;
    public static final String SERVER_URL = "https://paint.antoine-rcbs.ovh:443";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        locationConsumer = this;
        MA = this;
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
//        setContentView(R.layout.activity_drawing);




        //Instanciation du socket avec le serveur node.js
        try {
            mSocket = IO.socket(SERVER_URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(backAvailable);
//        bPlay = findViewById(R.id.buttonPlay);
//        bActivate = findViewById(R.id.buttonActivate);
//        bPlay.setEnabled(false);
//        bActivate.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                bPlay.setEnabled(true);
//            }
//        });
//
//        bPlay.setOnClickListener(new View.OnClickListener() {
//            @SuppressLint("ClickableViewAccessibility")
//            @Override
//            public void onClick(View v) {
        DV = new drawingView(MA,context,mSocket,locationConsumer);
//            }
//        });
        mSocket.connect();
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        AlertDialog.Builder alertExit = new AlertDialog.Builder(context);
        alertExit.setMessage("Se déconnecter?");
        alertExit.setPositiveButton("Oui", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                System.exit(0);
            }
        });
        alertExit.setNegativeButton("Non", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertExit.show();
        return false;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog.Builder alertExit = new AlertDialog.Builder(context);
        alertExit.setMessage("Se déconnecter?");
        alertExit.setPositiveButton("Oui", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                System.exit(0);
            }
        });
        alertExit.setNegativeButton("Non", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertExit.show();
        return super.onOptionsItemSelected(item);
    }
    public void backAvailable(boolean is){
        backAvailable=is;
    }
    @Override
    public void onLocationChanged(Location location, IMyLocationProvider source) {
        if (loc==null) {
            loc = location;
            DV.locationChanged(loc);
            System.out.println("initial location : "+location.getLatitude()+" , "+location.getLongitude());
            DV.setProgressBarOff();
        }else if (loc.distanceTo(location)>500){
            loc=location;
            DV.locationChanged(loc);
            System.out.println("new location : "+location.getLatitude()+" , "+location.getLongitude());
        }
    }

    public GeoPoint getLocation(){
        if (loc==null){
            return null;
        }else{
            return new GeoPoint(loc.getLatitude(), loc.getLongitude());
        }
    }

}
