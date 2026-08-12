package com.nettel.maritimo.next.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.nettel.maritimo.next.R;
import com.nettel.maritimo.next.data.NettelRepository;
import com.nettel.maritimo.next.data.SessionStore;
import com.nettel.maritimo.next.data.User;

import java.util.List;

public class UsersActivity extends BaseActivity {
    private TextView profileSummary;
    private TextInputLayout emailField;
    private TextInputLayout phoneField;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout root = Ui.page(this, "Usuarios");
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        Ui.pad(content, 14);

        content.addView(label("Usuario logoneado", 18, Typeface.BOLD));

        profileSummary = label("Cargando datos del usuario...", 14, Typeface.NORMAL);
        profileSummary.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 12));
        content.addView(profileSummary);

        emailField = Ui.field(this, "Correo electrónico", false);
        if (emailField.getEditText() != null) {
            emailField.getEditText().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        }
        content.addView(emailField);

        phoneField = Ui.field(this, "Número de celular", false);
        if (phoneField.getEditText() != null) {
            phoneField.getEditText().setInputType(InputType.TYPE_CLASS_PHONE);
        }
        content.addView(phoneField);

        View save = Ui.button(this, "Actualizar información");
        content.addView(save);

        TextView note = label("Solo se muestran los datos del usuario actualmente conectado.", 13, Typeface.NORMAL);
        note.setPadding(0, Ui.dp(this, 14), 0, 0);
        content.addView(note);

        root.addView(content, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);

        save.setOnClickListener(v -> saveProfile());
        load();
    }

    private void load() {
        async(
                () -> {
                    NettelRepository repo = new NettelRepository(this);
                    return profileFrom(repo.users(), new SessionStore(this).user());
                },
                this::bindProfile
        );
    }

    private User profileFrom(List<User> items, String loggedUser) {
        User fallback = new User();
        fallback.id = loggedUser;
        if (items == null) return fallback;
        for (User user : items) {
            if (same(user.id, loggedUser)) return user;
        }
        return fallback;
    }

    private void bindProfile(User user) {
        if (user == null) user = new User();
        profileSummary.setText(
                "Usuario: " + value(user.id, new SessionStore(this).user()) + "\n"
                        + "Nombre: " + value(user.name, "N/D") + "\n"
                        + "Identificación: " + value(user.identification, "N/D")
        );
        setText(emailField, user.email);
        setText(phoneField, user.phone);
    }

    private void saveProfile() {
        String email = Ui.text(emailField);
        String phone = Ui.text(phoneField);
        if (email.isEmpty()) {
            Ui.toast(this, "Ingrese el correo electrónico.", Toast.LENGTH_LONG);
            return;
        }
        if (phone.isEmpty()) {
            Ui.toast(this, "Ingrese el número de celular.", Toast.LENGTH_LONG);
            return;
        }
        Ui.toast(this, "Actualizando información...", Toast.LENGTH_SHORT);
        async(
                () -> {
                    new NettelRepository(this).updateLoggedUserProfile(email, phone);
                    return true;
                },
                ok -> {
                    Ui.toast(this, "Información actualizada.", Toast.LENGTH_LONG);
                    load();
                },
                error -> Ui.toast(this, cleanMessage(error), Toast.LENGTH_LONG)
        );
    }

    private TextView label(String text, int sp, int style) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(sp);
        view.setTextColor(getColor(R.color.navy));
        view.setTypeface(Typeface.DEFAULT, style);
        return view;
    }

    private void setText(TextInputLayout field, String value) {
        if (field.getEditText() != null) field.getEditText().setText(value == null ? "" : value);
    }

    private boolean same(String a, String b) {
        if (a == null) return b == null;
        return a.equalsIgnoreCase(b == null ? "" : b);
    }

    private String value(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }
}
