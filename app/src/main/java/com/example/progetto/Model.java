package com.example.progetto;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class Model {

    private static final Model ourInstance = new Model();
    public static Model getInstance() {
        return ourInstance;
    }

    private JSONObject id;
    private String image;                       // TO DO: save both in Room

    private ShownObject[] shownObjects;
    private User[] users;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
    public JSONObject getId() {
        return this.id;
    }

    public void setId(JSONObject id) {
        this.id = id;
    }

    public User[] getUsers() {
        return this.users;
    }

    public ShownObject[] getShownObjects() {
        return shownObjects;
    }

    public ShownObject getShownObjectById(int id) {
        for (int i = 0; i < shownObjects.length; i++) {
            if (shownObjects[i].getId() == id) {
                return shownObjects[i];
            }
        }
        return null;
    }

    /*
    public User getUserById(String id) {
        for (int i=0; i<20; i++) {
            if (users[i].getId().equals(id)) {
                return users[i];
            }
        }
        return null;
    }
    */

    public void refreshShownObjects(ShownObject[] shownObjects) {
        this.shownObjects = shownObjects;
    }

    public void refreshUsers(User[] users){
        this.users = users;
    }




    /*
    public boolean isDistanceCandyOk(int id) {
        Candy c = getCandyById(id);
        double x = Math.abs(this.longitude - c.getLon());
        double y = Math.abs(this.latitude - c.getLat());
        double value = Math.sqrt((x*x) + (y*y));
        if (value < 50) {
            return true;
        }
        return false;
    }

    public boolean isDistanceMonsterOk(int id) {
        Monster m = getMonsterById(id);
        double x = Math.abs(this.longitude - m.getLon());
        double y = Math.abs(this.latitude - m.getLat());
        double value = Math.sqrt((x*x) + (y*y));
        if (value < 50) {
            return true;
        }
        return false;
    }
    */

}
