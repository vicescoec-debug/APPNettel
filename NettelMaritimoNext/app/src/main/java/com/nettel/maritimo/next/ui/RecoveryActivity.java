package com.nettel.maritimo.next.ui;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.nettel.maritimo.next.data.NettelRepository;

public class RecoveryActivity extends BaseActivity {
    private TextInputLayout customerId;
    private TextInputLayout user;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout root = Ui.page(this, "Recuperar contraseña");
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        Ui.pad(form, 24);

        customerId = Ui.field(this, "Identificación del cliente", false);
        user = Ui.field(this, "Usuario", false);
        form.addView(customerId);
        form.addView(user);

        android.view.View send = Ui.button(this, "Enviar clave temporal");
        form.addView(send);
        root.addView(form);
        setContentView(root);

        send.setOnClickListener(v -> sendRecovery());
    }

    private void sendRecovery() {
        String idText = Ui.text(customerId);
        String userText = Ui.text(user);

        if (idText.isEmpty()) {
            Ui.toast(this, "Ingrese la identificación del cliente.", Toast.LENGTH_LONG);
            return;
        }
        if (userText.isEmpty()) {
            Ui.toast(this, "Ingrese el usuario.", Toast.LENGTH_LONG);
            return;
        }

        Ui.toast(this, "Enviando clave temporal...", Toast.LENGTH_SHORT);
        async(
                () -> new NettelRepository(this).forgotPassword(idText, userText),
                j -> Ui.toast(this, j.optString("mensaje", j.optString("message", "Solicitud procesada")), Toast.LENGTH_LONG),
                error -> Ui.toast(this, cleanMessage(error), Toast.LENGTH_LONG)
        );
    }
}
