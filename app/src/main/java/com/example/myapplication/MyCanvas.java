package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.view.MotionEvent;
import android.view.View;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.Projection;

import java.util.ArrayList;

@SuppressLint("ViewConstructor")
public class MyCanvas extends View {
    private Paint paint;
    private Path path;
    private ArrayList<Path> paths = new ArrayList<>();
    private ArrayList<Paint> paints = new ArrayList<>();
    private ArrayList<Path> undonePaths = new ArrayList<>();
    private ArrayList<Paint> undonePaints = new ArrayList<>();
    private Bitmap bitmap;
    private Canvas myCanvas;
    private int color =0x000000;
    private int thickness =30;
    private int x;
    private int y;
    private int radius;
    private int ratio=1;
    private ArrayList<ArrayList<Point>> listLine = new ArrayList<>();
    private ArrayList<ArrayList<Point>> undoneLine = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> lineInfo = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> undoneLineInfo = new ArrayList<>();
    private ArrayList<Point> listPoint = new ArrayList<>();
    private MainActivity MA;

    public MyCanvas(Context context, MainActivity mainact) {
        super(context);
        MA=mainact;
        paint = new Paint();
        path = new Path();
        setupPaint(paint);
    }

    public void setupPaint(Paint p){
        p.setAntiAlias(true);
        p.setColor(color);
        p.setStrokeJoin(Paint.Join.ROUND);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth((float) thickness /ratio);
        System.out.println("paint setup");
    }

    public void ratioChange(int r){
        ratio=r;
        paint.setStrokeWidth((float) thickness /ratio);
    }

    public void eraseAll(){
        myCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
        paths.clear();
        undonePaths.clear();
        paints.clear();
        undonePaints.clear();
        listLine.clear();
        undoneLine.clear();
        lineInfo.clear();
        undoneLineInfo.clear();
        listPoint.clear();
        MA.displayUndoRedo(false, false);
        MA.ActivateZoomButton(true);
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        myCanvas = new Canvas(bitmap);
        x= myCanvas.getWidth();
        y= myCanvas.getHeight();
        //rayon=(int)(Integer.min(x,y)*0.45)/ratio;
        radius = MA.getRadius();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        System.out.println("drawing");
        for (int i=0; i<paths.size();i++){
            canvas.drawBitmap(bitmap, 0,0,paints.get(i));
            canvas.drawPath(paths.get(i), paints.get(i));
        }
        canvas.drawBitmap(bitmap, 0, 0, paint);
        canvas.drawPath(path,paint);

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float xPos = event.getX();
        float yPos = event.getY();
        System.out.println("x="+ xPos +" ; y="+ yPos + "ratio " + ratio);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!isInRadius(xPos,yPos)){
                    return false;
                }else{
                    path.reset();
                    undonePaths.clear();
                    undoneLine.clear();
                    undonePaints.clear();
                    path.moveTo(xPos, yPos);
                    listPoint.add(new Point((int)xPos,(int)yPos));
                    MA.ActivateZoomButton(false);
                    MA.ActivateOtherButton(false);
                    return true;
                }
            case MotionEvent.ACTION_MOVE:
                if (isInRadius(xPos,yPos)) {
                    path.lineTo(xPos, yPos);
                    listPoint.add(new Point((int)xPos,(int)yPos));
                } else {
                    int closeX = (int)((x / 2) + (xPos-x/2)/ proportionRadius(xPos,yPos));
                    int closeY = (int)((y / 2) + (yPos-y/2)/ proportionRadius(xPos,yPos));
                    path.lineTo(closeX,closeY);
                    listPoint.add(new Point(closeX,closeY));
                }
                break;
            case MotionEvent.ACTION_UP:
                myCanvas.drawPath(path,paint);
                paths.add(path);
                paints.add(paint);
                listLine.add(listPoint);
                lineInfo.add(new ArrayList<Integer>() {{add(paint.getColor()) ; add(thickness);}});
                listPoint = new ArrayList<>();
                path = new Path();
                paint=new Paint();
                setupPaint(paint);
                MA.displayUndoRedo(canUndo(),canRedo());
                MA.ActivateOtherButton(true);
                break;
            default :
                return false;
        }
        invalidate();
        return true;
    }
    public boolean canUndo(){
        return paths.size()>0;
    }

    public boolean undo(){
        if (canUndo()) {
            myCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
            undonePaths.add(paths.remove(paths.size() - 1));
            undonePaints.add(paints.remove(paints.size() - 1));
            undoneLine.add(listLine.remove(listLine.size()-1));
            undoneLineInfo.add(lineInfo.remove(lineInfo.size()-1));
            invalidate();
            System.out.println("undo");
        }
        MA.displayUndoRedo(canUndo(),canRedo());
        return canUndo();
    }

    public boolean canRedo(){
        return undonePaths.size()>0;
    }

    public boolean redo(){
        if (canRedo()){
            myCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
            paths.add(undonePaths.remove(undonePaths.size()-1));
            paints.add(undonePaints.remove(undonePaints.size() - 1));
            listLine.add(undoneLine.remove(undoneLine.size()-1));
            lineInfo.add(undoneLineInfo.remove(undoneLineInfo.size()-1));
            invalidate();
            System.out.println("redo");
        }
        MA.displayUndoRedo(canUndo(),canRedo());
        return canUndo();
    }

    public boolean isInRadius(float px, float py){
        return Math.sqrt(Math.pow(px-(float)x/2,2)+Math.pow(py-(float)y/2,2))<=(float) radius /ratio-(float) thickness /(2*ratio);
    }

    public double proportionRadius(float px, float py){
        return Math.sqrt(Math.pow(px-(float)x/2,2)+Math.pow(py-(float)y/2,2))/((float) radius /ratio-(float) thickness /(2*ratio));
    }

    public void setColor(int newC){
        invalidate();
        color = newC;
        paint.setColor(color);
    }

    public void setThickness(int newT){
        invalidate();
        thickness =newT;
        paint.setStrokeWidth((float)thickness/ratio);
    }

    /*private ArrayList<ArrayList<Integer>> getInfoLigne(){
        return lineInfo;
    }*/

    private ArrayList<ArrayList<IGeoPoint>> getGeoPoints(){
        Projection pro = MA.getMap().getProjection();
        ArrayList<ArrayList<IGeoPoint>> r = new ArrayList<>();
        for (ArrayList<Point> a : listLine){
            ArrayList<IGeoPoint> listGeoPoint = new ArrayList<>();
            for (Point p : a){
                IGeoPoint geoP=pro.fromPixels(p.x,p.y);
                listGeoPoint.add(geoP);
            }
            r.add(listGeoPoint);
        }
        return r;
    }

    public ArrayList<MapDrawingLine> getMapDrawingLines() {
        ArrayList<MapDrawingLine> mdl = new ArrayList<>();
        ArrayList<ArrayList<IGeoPoint>> geo = getGeoPoints();
        for (int i = 0; i < geo.size(); i++) {
            mdl.add(new MapDrawingLine(geo.get(i), lineInfo.get(i).get(0), lineInfo.get(i).get(1)));
        }
        return  mdl;
    }
}
