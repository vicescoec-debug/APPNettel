package com.nettel.maritimo.next.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.nettel.maritimo.next.data.NettelRepository;

public class LoginActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout root = Ui.page(this, "Acceso seguro");
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        Ui.pad(form, 20);

        TextView intro = new TextView(this);
        intro.setText("Nettel Marítimo\nControl de Flota y Alertas");
        intro.setTextSize(24);
        intro.setTextColor(Color.rgb(20, 31, 43));
        intro.setGravity(Gravity.CENTER);
        form.addView(intro, new LinearLayout.LayoutParams(-1, Ui.dp(this, 105)));

        TextInputLayout user = Ui.field(this, "Usuario", false);
        TextInputLayout pass = Ui.field(this, "Contraseña", true);
        form.addView(user);
        form.addView(pass);

        View login = Ui.button(this, "Iniciar sesión");
        form.addView(login);

        View recovery = Ui.button(this, "Recuperar contraseña");
        form.addView(recovery);

        LinearLayout privacyBox = new LinearLayout(this);
        privacyBox.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams privacyBoxParams = new LinearLayout.LayoutParams(-1, Ui.dp(this, 150));
        privacyBoxParams.setMargins(0, Ui.dp(this, 72), 0, 0);

        TextView privacy = new TextView(this);
        privacy.setText("Protección de Datos\n"
                + "En cumplimiento con la LOPDP, Nettel S.A. garantiza la protección de tus datos personales. "
                + "Aplicamos el principio de minimización y solo tratamos lo necesario para brindarte el mejor servicio. "
                + "Puedes ejercer tus derechos de rectificación, eliminación o portabilidad contactando a nuestro delegado "
                + "de Protección de Datos en usodedatos@nettelcorp.com. "
                + "La autoridad de control es la Superintendencia de Protección de Datos Personales.");
        privacy.setTextSize(8);
        privacy.setTextColor(Color.rgb(59, 80, 102));
        privacy.setGravity(Gravity.CENTER);
        privacy.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        privacy.setLineSpacing(0, 0.98f);
        privacy.setPadding(Ui.dp(this, 12), 0, Ui.dp(this, 12), 0);
        privacyBox.addView(privacy, new LinearLayout.LayoutParams(-1, LinearLayout.LayoutParams.WRAP_CONTENT));
        form.addView(privacyBox, privacyBoxParams);

        root.addView(form, new LinearLayout.LayoutParams(-1, -1));
        setContentView(root);

        login.setOnClickListener(v -> {
            String u = Ui.text(user);
            String p = Ui.text(pass);
            if (u.isEmpty() || p.isEmpty()) {
                Ui.toast(this, "Ingrese usuario y contraseña", Toast.LENGTH_SHORT);
                return;
            }
            v.setEnabled(false);
            async(() -> new NettelRepository(this).login(u, p), r -> {
                v.setEnabled(true);
                if (r.success()) {
                    async(
                            () -> new NettelRepository(this).verifyPasswordReset(u),
                            j -> {
                                if (j.optInt("mensaje", 0) == 1) {
                                    startActivity(new Intent(this, PasswordChangeActivity.class).putExtra("user", u));
                                } else {
                                    startActivity(new Intent(this, MainActivity.class));
                                }
                                finish();
                            }
                    );
                } else {
                    Ui.toast(this, cleanMessage(new IllegalStateException(r.message)), Toast.LENGTH_LONG);
                }
            }, e -> {
                v.setEnabled(true);
                Ui.toast(this, cleanMessage(e), Toast.LENGTH_LONG);
            });
        });

        recovery.setOnClickListener(v -> startActivity(new Intent(this, RecoveryActivity.class)));
    }
}
