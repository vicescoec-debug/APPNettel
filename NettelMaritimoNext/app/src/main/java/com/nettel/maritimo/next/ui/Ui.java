package com.nettel.maritimo.next.ui;

import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nettel.maritimo.next.R;

public final class Ui {
    private Ui() {}

    public static LinearLayout page(Context c, String title) {
        LinearLayout root = new LinearLayout(c);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(247, 249, 252));

        MaterialToolbar bar = new MaterialToolbar(c);
        bar.setTitle(title);
        bar.setTitleTextColor(Color.WHITE);
        bar.setBackgroundColor(c.getColor(R.color.navy));
        root.addView(bar, new LinearLayout.LayoutParams(-1, dp(c, 64)));
        return root;
    }

    public static TextInputLayout field(Context c, String hint, boolean password) {
        TextInputLayout layout = new TextInputLayout(c);
        layout.setHintEnabled(false);
        layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        layout.setBoxBackgroundColor(Color.WHITE);
        layout.setBoxStrokeColor(c.getColor(R.color.navy));

        TextInputEditText edit = new TextInputEditText(c);
        edit.setHint(hint);
        edit.setTextColor(Color.rgb(20, 31, 43));
        edit.setHintTextColor(Color.rgb(59, 80, 102));
        edit.setTextSize(16);
        edit.setSingleLine(true);

        if (password) {
            edit.setInputType(0x81);
            layout.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
            layout.setEndIconTintList(android.content.res.ColorStateList.valueOf(c.getColor(R.color.navy)));
            layout.setEndIconContentDescription("Mostrar u ocultar contraseña");
        } else {
            edit.setInputType(InputType.TYPE_CLASS_TEXT
                    | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        }

        layout.addView(edit, new LinearLayout.LayoutParams(-1, dp(c, 56)));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(c, 72));
        lp.setMargins(0, 0, 0, dp(c, 10));
        layout.setLayoutParams(lp);
        return layout;
    }

    public static String text(TextInputLayout l) {
        return l.getEditText() == null ? "" : l.getEditText().getText().toString().trim();
    }

    public static MaterialButton button(Context c, String text) {
        MaterialButton b = new MaterialButton(c);
        b.setText(text);
        b.setTextColor(Color.WHITE);
        b.setTextSize(16);
        b.setAllCaps(false);
        b.setBackgroundColor(c.getColor(R.color.navy));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(c, 56));
        lp.setMargins(0, dp(c, 8), 0, 0);
        b.setLayoutParams(lp);
        return b;
    }

    public static int dp(Context c, int n) {
        return (int) (n * c.getResources().getDisplayMetrics().density + .5f);
    }

    public static void pad(View v, int n) {
        int x = dp(v.getContext(), n);
        v.setPadding(x, x, x, x);
    }

    public static void toast(Context c, String message, int duration) {
        Toast toast = Toast.makeText(c, message, duration);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, dp(c, 180));
        toast.show();
    }
}
