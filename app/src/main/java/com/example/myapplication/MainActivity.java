package com.example.myapplication;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;
import java.sql.Time;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity implements IMyLocationConsumer {
    private Button bplay, bactivate, bbleu, brouge, bplus, bmoins, bvert, bcyan, bmagenta, bjaune, bnoir, bblanc, beffacertout, benvoyer;
    private ImageButton bannuler, brefaire;
    private SeekBar srouge, svert, sbleu , sepaisseur;
    private Switch spremium;
    private TextView tcouleur, tepaisseur, tzoom;
    private Context c;
    private boolean retourpossible=false;
    private MyCanvas canvas;
    private static MapView mMapView;
    private MyLocationNewOverlay mLocationOverlay;
    private GpsMyLocationProvider mLocationProvider;
    private Socket mSocket;
    private static double zoom=20.0;
    private String playerName = "RootUser42";
    private IMyLocationConsumer localconsum;
    public static final String SERVER_URL = "https://paint.antoine-rcbs.ovh:443";
    private ArrayList<GeoPoint> lineLocations = new ArrayList<>();
    private ArrayList<ArrayList<IGeoPoint>> geo = new ArrayList<>();
    private boolean premium=false;
    private ArrayList<ArrayList<Integer>> info=new ArrayList<>();


    @Override
        protected void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            localconsum=this;

            //Instanciation du socket avec le serveur node.js
            try {
                mSocket = IO.socket(SERVER_URL);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            mSocket.connect();

            getSupportActionBar().setDisplayHomeAsUpEnabled(retourpossible);
            bplay= findViewById(R.id.bouton_play);
            bactivate= findViewById(R.id.bouton_activate);
            bplay.setEnabled(false);
            c=this;
            bactivate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bplay.setEnabled(true);
                    System.out.println("bactivate");
                }
            });

            bplay.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("ClickableViewAccessibility")
                @Override
                public void onClick(View v) {
                    System.out.println("baplay");
                    setContentView(R.layout.dessin);
                    retourpossible=true;
                    getSupportActionBar().setDisplayHomeAsUpEnabled(retourpossible);

                    OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
                        @Override
                        public void handleOnBackPressed() {
                            setContentView(R.layout.activity_main);
                        }
                    };
                    //requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

                    mMapView = findViewById(R.id.map);
                    mMapView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            return true;
                        }
                    });
                    mMapView.setTileSource(TileSourceFactory.MAPNIK);
                    mMapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT);
                    mMapView.setMultiTouchControls(false);
                    final IMapController mapController = mMapView.getController();
                    mapController.setZoom(zoom);
                    GeoPoint startPoint = new GeoPoint(45.7837763, 4.872973);
                    mapController.setCenter(startPoint);
                    mLocationProvider = new GpsMyLocationProvider(c);
                    mLocationProvider.startLocationProvider(localconsum);
                    mLocationProvider.setLocationUpdateMinDistance(3);
                    mLocationProvider.setLocationUpdateMinTime(1000);
                    mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(c), mMapView);
                    mLocationOverlay.enableMyLocation();
                    mLocationOverlay.enableFollowLocation();
                    mMapView.getOverlays().add(mLocationOverlay);
                    mMapView.setMaxZoomLevel(20.0);
                    mMapView.setMinZoomLevel(2.0);

                    //Instanciation du service de localisation

                    LinearLayout linear =findViewById(R.id.vMain);
                    canvas = new MyCanvas(c);
                    linear.addView(canvas);
                    spremium=findViewById(R.id.premium);
                    spremium.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked){
                                premium=true;
                                spremium.setText("Premium");
                            }else{
                                premium=false;
                                spremium.setText("Regular");
                            }
                        }
                    });
                    srouge=findViewById(R.id.proprouge);
                    srouge.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                    srouge.getThumb().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                    srouge.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            changecouleur(Color.rgb(srouge.getProgress(),svert.getProgress(),sbleu.getProgress()));
                        }
                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }
                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    });
                    svert=findViewById(R.id.propvert);
                    svert.getProgressDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
                    svert.getThumb().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
                    svert.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            changecouleur(Color.rgb(srouge.getProgress(),svert.getProgress(),sbleu.getProgress()));
                        }
                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }
                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    });
                    sbleu=findViewById(R.id.propbleu);
                    sbleu.getProgressDrawable().setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN);
                    sbleu.getThumb().setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN);
                    sbleu.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            changecouleur(Color.rgb(srouge.getProgress(),svert.getProgress(),sbleu.getProgress()));
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    });
                    sepaisseur=findViewById(R.id.propepaisseur);
                    sepaisseur.getProgressDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
                    sepaisseur.getThumb().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
                    sepaisseur.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            tepaisseur.setText(sepaisseur.getProgress() +" px");
                            canvas.epaisseur(sepaisseur.getProgress());
                        }
                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }
                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    });
                    tzoom=findViewById(R.id.nivzoom);
                    tcouleur=findViewById(R.id.couleur);
                    tepaisseur=findViewById(R.id.epaisseur);
                    bplus=findViewById(R.id.plus);
                    bplus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            zoom=mMapView.getZoomLevelDouble();
                            if (mMapView.canZoomIn()){
                                zoom+=1;
                            }
                            mapController.zoomIn();
                            tzoom.setText(Integer.toString((int)(zoom)));
                        }
                    });
                    bmoins=findViewById(R.id.moins);
                    bmoins.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            zoom=mMapView.getZoomLevelDouble();
                            if (mMapView.canZoomOut()){
                                zoom-=1;
                            }
                            mapController.zoomOut();
                            tzoom.setText(Integer.toString((int)(zoom)));
                        }
                    });
                    benvoyer=findViewById(R.id.envoyer);
                    benvoyer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            geo=canvas.getGeoPoints();
                            info=canvas.getInfoLigne();
                            for (int i=0; i<geo.size();i++){
                                mSocket.emit("new_line", lineToJSON(geo.get(i), info.get(i).get(0), info.get(i).get(1)));
                            }
                            canvas.effacertout();
                        }
                    });
                    bannuler=findViewById(R.id.annuler);
                    bannuler.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            canvas.annuler();
                        }
                    });

                    brefaire=findViewById(R.id.refaire);
                    brefaire.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            canvas.refaire();
                        }
                    });
                    beffacertout=findViewById(R.id.effacertout);
                    beffacertout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder newDialog = new AlertDialog.Builder(c);
                            newDialog.setMessage("Effacer tout ?");
                            newDialog.setPositiveButton("Oui", new DialogInterface.OnClickListener(){
                                public void onClick(DialogInterface dialog, int which){
                                    canvas.effacertout();
                                    dialog.dismiss();
                                }
                            });
                            newDialog.setNegativeButton("Non", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            newDialog.show();
                        }
                    });
                    bbleu=findViewById(R.id.bleu);
                    bbleu.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            boutoncouleur(Color.BLUE);
                        }
                    });
                    brouge=findViewById(R.id.rouge);
                    brouge.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            boutoncouleur(Color.RED);
                        }
                    });
                    bvert=findViewById(R.id.vert);
                    bvert.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            boutoncouleur(Color.GREEN);
                        }
                    });
                    bcyan=findViewById(R.id.cyan);
                    bcyan.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            boutoncouleur(Color.CYAN);
                        }
                    });
                    bmagenta=findViewById(R.id.magenta);
                    bmagenta.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            boutoncouleur(Color.MAGENTA);
                        }
                    });
                    bjaune=findViewById(R.id.jaune);
                    bjaune.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            boutoncouleur(Color.YELLOW);
                        }
                    });
                    bnoir=findViewById(R.id.noir);
                    bnoir.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            boutoncouleur(Color.BLACK);
                        }
                    });
                    bblanc=findViewById(R.id.blanc);
                    bblanc.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            boutoncouleur(Color.WHITE);
                        }
                    });
                }
            });
        }
        protected void changecouleur(int couleur){
            tcouleur.setBackgroundColor(couleur);
            tcouleur.setText(Integer.toHexString(couleur));
            tcouleur.setTextColor(Color.rgb(255-Color.red(couleur),255-Color.green(couleur),255-Color.blue(couleur)));
            canvas.couleur(couleur);
        }
        protected void boutoncouleur(int couleur){
            sbleu.setProgress(Color.blue(couleur));
            svert.setProgress(Color.green(couleur));
            srouge.setProgress(Color.red(couleur));
            changecouleur(couleur);
        }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        AlertDialog.Builder newquitter = new AlertDialog.Builder(c);
        if (retourpossible){
            AlertDialog.Builder newretour = new AlertDialog.Builder(c);
            newretour.setMessage("Retourner au menu principal ?");
            newretour.setPositiveButton("Oui", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    recreate();
                }
            });
            newretour.setNegativeButton("Non", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            newretour.show();
            return true;
        }else{
            newquitter.setMessage("Quitter l'application?");
            newquitter.setPositiveButton("Oui", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    finish();
                }
            });
            newquitter.setNegativeButton("Non", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            newquitter.show();
        }
        return false;
    }



    @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case android.R.id.home:
                    AlertDialog.Builder newDialog = new AlertDialog.Builder(c);
                    newDialog.setMessage("Retourner au menu principal ?");
                    newDialog.setPositiveButton("Oui", new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int which){
                            recreate();
                        }
                    });
                    newDialog.setNegativeButton("Non", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    newDialog.show();
                    return true;
            }
            return super.onOptionsItemSelected(item);
        }

    @Override
    public void onLocationChanged(Location location, IMyLocationProvider source) {

    }

    public static MapView getMap(){
        return mMapView;
    }

    private JSONObject lineToJSON(ArrayList<IGeoPoint> pointList, int color, int thickness) {
        JSONObject json = new JSONObject();
        JSONArray pointsArray = new JSONArray();
        try {
            json.put("player name", playerName);
            json.put("premium", premium);
            json.put("color", color);
            json.put("thickness", thickness);
            Date now = new Date();
            DateFormat datetimeform = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
            String datetime = datetimeform.format(now);
            json.put("datetime", datetime);
            for (IGeoPoint geoPoint : pointList) {
                JSONArray point = new JSONArray();
                point.put(geoPoint.getLatitude());
                point.put(geoPoint.getLongitude());
                pointsArray.put(point);
            }
            json.put("location", pointsArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}
