package com.nettel.maritimo.next.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.nettel.maritimo.next.BuildConfig;
import com.nettel.maritimo.next.R;
import com.nettel.maritimo.next.data.NettelRepository;

public class SettingsActivity extends BaseActivity {
    private static final String TERMS_URL = "https://nettelcorp.com/privacidad.html";

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout root = Ui.page(this, "Configuración");
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.TOP);
        Ui.pad(content, 20);

        android.view.View password = Ui.button(this, "Cambiar contraseña");
        android.view.View about = Ui.button(this, "Acerca de");
        android.view.View logout = Ui.button(this, "Cerrar sesión");
        content.addView(password);
        content.addView(about);
        content.addView(logout);

        root.addView(content);
        setContentView(root);

        password.setOnClickListener(v -> startActivity(new Intent(this, PasswordChangeActivity.class)));
        about.setOnClickListener(v -> showAboutDialog());
        logout.setOnClickListener(v -> async(
                () -> {
                    new NettelRepository(this).logout();
                    return true;
                },
                ok -> {
                    Intent i = new Intent(this, LoginActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                }
        ));
    }

    private void showAboutDialog() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.WHITE);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundColor(Color.WHITE);
        box.setPadding(Ui.dp(this, 22), Ui.dp(this, 22), Ui.dp(this, 22), Ui.dp(this, 10));
        scroll.addView(box);

        TextView title = text("Acerca de", 24, true);
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        title.setTextColor(getColor(R.color.navy));
        box.addView(title);

        TextView appName = text("Nettel Marítimo", 19, true);
        appName.setGravity(Gravity.CENTER_HORIZONTAL);
        appName.setTextColor(Color.rgb(20, 31, 43));
        appName.setPadding(0, Ui.dp(this, 10), 0, Ui.dp(this, 8));
        box.addView(appName);

        box.addView(section("Descripción de la APP"));
        box.addView(paragraph("Aplicación para control de flota marítima, visualización de dispositivos, alertas e histórico de posiciones en ambiente seguro."));

        box.addView(section("Desarrollado por"));
        box.addView(paragraph("Empresa Nettel\nSitio: nettelcorp.com"));

        box.addView(section("Versión"));
        box.addView(paragraph("1.0"));

        box.addView(section("Términos y condiciones"));
        TextView link = text(TERMS_URL, 16, false);
        link.setTextColor(getColor(R.color.blue));
        link.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        link.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 12));
        link.setOnClickListener(v -> openTerms());
        box.addView(link);

        MaterialButton openButton = Ui.button(this, "Abrir términos y condiciones");
        openButton.setOnClickListener(v -> openTerms());
        box.addView(openButton);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(scroll)
                .setPositiveButton("Cerrar", null)
                .create();
        dialog.setOnShowListener(d -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            }
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.navy));
        });
        dialog.show();
    }

    private TextView section(String value) {
        TextView text = text(value, 17, true);
        text.setTextColor(getColor(R.color.navy));
        text.setPadding(0, Ui.dp(this, 14), 0, Ui.dp(this, 4));
        return text;
    }

    private TextView paragraph(String value) {
        TextView text = text(value, 16, false);
        text.setTextColor(Color.rgb(20, 31, 43));
        text.setLineSpacing(0, 1.08f);
        return text;
    }

    private TextView text(String value, int sizeSp, boolean bold) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(sizeSp);
        if (bold) text.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return text;
    }

    private void openTerms() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(TERMS_URL));
        startActivity(intent);
    }
}
