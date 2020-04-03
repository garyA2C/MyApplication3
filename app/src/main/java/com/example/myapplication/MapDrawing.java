package com.example.myapplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IGeoPoint;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MapDrawing {
    private ArrayList<MapDrawingLine> lines;
    private String playerName;
    private boolean premium;
    private String datetime;

    public MapDrawing(ArrayList<MapDrawingLine> lines, String playerName, boolean premium) {
        this.lines = lines;
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
}
