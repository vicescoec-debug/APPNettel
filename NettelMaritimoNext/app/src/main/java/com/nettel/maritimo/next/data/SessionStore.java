package com.nettel.maritimo.next.data;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public final class SessionStore {
    private final SharedPreferences prefs;
    public SessionStore(Context context) {
        try {
            MasterKey key=new MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build();
            prefs=EncryptedSharedPreferences.create(context,"nettel_secure",key,EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch(Exception e) { throw new IllegalStateException("No se pudo inicializar almacenamiento seguro",e); }
    }
    public void save(String user,String token){
        String previous = prefs.getString("user", "");
        SharedPreferences.Editor editor = prefs.edit().putString("user",user).putString("token",token);
        if (!previous.equalsIgnoreCase(user)) editor.remove("fleet_user").remove("fleet_user_owner");
        editor.apply();
    }
    public void updateToken(String token){if(token!=null&&!token.isEmpty())prefs.edit().putString("token",token).apply();}
    public void saveFleetUser(String user){prefs.edit().putString("fleet_user",user).putString("fleet_user_owner",user()).apply();}
    public String user(){return prefs.getString("user","");}
    public String token(){return prefs.getString("token","");}
    public String fleetUser(){
        String selected = prefs.getString("fleet_user","");
        if (selected.isEmpty()) return "";
        String owner = prefs.getString("fleet_user_owner","");
        if (owner.isEmpty()) return "";
        return owner.equalsIgnoreCase(user()) ? selected : "";
    }
    public boolean active(){return !user().isEmpty()&&!token().isEmpty();}
    public void clear(){prefs.edit().clear().apply();}
}
