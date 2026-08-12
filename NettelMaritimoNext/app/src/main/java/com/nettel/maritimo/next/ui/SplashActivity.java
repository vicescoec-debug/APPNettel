package com.nettel.maritimo.next.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nettel.maritimo.next.R;
import com.nettel.maritimo.next.data.SessionStore;

public class SplashActivity extends BaseActivity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private ImageView cover;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        cover = new ImageView(this);
        cover.setImageResource(R.drawable.splash_cover);
        cover.setScaleType(ImageView.ScaleType.CENTER_CROP);
        cover.setAdjustViewBounds(false);
        setContentView(cover, new ViewGroup.LayoutParams(-1, -1));

        handler.postDelayed(() -> {
            cover.setImageResource(R.drawable.splash_welcome);
            handler.postDelayed(this::goNext, 2000);
        }, 1300);
    }

    private void goNext() {
        startActivity(new Intent(
                this,
                new SessionStore(this).active() ? MainActivity.class : LoginActivity.class
        ));
        finish();
    }
}
