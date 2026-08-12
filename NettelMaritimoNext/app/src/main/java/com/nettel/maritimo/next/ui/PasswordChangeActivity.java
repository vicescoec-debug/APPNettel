package com.nettel.maritimo.next.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.nettel.maritimo.next.data.NettelRepository;
import com.nettel.maritimo.next.data.SessionStore;

public class PasswordChangeActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        String user = getIntent().getStringExtra("user");

        LinearLayout root = Ui.page(this, "Cambiar contraseña");
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        Ui.pad(form, 24);

        TextInputLayout old = Ui.field(this, "Clave actual", true);
        TextInputLayout next = Ui.field(this, "Nueva clave", true);
        TextInputLayout confirm = Ui.field(this, "Confirmar clave", true);
        form.addView(old);
        form.addView(next);
        form.addView(confirm);

        android.view.View save = Ui.button(this, "Guardar contraseña");
        form.addView(save);
        root.addView(form);
        setContentView(root);

        save.setOnClickListener(v -> {
            if (!Ui.text(next).equals(Ui.text(confirm))) {
                Ui.toast(this, "Las claves no coinciden", Toast.LENGTH_SHORT);
                return;
            }
            String activeUser = user == null ? new SessionStore(this).user() : user;
            async(
                    () -> new NettelRepository(this).changePassword(activeUser, Ui.text(old), Ui.text(next)),
                    j -> {
                        int ok = j.optInt("mensaje", j.optBoolean("success", false) ? 1 : 0);
                        if (ok == 1 || j.optInt("action", 2) == 0) {
                            Ui.toast(this, "Contraseña actualizada", Toast.LENGTH_SHORT);
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        } else {
                            Ui.toast(this, j.optString("message", "No fue posible cambiar la contraseña"), Toast.LENGTH_LONG);
                        }
                    }
            );
        });
    }
}
