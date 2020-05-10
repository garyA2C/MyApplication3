package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.os.Build;
import android.text.Layout;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import org.osmdroid.api.IMapView;
import org.osmdroid.shape.ShapeConverter;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayManager;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
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
    private Button bBlue, bRed, bPlus, bMinus, bGreen, bCyan, bMagenta, bYellow, bBlack, bWhite, bEraseAll, bSend, bHideRadius, bOverlayPremium;
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
    private ArrayList<ArrayList<GeoPoint>> mapGeoPoints = new ArrayList<>();
    private int numberMapDrawings=0;
    private ArrayList<Paint> mapPaints = new ArrayList<>();
    private ArrayList<Float> mapStrokes = new ArrayList<>();
    private ArrayList<Polyline> mapPolylines = new ArrayList<>();
    private ArrayList<Boolean> mapPremiums = new ArrayList<>();
    private ArrayList<Overlay> overlayPremium = new ArrayList<>();
    private ArrayList<Overlay> overlayRegular = new ArrayList<>();
    private int nbRegular=0;
    private int nbPremium=0;
    private int alphaRegular=255;

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
        /*List<Overlay> folder = ShapeConverter.convert(mMapView, new File(myshape));
        mMapView.getOverlayManager().addAll(folder);
        mMapView.invalidate();*/

        /*ArrayList<GeoPoint> geop=new ArrayList<>();
        geop.add(new GeoPoint(45.78169048927032,4.875955581665039));
        geop.add(new GeoPoint(45.78071779119017,4.877849221229553));
        addPolyline(geop,Color.RED,(float)32,true);
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
        //mMapView.getOverlayManager().overlaysReversed();
        //mMapView.getOverlayManager().
        //line.draw(mapCanvas, mMapView,false);

        /*line.setOnClickListener(new Polyline.OnClickListener() {
            @Override
            public boolean onClick(Polyline polyline, MapView mapView, GeoPoint eventPos) {
                Toast.makeText(mapView.getContext(), "polyline with " + polyline.getPoints().size() + "pts was tapped", Toast.LENGTH_LONG).show();
                return false;
            }
        });*/

        //create the first tilesOverlay
        /*final MapTileProviderBasic tileProvider = new MapTileProviderBasic(context);
        final TilesOverlay tilesOverlay = new TilesOverlay(tileProvider, context);
        tilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);

        //create the second one
        final MapTileProviderBasic anotherTileProvider = new MapTileProviderBasic(context);
        final TilesOverlay secondTilesOverlay = new TilesOverlay(anotherTileProvider, context);
        secondTilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);

        // add the first tilesOverlay to the list
        mMapView.getOverlays().add(tilesOverlay);

        // add the second tilesOverlay to the list
        mMapView.getOverlays().add(secondTilesOverlay);*/


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
        bHideRadius=MA.findViewById(R.id.buttonHideRadius);
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
        bOverlayPremium=MA.findViewById(R.id.buttonOverlayPremium);
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
                }else{
                    alphaRegular=0;
                    for (int j=0;j<numberMapDrawings;j++){
                        if (!mapPremiums.get(j)){
                            mapPaints.get(j).setAlpha(alphaRegular);
                        }
                    }
                    bOverlayPremium.setText("Montrer Tout");
                }
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
        for (int j=0; j<numberMapDrawings;j++){
            mapPaints.get(j).setStrokeWidth(mapStrokes.get(j)/ratio);
        }
        System.out.println("ratio : " + ratio);
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
        if (newp){
            //mMapView.getOverlayManager().removeAll(overlayPremium);
            overlayPremium.add(mapPolylines.get(numberMapDrawings));
            mMapView.getOverlayManager().add(numberMapDrawings,mapPolylines.get(numberMapDrawings));
            nbPremium+=1;
        }else{
            //mMapView.getOverlayManager().removeAll(overlayRegular);
            overlayRegular.add(mapPolylines.get(numberMapDrawings));
            mapPaints.get(numberMapDrawings).setAlpha(alphaRegular);
            mMapView.getOverlayManager().add(nbRegular,mapPolylines.get(numberMapDrawings));
            nbRegular+=1;
        }
        mMapView.invalidate();
        numberMapDrawings+=1;
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
}
