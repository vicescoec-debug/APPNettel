package com.nettel.maritimo.next.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BaseActivity extends AppCompatActivity {
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    interface Task<T> {
        T run() throws Exception;
    }

    interface Done<T> {
        void run(T value);
    }

    protected <T> void async(Task<T> task, Done<T> done) {
        async(task, done, e -> Ui.toast(this, cleanMessage(e), Toast.LENGTH_LONG));
    }

    protected <T> void async(Task<T> task, Done<T> done, Done<Exception> error) {
        io.execute(() -> {
            try {
                T value = task.run();
                runOnUiThread(() -> done.run(value));
            } catch (Exception e) {
                runOnUiThread(() -> error.run(e));
            }
        });
    }

    protected String cleanMessage(Exception e) {
        String message = e.getMessage();
        if (message == null || message.trim().isEmpty()) return "Error de comunicación";
        message = extractRestMessage(message);
        String lower = message.toLowerCase(Locale.US);

        if (lower.contains("usuario") && lower.contains("contrase")) {
            return "Usuario o contraseña incorrectos.";
        }
        if (message.contains("REST HTTP 401")) {
            return "Usuario o contraseña incorrectos.";
        }
        if (message.contains("REST HTTP 404") || lower.contains("endpoint no")) {
            return "Servicio no disponible en el servidor. Solicite habilitar el endpoint correspondiente.";
        }
        if (message.contains("REST HTTP 400") && lower.contains("campo requerido")) {
            if (lower.contains("customer_id")) return "Ingrese la identificación del cliente.";
            if (lower.contains("username") || lower.contains("usuario")) return "Ingrese el usuario.";
            return "Complete los campos requeridos.";
        }
        if (message.length() > 180) return message.substring(0, 180) + "...";
        return message;
    }

    private String extractRestMessage(String message) {
        int jsonStart = message.indexOf('{');
        if (jsonStart < 0) return message;
        try {
            JSONObject json = new JSONObject(message.substring(jsonStart));
            String clean = json.optString("message", json.optString("mensaje", ""));
            if (!clean.trim().isEmpty()) return clean.trim();
        } catch (Exception ignored) {
        }
        return message;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        io.shutdownNow();
    }
}
