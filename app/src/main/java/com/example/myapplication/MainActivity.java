package com.example.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
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
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements IMyLocationConsumer {
    private Button bPlay, bActivate, bBlue, bRed, bPlus, bMinus, bGreen, bCyan, bMagenta, bYellow, bBlack, bWhite, bEraseAll, bSend;
    private ImageButton bUndo, bRedo;
    private SeekBar sRed, sGreen, sBlue, sWidth;
    private Switch sPremium;
    private TextView tColor, tWidth, tZoom;
    private Context context;
    private boolean canReturn = false;
    private MyCanvas canvas;
    private MapView mMapView;
    private ImageView iCircle;
    private MyLocationNewOverlay mLocationOverlay;
    private GpsMyLocationProvider mLocationProvider;
    private Socket mSocket;
    private int zoom=20;
    private String playerName = "RootUser42";
    private int playerLevel= 42;
    private IMyLocationConsumer localConsumer;
    private MainActivity MA;
    public static final String SERVER_URL = "https://paint.antoine-rcbs.ovh:443";
    private ArrayList<GeoPoint> lineLocations = new ArrayList<>();
    private ArrayList<ArrayList<IGeoPoint>> geoPoints = new ArrayList<>();
    private boolean premium=false;
    private ArrayList<ArrayList<Integer>> info =new ArrayList<>();
    private int ratio=1;
    private int radium = (500+playerLevel*10)/ratio;

    @Override
        protected void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
            setContentView(R.layout.activity_main);
            localConsumer =this;
            MA =this;

            //Instanciation du socket avec le serveur node.js
            try {
                mSocket = IO.socket(SERVER_URL);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            mSocket.connect();

            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(canReturn);
            bPlay = findViewById(R.id.buttonPlay);
            bActivate = findViewById(R.id.buttonActivate);
            bPlay.setEnabled(false);
            context =this;
            bActivate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bPlay.setEnabled(true);
                    System.out.println("bActivate");
                }
            });

            bPlay.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("ClickableViewAccessibility")
                @Override
                public void onClick(View v) {
                    System.out.println("bPlay");
                    setContentView(R.layout.activity_drawing);
                    canReturn =true;
                    Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(canReturn);

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
                    mapController.setZoom((double)zoom);
                    GeoPoint startPoint = new GeoPoint(45.7837763, 4.872973);
                    mapController.setCenter(startPoint);
                    mLocationProvider = new GpsMyLocationProvider(context);
                    mLocationProvider.startLocationProvider(localConsumer);
                    mLocationProvider.setLocationUpdateMinDistance(3);
                    mLocationProvider.setLocationUpdateMinTime(1000);
                    mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(context), mMapView);
                    mLocationOverlay.enableMyLocation();
                    mLocationOverlay.enableFollowLocation();
                    mMapView.getOverlays().add(mLocationOverlay);
                    mMapView.setMaxZoomLevel(20.0);
                    mMapView.setMinZoomLevel(15.0);

                    //Instanciation du service de localisation

                    LinearLayout linear =findViewById(R.id.layoutCanvas);
                    canvas = new MyCanvas(context, MA);

                    linear.addView(canvas);
                    iCircle =findViewById(R.id.imageCircle);
                    updateRatio();
                    System.out.println("height"+ iCircle.getHeight());
                    sPremium =findViewById(R.id.switchPremium);
                    sPremium.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked){
                                premium=true;
                                sPremium.setText(R.string.premium);
                            }else{
                                premium=false;
                                sPremium.setText(R.string.regular);
                            }
                        }
                    });
                    sRed =findViewById(R.id.seekRed);
                    sRed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            changeColor(Color.rgb(sRed.getProgress(), sGreen.getProgress(), sBlue.getProgress()));
                        }
                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }
                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    });
                    sGreen =findViewById(R.id.seekGreen);
                    sGreen.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            changeColor(Color.rgb(sRed.getProgress(), sGreen.getProgress(), sBlue.getProgress()));
                        }
                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }
                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    });
                    sBlue =findViewById(R.id.seekBlue);
                    sBlue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            changeColor(Color.rgb(sRed.getProgress(), sGreen.getProgress(), sBlue.getProgress()));
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    });

                    sWidth =findViewById(R.id.seekWidth);
                    sWidth.setMax(100+playerLevel*5);
                    sWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            tWidth.setText(getString(R.string.width, String.format(Locale.getDefault(),"%d", sWidth.getProgress())));
                            canvas.setWidth(sWidth.getProgress());
                        }
                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }
                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    });

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        sRed.getProgressDrawable().setColorFilter(new BlendModeColorFilter(Color.RED, BlendMode.SRC_ATOP));
                        sRed.getThumb().setColorFilter(new BlendModeColorFilter(Color.RED, BlendMode.SRC_ATOP));
                        sGreen.getProgressDrawable().setColorFilter(new BlendModeColorFilter(Color.GREEN, BlendMode.SRC_ATOP));
                        sGreen.getThumb().setColorFilter(new BlendModeColorFilter(Color.GREEN, BlendMode.SRC_ATOP));
                        sBlue.getProgressDrawable().setColorFilter(new BlendModeColorFilter(Color.BLUE, BlendMode.SRC_ATOP));
                        sBlue.getThumb().setColorFilter(new BlendModeColorFilter(Color.BLUE, BlendMode.SRC_ATOP));
                        sWidth.getProgressDrawable().setColorFilter(new BlendModeColorFilter(Color.BLACK, BlendMode.SRC_ATOP));
                        sWidth.getThumb().setColorFilter(new BlendModeColorFilter(Color.BLACK, BlendMode.SRC_ATOP));
                    } else {
                        sRed.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                        sRed.getThumb().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                        sGreen.getProgressDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
                        sGreen.getThumb().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
                        sBlue.getProgressDrawable().setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN);
                        sBlue.getThumb().setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN);
                        sWidth.getProgressDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
                        sWidth.getThumb().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
                    }

                    tZoom =findViewById(R.id.textZoom);
                    tColor =findViewById(R.id.textColor);
                    tWidth =findViewById(R.id.textWidth);
                    bPlus =findViewById(R.id.buttonPlus);
                    bPlus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mMapView.canZoomIn()){
                                zoom+=1;
                                mapController.setZoom((double)zoom);
                                updateRatio();
                                canvas.eraseAll();
                            }
                            tZoom.setText(String.format(Locale.getDefault(),"%d", zoom));
                        }
                    });
                    bMinus =findViewById(R.id.buttonMinus);
                    bMinus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mMapView.canZoomOut()){
                                zoom-=1;
                                mapController.setZoom((double)zoom);
                                updateRatio();
                                canvas.eraseAll();
                            }
                            tZoom.setText(String.format(Locale.getDefault(),"%d", zoom));
                        }
                    });
                    bSend =findViewById(R.id.buttonSend);
                    bSend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            geoPoints =canvas.getGeoPoints();
                            info =canvas.getInfoLine();
                            for (int i = 0; i< geoPoints.size(); i++){
                                mSocket.emit("new_line", lineToJSON(geoPoints.get(i), info.get(i).get(0), info.get(i).get(1)));
                            }
                            canvas.eraseAll();
                        }
                    });
                    bUndo =findViewById(R.id.buttonUndo);
                    bUndo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!canvas.undo()){
                                ActivateZoomButton(true);
                            }
                        }
                    });

                    bRedo =findViewById(R.id.buttonRedo);
                    bRedo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (canvas.redo()){
                                ActivateZoomButton(false);
                            }
                        }
                    });
                    bEraseAll =findViewById(R.id.buttonEraseAll);
                    bEraseAll.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder newDialog = new AlertDialog.Builder(context);
                            newDialog.setMessage("Effacer tout ?");
                            newDialog.setPositiveButton("Oui", new DialogInterface.OnClickListener(){
                                public void onClick(DialogInterface dialog, int which){
                                    canvas.eraseAll();
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
                    bBlue =findViewById(R.id.buttonBlue);
                    bBlue.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            buttonColor(Color.BLUE);
                        }
                    });
                    bRed =findViewById(R.id.buttonRed);
                    bRed.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            buttonColor(Color.RED);
                        }
                    });
                    bGreen =findViewById(R.id.buttonGreen);
                    bGreen.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            buttonColor(Color.GREEN);
                        }
                    });
                    bCyan =findViewById(R.id.buttonCyan);
                    bCyan.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            buttonColor(Color.CYAN);
                        }
                    });
                    bMagenta =findViewById(R.id.buttonMagenta);
                    bMagenta.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            buttonColor(Color.MAGENTA);
                        }
                    });
                    bYellow =findViewById(R.id.buttonYellow);
                    bYellow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            buttonColor(Color.YELLOW);
                        }
                    });
                    bBlack =findViewById(R.id.buttonBlack);
                    bBlack.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            buttonColor(Color.BLACK);
                        }
                    });
                    bWhite =findViewById(R.id.buttonWhite);
                    bWhite.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            buttonColor(Color.WHITE);
                        }
                    });
                }
            });
        }
        protected void changeColor(int col){
            tColor.setBackgroundColor(col);
            tColor.setText(Integer.toHexString(col));
            tColor.setTextColor(Color.rgb(255-Color.red(col),255-Color.green(col),255-Color.blue(col)));
            canvas.setColor(col);
        }
        protected void buttonColor(int col){
            sBlue.setProgress(Color.blue(col));
            sGreen.setProgress(Color.green(col));
            sRed.setProgress(Color.red(col));
            changeColor(col);
        }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (canReturn){
            AlertDialog.Builder alertReturn = new AlertDialog.Builder(context);
            alertReturn.setMessage("Retourner au menu principal ?");
            alertReturn.setPositiveButton("Oui", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    recreate();
                }
            });
            alertReturn.setNegativeButton("Non", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            alertReturn.show();
            return true;
        }else{
            AlertDialog.Builder alertExit = new AlertDialog.Builder(context);
            alertExit.setMessage("Quitter l'application?");
            alertExit.setPositiveButton("Oui", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    finish();
                }
            });
            alertExit.setNegativeButton("Non", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            alertExit.show();
        }
        return false;
    }



    @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            if (item.getItemId() == android.R.id.home){
                AlertDialog.Builder alertReturn = new AlertDialog.Builder(context);
                alertReturn.setMessage("Retourner au menu principal ?");
                alertReturn.setPositiveButton("Oui", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which){
                        recreate();
                    }
                });
                alertReturn.setNegativeButton("Non", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertReturn.show();
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
        iCircle.getLayoutParams().height = radium /ratio*2;
        iCircle.getLayoutParams().width = radium /ratio*2;
        canvas.ratioChange(ratio);
        System.out.println("ratio : " + ratio);
    }

    public void ActivateZoomButton(boolean bool){
        bPlus.setEnabled(bool);
        bMinus.setEnabled(bool);
    }

    public void ActivateOtherButton(boolean bool){
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
        bRedo.setEnabled(bool);
        sRed.setEnabled(bool);
        sGreen.setEnabled(bool);
        sBlue.setEnabled(bool);
        sWidth.setEnabled(bool);
    }

    public MapView getMap(){
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
            DateFormat dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
            String datetime = dateTimeFormat.format(now);
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
