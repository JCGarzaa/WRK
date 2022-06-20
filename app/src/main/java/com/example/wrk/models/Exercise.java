package com.example.wrk.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Exercise")
public class Exercise extends ParseObject {
    public static final String KEY_NAME = "name";
    public static final String KEY_BODYPART = "bodyPart";

    public String getName() { return getString(KEY_NAME); }
    public void setName(String name) { put(KEY_NAME, name); }

    public String getBodyPart() { return getString(KEY_BODYPART); }
    public void setBodyPart(String bodyPart) { put(KEY_BODYPART, bodyPart); }
}
