package model;

import com.google.gson.Gson;

public record AuthData(String authToken, String username) {
    public String toJson() {
        return new Gson().toJson(this);
    }
}
