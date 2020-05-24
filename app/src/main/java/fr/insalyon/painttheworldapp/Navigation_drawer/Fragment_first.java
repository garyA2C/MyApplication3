package fr.insalyon.painttheworldapp.Navigation_drawer;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.multidex.BuildConfig;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import fr.insalyon.painttheworldapp.R;
import fr.insalyon.painttheworldapp.util.MyView;

public class Fragment_first extends Fragment implements IMyLocationConsumer, View.OnTouchListener {

    /**Elements généraux */
    private Context context;
    private Fragment_first MA;
    private String playerName = "RootUser42";
    private int playerLevel= 420;
    private boolean premium=false;
    File ext = Environment.getExternalStorageDirectory();
    private Location loc;


    /**Elements d'interface */
    private Button bBlue, bRed, bPlus, bMinus, bGreen, bCyan, bMagenta, bYellow, bBlack, bWhite, bEraseAll, bSend, bHideRadius, bOverlayPremium;
    private ImageButton bUndo, bRedo;
    private SeekBar sRed, sGreen, sBlue, sThickness;
    private Switch sPremium;
    private TextView tColor, tThickness, tZoom, tOverlay;


    /**Elements géographiques*/
    private MapView mMapView;
    private ImageView iCircle;
    private MyLocationNewOverlay mLocationOverlay;
    private ArrayList<Paint> mapPaints = new ArrayList<>();
    private GpsMyLocationProvider mLocationProvider;
    private IMyLocationConsumer locationConsumer;
    private int zoom=20;
    private int numberMapDrawings=0;
    private double premiumVolume=0.0, regularVolume=1.0, volume=0.0;

    private int ratio=1;
    private int radius = (500+playerLevel*10)/ratio;

    private boolean backAvailable =false;
    private MyCanvas canvas;

    /** Elements du serveur */
    private Socket mSocket;
    public static final String SERVER_URL = "https://paint.antoine-rcbs.ovh:443";

    /** Elements du screenshot */
    private int x;
    private int y;
    private int m;
    private int n;
    private int width;
    private int height;
    private Bitmap bitmap;
    private MyView myView;
    private ProgressBar pBar;
    private ArrayList<ArrayList<GeoPoint>> mapGeoPoints = new ArrayList<>();
    private ArrayList<Float> mapStrokes = new ArrayList<>();
    private ArrayList<Polyline> mapPolylines = new ArrayList<>();
    private ArrayList<Boolean> mapPremiums = new ArrayList<>();
    private ArrayList<Overlay> overlayPremium = new ArrayList<>();
    private ArrayList<Overlay> overlayRegular = new ArrayList<>();
    private int nbRegular=0;
    private int nbPremium=0;
    private int alphaRegular=255;
    private MapDrawing drawing;
    private String string ="";
    private JSONObject boundsJSON= new JSONObject();
    private JSONObject northEastJSON= new JSONObject();
    private JSONObject southWestJSON= new JSONObject();
    private Emitter.Listener onNewMessage;



    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_first, container, false);
        myView = new MyView(getContext());
        MA = this;
        context = getContext();
        locationConsumer = this;
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

        try {
            mSocket = IO.socket(SERVER_URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        mSocket.connect();
        //Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(backAvailable);
        backAvailable(true);

        mMapView = root.findViewById(R.id.map);
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
        mLocationProvider = new GpsMyLocationProvider(context);
        mLocationProvider.startLocationProvider(locationConsumer);
        mLocationProvider.setLocationUpdateMinDistance(10);
        mLocationProvider.setLocationUpdateMinTime(5000);
        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(context), mMapView);
        mLocationOverlay.enableMyLocation();
        mLocationOverlay.enableFollowLocation();
        mapController.setCenter(getLocation());
        mMapView.getOverlays().add(mLocationOverlay);
        mMapView.setMaxZoomLevel(20.0);
        mMapView.setMinZoomLevel(16.0);



        /*ArrayList<GeoPoint> geop=new ArrayList<>();
        geop.add(new GeoPoint(45.7727561260112,4.87215628941745));
        geop.add(new GeoPoint(45.786752, 4.875813));
        addPolyline(geop,Color.RED,(float)32,true);
        geop.clear();
        geop.add(new GeoPoint(45.7726561261112,4.87205628942745));
        geop.add(new GeoPoint(45.786652, 4.875713));
        addPolyline(geop,Color.BLUE,(float)32,true);
        geop.clear();

        geop.add(new GeoPoint(45.780588720361955,4.876188933849335));
        geop.add(new GeoPoint(45.78175782927855,4.877481758594513));
        addPolyline(geop,Color.BLUE,(float)24,true);
        geop.clear();
        geop.add(new GeoPoint(45.781789628698604,4.877082109451294));
        geop.add(new GeoPoint(45.780512025960185,4.87730473279953));
        addPolyline(geop,Color.YELLOW,(float)18,false);
        geop.clear();
        geop.add(new GeoPoint(45.78083750879273,4.87592875957489));
        geop.add(new GeoPoint(45.78100773181626,4.878007471561432));
        addPolyline(geop,Color.GREEN,(float)14,false);*/


        /*mMapView.getOverlayManager().addAll(1,overlayPremium);
        mMapView.getOverlayManager().addAll(0,overlayRegular);
        mMapView.getOverlayManager().add(mapPolylines.get(3));
        mMapView.getOverlayManager().set(0,mapPolylines.get(3));*/

        /*
        if (mapPremiums.get(3)){
            mMapView.getOverlayManager().removeAll(overlayPremium);
            overlayPremium.add(mapPolylines.get(3));
            mMapView.getOverlayManager().addAll(1,overlayPremium);
        }else{
            mMapView.getOverlayManager().removeAll(overlayRegular);
            overlayRegular.add(mapPolylines.get(3));
            mMapView.getOverlayManager().addAll(0,overlayRegular);
        }*/


        //mMapView.getOverlayManager().removeAll(overlayPremium);
        //mMapView.getOverlayManager().removeAll(overlayRegular);
        //mMapView.getOverlayManager().clear();
        //mMapView.getOverlayManager().addAll(1,overlayPremium);
        //mMapView.getOverlayManager().addAll(0,overlayRegular);



        //mMapView.getOverlayManager().add(0,mapPolylines.get(0));
        //mMapView.getOverlayManager().add(1,mapPolylines.get(1));
        //mMapView.getOverlayManager().add(0,mapPolylines.get(2));

        onNewMessage = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = (JSONObject) args[0];
                        if (data.has("new_val")){
                            try {
                                data = data.getJSONObject("new_val");
                            } catch (JSONException e) {
                                e.printStackTrace();
                                System.out.println("new val samarchepa");
                            }
                        }
                        String player;
                        try {
                            JSONArray lines;
                            lines = data.getJSONArray("lines");
                            boolean newpre = data.getBoolean("premium");
                            System.out.println(newpre);
                            for (int i=0;i<lines.length(); i++){
                                int newcol = lines.getJSONObject(i).getInt("color");
                                int newthi = lines.getJSONObject(i).getInt("thickness");
                                JSONArray newjsonloc = lines.getJSONObject(i).getJSONArray("location");
                                ArrayList<GeoPoint> newloc = new ArrayList<>();
                                for (int j=0;j<newjsonloc.length();j++){
                                    GeoPoint newGP = new GeoPoint(newjsonloc.getJSONArray(j).getDouble(0),newjsonloc.getJSONArray(j).getDouble(1));
                                    newloc.add(newGP);
                                }
                                addPolyline(newloc,newcol,newthi,newpre);
                            }
                        } catch (JSONException e) {
                            System.out.println("lines samarchepa");
                            return;
                        }
                        System.out.println("heho");
                    }
                });
            }
        };

        mSocket.on("drawings_loaded", onNewMessage);
        mSocket.on("drawings_updated", onNewMessage);
        //Instanciation du service de localisation

        LinearLayout linear =root.findViewById(R.id.layoutCanvas);
        canvas = new MyCanvas(context, this);

        pBar=root.findViewById(R.id.progressBar);

        linear.addView(canvas);
        iCircle =root.findViewById(R.id.imageCircle);
        updateRatio();
        sPremium =root.findViewById(R.id.switchPremium);
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
        sRed =root.findViewById(R.id.seekRed);
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
        sGreen =root.findViewById(R.id.seekGreen);
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
        sBlue =root.findViewById(R.id.seekBlue);
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
        sThickness =root.findViewById(R.id.seekThickness);
        sThickness.setMax(250);
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
        tZoom =root.findViewById(R.id.textZoom);
        tColor =root.findViewById(R.id.textColor);
        tThickness =root.findViewById(R.id.textThickness);
        tOverlay=root.findViewById(R.id.textOverlay);
        bPlus =root.findViewById(R.id.buttonPlus);
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
        bMinus =root.findViewById(R.id.buttonMinus);
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
        bSend =root.findViewById(R.id.buttonSend);
        bSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawing = new MapDrawing(canvas.getMapDrawingLines(), playerName, premium);
                if (premium){
                    volume=premiumVolume;
                    string="premium";
                }else{
                    volume=regularVolume;
                    string="regular";
                }
                if (drawing.getUsedPaint() > volume) {
                    AlertDialog.Builder newDialog = new AlertDialog.Builder(context);
                    newDialog.setMessage("Vous ne pouvez pas envoyer ce trait car ne possédez pas assez de peinture. Effacer ?");
                    newDialog.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
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
                } else {
                    AlertDialog.Builder newDialog = new AlertDialog.Builder(context);
                    newDialog.setMessage("Envoyer ce dessin ? Il vous coutera " + drawing.getUsedPaint() + "L de peinture " + string);
                    newDialog.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            mSocket.emit("new_drawing", drawing.toJSON());
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
            }
        });
        bUndo =root.findViewById(R.id.buttonUndo);
        bUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!canvas.undo()){
                    ActivateZoomButton(true);
                }
            }
        });

        bRedo =root.findViewById(R.id.buttonRedo);
        bRedo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canvas.redo()) {
                    ActivateZoomButton(false);
                }
            }
        });
        bEraseAll =root.findViewById(R.id.buttonEraseAll);
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
        bBlue =root.findViewById(R.id.buttonBlue);
        bBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonColor(Color.BLUE);
            }
        });
        bRed =root.findViewById(R.id.buttonRed);
        bRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonColor(Color.RED);
            }
        });
        bGreen =root.findViewById(R.id.buttonGreen);
        bGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonColor(Color.GREEN);
            }
        });
        bCyan =root.findViewById(R.id.buttonCyan);
        bCyan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonColor(Color.CYAN);
            }
        });
        bMagenta =root.findViewById(R.id.buttonMagenta);
        bMagenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonColor(Color.MAGENTA);
            }
        });
        bYellow =root.findViewById(R.id.buttonYellow);
        bYellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonColor(Color.YELLOW);
            }
        });
        bBlack =root.findViewById(R.id.buttonBlack);
        bBlack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonColor(Color.BLACK);
            }
        });
        bWhite =root.findViewById(R.id.buttonWhite);
        bWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonColor(Color.WHITE);
            }
        });
        bHideRadius=root.findViewById(R.id.buttonHideRadius);
        bHideRadius.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bHideRadius.getText()=="Afficher Rayon"){
                    bHideRadius.setText("Cacher Rayon");
                    iCircle.setAlpha((float) 0.3);
                }else{
                    bHideRadius.setText("Afficher Rayon");
                    iCircle.setAlpha((float) 0);
                }
            }
        });
        bOverlayPremium=root.findViewById(R.id.buttonOverlayPremium);
        bOverlayPremium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bOverlayPremium.getText()=="Montrer Tout"){
                    alphaRegular=255;
                    for (int j=0;j<numberMapDrawings;j++){
                        if (!mapPremiums.get(j)){
                            mapPaints.get(j).setAlpha(alphaRegular);
                        }
                    }
                    bOverlayPremium.setText("Montrer Premium");
                    tOverlay.setText("Vue : tout les dessins");
                }else{
                    alphaRegular=0;
                    for (int j=0;j<numberMapDrawings;j++){
                        if (!mapPremiums.get(j)){
                            mapPaints.get(j).setAlpha(alphaRegular);
                        }
                    }
                    bOverlayPremium.setText("Montrer Tout");
                    tOverlay.setText("Vue : dessins premiums");
                }
                mMapView.invalidate();
            }
        });


        getActivity().addContentView(myView, new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT));

        return root;
    }



//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        if (backAvailable){
//            AlertDialog.Builder alertReturn = new AlertDialog.Builder(context);
//            alertReturn.setMessage("Retourner au menu principal ?");
//            alertReturn.setPositiveButton("Oui", new DialogInterface.OnClickListener(){
//                public void onClick(DialogInterface dialog, int which){
//                    recreate();
//                }
//            });
//            alertReturn.setNegativeButton("Non", new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.cancel();
//                }
//            });
//            alertReturn.show();
//            return true;
//        }else{
//            AlertDialog.Builder alertExit = new AlertDialog.Builder(context);
//            alertExit.setMessage("Quitter l'application?");
//            alertExit.setPositiveButton("Oui", new DialogInterface.OnClickListener(){
//                public void onClick(DialogInterface dialog, int which){
//                    finish();
//                }
//            });
//            alertExit.setNegativeButton("Non", new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.cancel();
//                }
//            });
//            alertExit.show();
//        }
//        return false;
//    }




//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == android.R.id.home){
//            AlertDialog.Builder newDialog = new AlertDialog.Builder(context);
//            newDialog.setMessage("Retourner au menu principal ?");
//            newDialog.setPositiveButton("Oui", new DialogInterface.OnClickListener(){
//                public void onClick(DialogInterface dialog, int which){
//                    root.recreate();
//                }
//            });
//            newDialog.setNegativeButton("Non", new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.cancel();
//                }
//            });
//            newDialog.show();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            x = 0;
            y = 0;
            width = 0;
            height = 0;
            x = (int) event.getX();
            y = (int) event.getY();

            Log.i(playerName, "onTouch: x = "+x +" y = "+y);
        }

        if(event.getAction() == MotionEvent.ACTION_MOVE){
            m = (int) event.getX();
            n = (int) event.getY();
            myView.setSeat(x,y,m,n);
            myView.postInvalidate();

            Log.i(playerName, "onTouch: x = "+x +" y = "+y +" m = "+m +" n = "+n);
        }

        if(event.getAction() == MotionEvent.ACTION_UP){
            if(event.getX()>x){
                width = (int)event.getX()-x;
            }else{
                width = (int)(x-event.getX());
                x = (int) event.getX();
            }
            if(event.getY()>y){
                height = (int) event.getY()-y;
            }else{
                height = (int)(y-event.getY());
                y = (int) event.getY();
            }

            Log.i(playerName, "onTouch: x = "+x +" y = "+y +" m = "+m +" n = "+n);


        }

        if(myView.isSign()){
            return false;
        }else{
            return true;
        }
    }




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
    public int getPlayerLevel(){
        return playerLevel;
    }

    public void updateRatio(){
        int i=20-zoom;
        ratio=(int)Math.pow(2, i);
        iCircle.getLayoutParams().height = radius /ratio*2;
        iCircle.getLayoutParams().width = radius /ratio*2;
        canvas.ratioChange(ratio);
        for (int j=0; j<numberMapDrawings;j++){
            mapPaints.get(j).setStrokeWidth(mapStrokes.get(j)/ratio);
        }
        //System.out.println("ratio : " + ratio);
        mMapView.invalidate();
    }

    public void addPolyline(ArrayList<GeoPoint> newgp, int newc, float news, boolean newp){
        mapGeoPoints.add(new ArrayList<GeoPoint>());
        mapGeoPoints.get(numberMapDrawings).addAll(newgp);
        mapPolylines.add(new Polyline());
        mapPolylines.get(numberMapDrawings).setPoints(mapGeoPoints.get(numberMapDrawings));
        mapPaints.add(mapPolylines.get(numberMapDrawings).getOutlinePaint());
        mapPaints.get(numberMapDrawings).setAntiAlias(true);
        mapPaints.get(numberMapDrawings).setColor(newc);
        mapPaints.get(numberMapDrawings).setStrokeJoin(Paint.Join.ROUND);
        mapPaints.get(numberMapDrawings).setStyle(Paint.Style.STROKE);
        mapPaints.get(numberMapDrawings).setStrokeCap(Paint.Cap.ROUND);
        mapStrokes.add(news);
        mapPaints.get(numberMapDrawings).setStrokeWidth(mapStrokes.get(numberMapDrawings)/ratio);
        mapPremiums.add(newp);
        System.out.print("printons");
        if (newp){
            //mMapView.getOverlayManager().removeAll(overlayPremium);
            overlayPremium.add(mapPolylines.get(numberMapDrawings));
            mapPaints.get(numberMapDrawings).setAlpha(255);
            mMapView.getOverlayManager().add(numberMapDrawings,mapPolylines.get(numberMapDrawings));
            System.out.println(" un premium");
            nbPremium+=1;
        }else{
            //mMapView.getOverlayManager().removeAll(overlayRegular);
            overlayRegular.add(mapPolylines.get(numberMapDrawings));
            mapPaints.get(numberMapDrawings).setAlpha(alphaRegular);
            mMapView.getOverlayManager().add(nbRegular,mapPolylines.get(numberMapDrawings));
            System.out.println(" un regular");
            nbRegular+=1;
        }
        numberMapDrawings+=1;
        mMapView.invalidate();
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
            locationChanged(loc);
            System.out.println("initial location : "+location.getLatitude()+" , "+location.getLongitude());
            setProgressBarOff();
        }else if (loc.distanceTo(location)>500){
            loc=location;
            locationChanged(loc);
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

    public boolean getPremium(){
        return premium;
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

    public void locationChanged(Location location){
        try {
            northEastJSON.put("lat",location.getLatitude()+0.02);
            northEastJSON.put("lng",location.getLongitude()+0.02);
            southWestJSON.put("lat",location.getLatitude()-0.02);
            southWestJSON.put("lng",location.getLongitude()-0.02);
            boundsJSON.put("_northEast",northEastJSON);
            boundsJSON.put("_southWest",southWestJSON);
            boundsJSON.put("zoom",20);
            System.out.println("json ok");
        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("json pas ok");
        }
        mSocket.emit("bounds_changed", boundsJSON);
    }

    public void setProgressBarOff(){
        pBar.setVisibility(View.GONE);
    }
}
