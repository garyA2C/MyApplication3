package com.example.myapplication;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity implements IMyLocationConsumer {

    /**Elements généraux */
    private Context context;
    private MainActivity ma;
    private String playerName = "RootUser42";
    private int playerLevel= 42;
    private boolean premium=false;


    /**Elements d'interface */
    private Button bPlay, bActivate, bBlue, bRed, bPlus, bMinus, bGreen, bCyan, bMagenta, bYellow, bBlack, bWhite, bEraseAll, bSend;
    private ImageButton bUndo, bRemake;
    private SeekBar sRed, sGreen, sBlue, sThickness;
    private Switch sPremium;
    private TextView tColor, tThickness, tZoom;

    /**Elements géographiques*/
    private MapView mMapView;
    private ImageView iCircle;
    private MyLocationNewOverlay mLocationOverlay;
    private GpsMyLocationProvider mLocationProvider;
    private IMyLocationConsumer locationConsumer;
    private int zoom=20;

    private int ratio=1;
    private int rayon = (500+playerLevel*10)/ratio;

    private boolean backAvailable =false;
    private MyCanvas canvas;

    /** Elements du dessin */
    private ArrayList<ArrayList<IGeoPoint>> geoDrawingLines = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> linesInfos =new ArrayList<>();

    /** Elements du serveur */
    private Socket mSocket;
    public static final String SERVER_URL = "https://paint.antoine-rcbs.ovh:443";



    @Override
        protected void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            context = this;
            locationConsumer = this;
            ma = this;
            Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
            setContentView(R.layout.activity_main);

            //Instanciation du socket avec le serveur node.js
            try {
                mSocket = IO.socket(SERVER_URL);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            mSocket.connect();

            getSupportActionBar().setDisplayHomeAsUpEnabled(backAvailable);
            bPlay = findViewById(R.id.bouton_play);
            bActivate = findViewById(R.id.bouton_activate);
            bPlay.setEnabled(false);
            context =this;
            bActivate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bPlay.setEnabled(true);
                    System.out.println("bactivate");
                }
            });

            bPlay.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("ClickableViewAccessibility")
                @Override
                public void onClick(View v) {
                    System.out.println("baplay");
                    setContentView(R.layout.dessin);
                    backAvailable =true;
                    getSupportActionBar().setDisplayHomeAsUpEnabled(backAvailable);

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
                    mLocationProvider = new GpsMyLocationProvider(context);
                    mLocationProvider.startLocationProvider(locationConsumer);
                    mLocationProvider.setLocationUpdateMinDistance(10);
                    mLocationProvider.setLocationUpdateMinTime(5000);
                    mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(context), mMapView);
                    mLocationOverlay.enableMyLocation();
                    mLocationOverlay.enableFollowLocation();
                    mMapView.getOverlays().add(mLocationOverlay);
                    mMapView.setMaxZoomLevel(20.0);
                    mMapView.setMinZoomLevel(15.0);

                    //Instanciation du service de localisation

                    LinearLayout linear =findViewById(R.id.vMain);
                    canvas = new MyCanvas(context,ma);

                    linear.addView(canvas);
                    iCircle =findViewById(R.id.cercle);
                    updateRatio();
                    System.out.println("height"+ iCircle.getHeight());
                    sPremium =findViewById(R.id.premium);
                    sPremium.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked){
                                premium=true;
                                sPremium.setText("Premium");
                            }else{
                                premium=false;
                                sPremium.setText("Regular");
                            }
                        }
                    });
                    sRed =findViewById(R.id.proprouge);
                    sRed.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                    sRed.getThumb().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                    sRed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            changecouleur(Color.rgb(sRed.getProgress(), sGreen.getProgress(), sBlue.getProgress()));
                        }
                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }
                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    });
                    sGreen =findViewById(R.id.propvert);
                    sGreen.getProgressDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
                    sGreen.getThumb().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
                    sGreen.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            changecouleur(Color.rgb(sRed.getProgress(), sGreen.getProgress(), sBlue.getProgress()));
                        }
                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }
                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    });
                    sBlue =findViewById(R.id.propbleu);
                    sBlue.getProgressDrawable().setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN);
                    sBlue.getThumb().setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN);
                    sBlue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            changecouleur(Color.rgb(sRed.getProgress(), sGreen.getProgress(), sBlue.getProgress()));
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    });
                    sThickness =findViewById(R.id.propepaisseur);
                    sThickness.getProgressDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
                    sThickness.getThumb().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
                    sThickness.setMax(100+playerLevel*5);
                    sThickness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            tThickness.setText(sThickness.getProgress() +" px");
                            canvas.epaisseur(sThickness.getProgress());
                        }
                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }
                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    });
                    tZoom =findViewById(R.id.nivzoom);
                    tColor =findViewById(R.id.couleur);
                    tThickness =findViewById(R.id.epaisseur);
                    bPlus =findViewById(R.id.plus);
                    bPlus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mMapView.canZoomIn()){
                                zoom+=1;
                                mapController.setZoom(zoom);
                                updateRatio();
                            }
                            tZoom.setText(Integer.toString(zoom));
                        }
                    });
                    bMinus =findViewById(R.id.moins);
                    bMinus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mMapView.canZoomOut()){
                                zoom-=1;
                                mapController.setZoom(zoom);
                                updateRatio();
                            }
                            tZoom.setText(Integer.toString(zoom));
                        }
                    });
                    bSend =findViewById(R.id.envoyer);
                    bSend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MapDrawing drawing = new MapDrawing(canvas.getMapDrawingLines(), playerName, premium);
                            mSocket.emit("new_drawing", drawing.toJSON());
                            canvas.effacertout();
                        }
                    });
                    bUndo =findViewById(R.id.annuler);
                    bUndo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            canvas.annuler();
                        }
                    });

                    bRemake =findViewById(R.id.refaire);
                    bRemake.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            canvas.refaire();
                        }
                    });
                    bEraseAll =findViewById(R.id.effacertout);
                    bEraseAll.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder newDialog = new AlertDialog.Builder(context);
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
                    bBlue =findViewById(R.id.bleu);
                    bBlue.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            boutoncouleur(Color.BLUE);
                        }
                    });
                    bRed =findViewById(R.id.rouge);
                    bRed.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            boutoncouleur(Color.RED);
                        }
                    });
                    bGreen =findViewById(R.id.vert);
                    bGreen.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            boutoncouleur(Color.GREEN);
                        }
                    });
                    bCyan =findViewById(R.id.cyan);
                    bCyan.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            boutoncouleur(Color.CYAN);
                        }
                    });
                    bMagenta =findViewById(R.id.magenta);
                    bMagenta.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            boutoncouleur(Color.MAGENTA);
                        }
                    });
                    bYellow =findViewById(R.id.jaune);
                    bYellow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            boutoncouleur(Color.YELLOW);
                        }
                    });
                    bBlack =findViewById(R.id.noir);
                    bBlack.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            boutoncouleur(Color.BLACK);
                        }
                    });
                    bWhite =findViewById(R.id.blanc);
                    bWhite.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            boutoncouleur(Color.WHITE);
                        }
                    });
                }
            });
        }
        protected void changecouleur(int couleur){
            tColor.setBackgroundColor(couleur);
            tColor.setText(Integer.toHexString(couleur));
            tColor.setTextColor(Color.rgb(255-Color.red(couleur),255-Color.green(couleur),255-Color.blue(couleur)));
            canvas.couleur(couleur);
        }
        protected void boutoncouleur(int couleur){
            sBlue.setProgress(Color.blue(couleur));
            sGreen.setProgress(Color.green(couleur));
            sRed.setProgress(Color.red(couleur));
            changecouleur(couleur);
        }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        AlertDialog.Builder newquitter = new AlertDialog.Builder(context);
        if (backAvailable){
            AlertDialog.Builder newretour = new AlertDialog.Builder(context);
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
                    AlertDialog.Builder newDialog = new AlertDialog.Builder(context);
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

    public int getPlayerLevel(){
        return playerLevel;
    }

    public void updateRatio(){
        int i=20-zoom;
        ratio=(int)Math.pow(2, i);
        iCircle.getLayoutParams().height = rayon/ratio*2;
        iCircle.getLayoutParams().width = rayon/ratio*2;
        canvas.ratiochanged(ratio);
        System.out.println("ratio : " + ratio);
    }

    public void activerzoom(boolean bool){
        bPlus.setEnabled(bool);
        bMinus.setEnabled(bool);
    }

    public void activerbouton(boolean bool){
        bBlue.setEnabled(bool);
        bRed.setEnabled(bool);
        bGreen.setEnabled(bool);
        bCyan.setEnabled(bool);
        bMagenta.setEnabled(bool);
        bYellow.setEnabled(bool);
        bBlack.setEnabled(bool);
        bWhite.setEnabled(bool);
        bEraseAll.setEnabled(bool);
        bSend.setEnabled(bool);
        bUndo.setEnabled(bool);
        bRemake.setEnabled(bool);
        sRed.setEnabled(bool);
        sGreen.setEnabled(bool);
        sBlue.setEnabled(bool);
        sThickness.setEnabled(bool);
    }

    public MapView getMap(){
        return mMapView;
    }




}
