package com.example.gabriel.mapsstarter2.models;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by gabriel on 15/11/17.
 */

public class User {
    private String name, email;

    public User() {
    }

    public HashMap<String, Object> getCustomHashMap(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("email", email);
        return map;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User(String name, String email) {

        this.name = name;
        this.email = email;
    }
}
