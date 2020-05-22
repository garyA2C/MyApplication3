package com.example.myapplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IGeoPoint;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MapDrawing {
    private ArrayList<MapDrawingLine> lines;
    private String playerName;
    private boolean premium;
    private String datetime;
    private double usedPaint;

    public MapDrawing(ArrayList<MapDrawingLine> lines, String playerName, boolean premium) {
        this.lines = lines;
        this.usedPaint=0;
        for (MapDrawingLine l : lines){
            usedPaint+=(l.getThickness()*l.getLineLength());
        }
        usedPaint/=50000;
        usedPaint=round(usedPaint,2);
        System.out.println(usedPaint);
        this.playerName = playerName;
        this.premium = premium;
        Date now = new Date();
        DateFormat datetimeform = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        this.datetime = datetimeform.format(now);
    }

    public ArrayList<MapDrawingLine> getLines() {
        return lines;
    }

    public String getPlayerName() {
        return playerName;
    }

    public boolean isPremium() {
        return premium;
    }

    public String getDatetime() {
        return datetime;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        JSONArray linesArray = new JSONArray();
        try {
            json.put("player", playerName);
            json.put("premium", premium);
            json.put("datetime", datetime);
            for (MapDrawingLine line : lines) {
                linesArray.put(line.toJSON());
            }
            json.put("lines", linesArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;

    }
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
