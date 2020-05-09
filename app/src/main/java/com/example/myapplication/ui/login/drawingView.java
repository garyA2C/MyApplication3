package com.example.myapplication.ui.login;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.myapplication.MainActivity;
import com.example.myapplication.MapDrawing;
import com.example.myapplication.MyCanvas;
import com.example.myapplication.R;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

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
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class drawingView {
    /**Elements généraux */
    private Context context;
    private MainActivity MA;
    private String playerName = "RootUser42";
    private int playerLevel= 42;
    private boolean premium=false;


    /**Elements d'interface */
    private Button bBlue, bRed, bPlus, bMinus, bGreen, bCyan, bMagenta, bYellow, bBlack, bWhite, bEraseAll, bSend;
    private ImageButton bUndo, bRedo;
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
    private int radius = (500+playerLevel*10)/ratio;

    private MyCanvas canvas;

    /** Elements du dessin */
    private ArrayList<ArrayList<IGeoPoint>> geoDrawingLines = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> linesInfos =new ArrayList<>();

    /** Elements du serveur */
    private Socket mSocket;

    private Emitter.Listener onNewMessage;

    public drawingView(MainActivity mainactivity, Context cont, Socket socket, IMyLocationConsumer consumer){
        MA=mainactivity;
        context=cont;
        mSocket=socket;
        locationConsumer=consumer;
        System.out.println("bPlay");
        MA.setContentView(R.layout.activity_drawing);
        MA.backAvailable(true);
        Objects.requireNonNull(MA.getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

    /*OnBackPressedCallback callback = new OnBackPressedCallback(true // enabled by default) {
        @Override
        public void handleOnBackPressed() {
            setContentView(R.layout.activity_main);
        }
    };*/
        //requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

        mMapView = MA.findViewById(R.id.map);
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
        mLocationProvider.startLocationProvider(locationConsumer);
        mLocationProvider.setLocationUpdateMinDistance(10);
        mLocationProvider.setLocationUpdateMinTime(5000);
        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(context), mMapView);
        mLocationOverlay.enableMyLocation();
        mLocationOverlay.enableFollowLocation();
        mMapView.getOverlays().add(mLocationOverlay);
        mMapView.setMaxZoomLevel(20.0);
        mMapView.setMinZoomLevel(15.0);

        onNewMessage = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                MA.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = (JSONObject) args[0];
                        String player;
                        try {
                            player = data.getString("player");
                        } catch (JSONException e) {
                            System.out.println("samarchepa");
                            return;
                        }

                        System.out.println("heho"+player);
                    }
                });
            }
        };
        mSocket.on("drawings_loaded", onNewMessage);
        mSocket.on("drawings_updated", onNewMessage);
        //Instanciation du service de localisation

        LinearLayout linear =MA.findViewById(R.id.layoutCanvas);
        canvas = new MyCanvas(context, MA,this);

        linear.addView(canvas);
        iCircle =MA.findViewById(R.id.imageCircle);
        updateRatio();
        System.out.println("height"+ iCircle.getHeight());
        sPremium =MA.findViewById(R.id.switchPremium);
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
        sRed =MA.findViewById(R.id.seekRed);
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
        sGreen =MA.findViewById(R.id.seekGreen);
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
        sBlue =MA.findViewById(R.id.seekBlue);
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
        sThickness =MA.findViewById(R.id.seekThickness);
        sThickness.setMax(100+playerLevel*5);
        sThickness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tThickness.setText(MA.getString(R.string.width, String.format(Locale.getDefault(),"%d", sThickness.getProgress())));
                canvas.setThickness(sThickness.getProgress());
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
            sThickness.getProgressDrawable().setColorFilter(new BlendModeColorFilter(Color.BLACK, BlendMode.SRC_ATOP));
            sThickness.getThumb().setColorFilter(new BlendModeColorFilter(Color.BLACK, BlendMode.SRC_ATOP));
        } else {
            sRed.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
            sRed.getThumb().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
            sGreen.getProgressDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
            sGreen.getThumb().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
            sBlue.getProgressDrawable().setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN);
            sBlue.getThumb().setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN);
            sThickness.getProgressDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
            sThickness.getThumb().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
        }
        tZoom =MA.findViewById(R.id.textZoom);
        tColor =MA.findViewById(R.id.textColor);
        tThickness =MA.findViewById(R.id.textThickness);
        bPlus =MA.findViewById(R.id.buttonPlus);
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
        bMinus =MA.findViewById(R.id.buttonMinus);
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
        bSend =MA.findViewById(R.id.buttonSend);
        bSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapDrawing drawing = new MapDrawing(canvas.getMapDrawingLines(), playerName, premium);
                mSocket.emit("new_drawing", drawing.toJSON());
                canvas.eraseAll();
            }
        });
        bUndo =MA.findViewById(R.id.buttonUndo);
        bUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!canvas.undo()){
                    ActivateZoomButton(true);
                }
            }
        });

        bRedo =MA.findViewById(R.id.buttonRedo);
        bRedo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canvas.redo()) {
                    ActivateZoomButton(false);
                }
            }
        });
        bEraseAll =MA.findViewById(R.id.buttonEraseAll);
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
        bBlue =MA.findViewById(R.id.buttonBlue);
        bBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonColor(Color.BLUE);
            }
        });
        bRed =MA.findViewById(R.id.buttonRed);
        bRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonColor(Color.RED);
            }
        });
        bGreen =MA.findViewById(R.id.buttonGreen);
        bGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonColor(Color.GREEN);
            }
        });
        bCyan =MA.findViewById(R.id.buttonCyan);
        bCyan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonColor(Color.CYAN);
            }
        });
        bMagenta =MA.findViewById(R.id.buttonMagenta);
        bMagenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonColor(Color.MAGENTA);
            }
        });
        bYellow =MA.findViewById(R.id.buttonYellow);
        bYellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonColor(Color.YELLOW);
            }
        });
        bBlack =MA.findViewById(R.id.buttonBlack);
        bBlack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonColor(Color.BLACK);
            }
        });
        bWhite =MA.findViewById(R.id.buttonWhite);
        bWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonColor(Color.WHITE);
            }
        });

        buttonColor(Color.BLACK);
    }
    public int getPlayerLevel(){
        return playerLevel;
    }

    public void updateRatio(){
        int i=20-zoom;
        ratio=(int)Math.pow(2, i);
        iCircle.getLayoutParams().height = radius /ratio*2;
        iCircle.getLayoutParams().width = radius /ratio*2;
        canvas.ratioChange(ratio);
        System.out.println("ratio : " + ratio);
    }

    public void displayUndoRedo(boolean boolUndo, boolean boolRedo){
        if (boolUndo) bUndo.setAlpha((float)1);
        else bUndo.setAlpha((float)0.2);
        if (boolRedo) bRedo.setAlpha((float)1);
        else bRedo.setAlpha((float)0.2);
    }

    public int getRadius(){
        return radius;
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
        sThickness.setEnabled(bool);
    }

    public MapView getMap(){
        return mMapView;
    }

    public void changeColor(int col){
        tColor.setBackgroundColor(col);
        tColor.setText(Integer.toHexString(col));
        tColor.setTextColor(Color.rgb(255-Color.red(col),255-Color.green(col),255-Color.blue(col)));
        canvas.setColor(col);
    }
    public void buttonColor(int col){
        sBlue.setProgress(Color.blue(col));
        sGreen.setProgress(Color.green(col));
        sRed.setProgress(Color.red(col));
        changeColor(col);
    }
}
