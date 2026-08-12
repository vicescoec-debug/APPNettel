package com.nettel.maritimo.next.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApiResult {
    public int action = 2;
    public String message = "Respuesta inválida";
    public String token = "";
    public final List<Map<String, String>> primary = new ArrayList<>();
    public final List<Map<String, String>> secondary = new ArrayList<>();
    public String scalar = "";

    public boolean success() {
        return action == 0;
    }
}
