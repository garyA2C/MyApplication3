package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.RequiresApi;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.Projection;

import java.util.ArrayList;

public class MyCanvas extends View {
    private Paint paint;
    private Path path;
    private ArrayList<Path> paths = new ArrayList<>();
    private ArrayList<Paint> paints = new ArrayList<>();
    private ArrayList<Path> undonePaths = new ArrayList<>();
    private ArrayList<Paint> undonepaints = new ArrayList<>();
    private Bitmap bitmap;
    private Canvas moncanvas;
    private int c=0x000000;
    private int ep=30;
    private int x;
    private int y;
    private int rayon;
    private int ratio=1;
    private ArrayList<ArrayList<Point>> listeligne = new ArrayList<>();
    private ArrayList<ArrayList<Point>> undoneligne = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> ligneinfo = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> undoneligneinfo = new ArrayList<>();
    private ArrayList<Point> listepoint = new ArrayList<>();
    private MainActivity MA;

    public MyCanvas(Context context, MainActivity mainact) {
        super(context);
        MA=mainact;
        paint = new Paint();
        path = new Path();
        setupaint(paint);
    }

    public void setupaint (Paint p){
        p.setAntiAlias(true);
        p.setColor(c);
        p.setStrokeJoin(Paint.Join.ROUND);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth(ep/ratio);
        System.out.println("paint setuped");
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void ratiochanged(int r){
        ratio=r;
        paint.setStrokeWidth(ep/ratio);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void effacertout(){
        moncanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
        paths.clear();
        undonePaths.clear();
        paints.clear();
        undonepaints.clear();
        listeligne.clear();
        undoneligne.clear();
        ligneinfo.clear();
        undoneligneinfo.clear();
        listepoint.clear();
        MA.activerzoom(true);
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        moncanvas = new Canvas(bitmap);
        x=moncanvas.getWidth();
        y=moncanvas.getHeight();
        //rayon=(int)(Integer.min(x,y)*0.45)/ratio;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            rayon=(500+MA.getPlayerLevel()*10)/ratio;
        }
    }

    public int getRayon(){
        return rayon;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        System.out.println("dessine");
        for (int i=0; i<paths.size();i++){
            canvas.drawBitmap(bitmap, 0,0,paints.get(i));
            canvas.drawPath(paths.get(i), paints.get(i));
        }
        canvas.drawBitmap(bitmap, 0, 0, paint);
        canvas.drawPath(path,paint);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float xPos = event.getX();
        float yPos = event.getY();
        System.out.println("x="+ xPos +" ; y="+ yPos + "ratio " + ratio);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!estdanslerayon(xPos,yPos)){
                    return false;
                }else{
                    path.reset();
                    undonePaths.clear();
                    undoneligne.clear();
                    undonepaints.clear();
                    path.moveTo(xPos, yPos);
                    listepoint.add(new Point((int)xPos,(int)yPos));
                    MA.activerzoom(false);
                    MA.activerbouton(false);
                    return true;
                }
            case MotionEvent.ACTION_MOVE:
                if (estdanslerayon(xPos,yPos)) {
                    path.lineTo(xPos, yPos);
                    listepoint.add(new Point((int)xPos,(int)yPos));
                } else {
                    int xproche = (int)((x / 2) + (xPos-x/2)/proportionrayon(xPos,yPos));
                    int yproche = (int)((y / 2) + (yPos-y/2)/proportionrayon(xPos,yPos));
                    path.lineTo(xproche,yproche);
                    listepoint.add(new Point(xproche,yproche));
                }
                break;
            case MotionEvent.ACTION_UP:
                moncanvas.drawPath(path,paint);
                paths.add(path);
                paints.add(paint);
                listeligne.add(listepoint);
                ligneinfo.add(new ArrayList<Integer>() {{add(c) ; add(ep);}});
                listepoint= new ArrayList<>();
                path = new Path();
                paint=new Paint();
                setupaint(paint);
                MA.activerbouton(true);
                break;
            default :
                return false;
        }
        invalidate();
        return true;
    }
    public boolean peutannuler(){
        return paths.size()>0;
    }

    public void annuler(){
        if (peutannuler()) {
            moncanvas.drawColor(0, PorterDuff.Mode.CLEAR);
            undonePaths.add(paths.remove(paths.size() - 1));
            undonepaints.add(paints.remove(paints.size() - 1));
            undoneligne.add(listeligne.remove(listeligne.size()-1));
            undoneligneinfo.add(ligneinfo.remove(ligneinfo.size()-1));
            invalidate();
            System.out.println("annuler");
        }
    }

    public boolean peutrefaire(){
        return undonePaths.size()>0;
    }

    public void refaire(){
        if (peutrefaire()){
            moncanvas.drawColor(0, PorterDuff.Mode.CLEAR);
            paths.add(undonePaths.remove(undonePaths.size()-1));
            paints.add(undonepaints.remove(undonepaints.size() - 1));
            listeligne.add(undoneligne.remove(undoneligne.size()-1));
            ligneinfo.add(undoneligneinfo.remove(undoneligneinfo.size()-1));
            invalidate();
            System.out.println("refaire");
        }
    }

    public boolean estdanslerayon(float px, float py){
        return Math.sqrt(Math.pow(px-x/2,2)+Math.pow(py-y/2,2))<=rayon/ratio-ep/(2*ratio);
    }

    public double proportionrayon(float px, float py){
        return Math.sqrt(Math.pow(px-x/2,2)+Math.pow(py-y/2,2))/(rayon/ratio-ep/(2*ratio));
    }

    public void couleur(int newc){
        invalidate();
        c = newc;
        paint.setColor(c);
    }

    public void epaisseur(int newep){
        invalidate();
        ep=newep;
        paint.setStrokeWidth(ep/ratio);
    }

    public ArrayList<ArrayList<Integer>> getInfoLigne(){
        return ligneinfo;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public ArrayList<ArrayList<IGeoPoint>> getGeoPoints(){
        Projection proj = MA.getMap().getProjection();
        ArrayList<ArrayList<IGeoPoint>> retour = new ArrayList<>();
        for (ArrayList<Point> a : listeligne){
            ArrayList<IGeoPoint> listegeopoint = new ArrayList<>();
            for (Point p : a){
                IGeoPoint geop=proj.fromPixels(p.x,p.y);
                listegeopoint.add(geop);
            }
            retour.add(listegeopoint);
        }
        return retour;
    }
}
