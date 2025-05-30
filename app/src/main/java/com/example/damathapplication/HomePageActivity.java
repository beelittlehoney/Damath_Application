package com.example.damathapplication;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class HomePageActivity extends AppCompatActivity {

    private ImageButton playButton;
    private ImageButton howToPlayButton;
    private MediaPlayer bkgdrmsc;
    private Handler handler = new Handler();
    private Random random = new Random();
    private String[] operators = {"+", "-", "×", "÷"};
    private int[][] colorSets = {
            {Color.argb(80, 255, 82, 82), Color.argb(80, 255, 215, 64)},   // Red/Amber
            {Color.argb(80, 100, 255, 218), Color.argb(80, 68, 138, 255)},  // Teal/Blue
            {Color.argb(80, 179, 136, 255), Color.argb(80, 255, 128, 171)}, // Purple/Pink
            {Color.argb(80, 105, 240, 174), Color.argb(80, 255, 255, 141)}  // Green/Yellow
    };
    private int[] textSizes = {36, 42, 48, 54}; // Larger sizes for visibility without background

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        // Initialize background music
        bkgdrmsc = MediaPlayer.create(this, R.raw.backgroundmusic);
        bkgdrmsc.setLooping(true);
        bkgdrmsc.start();

        playButton = findViewById(R.id.playButton);
        howToPlayButton = findViewById(R.id.howToPlayButton);

        playButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, Login.class);
            startActivity(intent);
            finish();
        });

        howToPlayButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, HowToPlayActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bkgdrmsc == null) {
            bkgdrmsc = MediaPlayer.create(this, R.raw.backgroundmusic);
            bkgdrmsc.setLooping(true);
            bkgdrmsc.start();
        }
        startOperatorAnimation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bkgdrmsc != null) {
            bkgdrmsc.release();
            bkgdrmsc = null;
        }
        stopOperatorAnimation();
    }

    private void startOperatorAnimation() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                createFallingOperator();
                handler.postDelayed(this, 800); // Slower creation rate for subtle effect
            }
        }, 500);
    }

    private void stopOperatorAnimation() {
        handler.removeCallbacksAndMessages(null);
        ViewGroup rootView = (ViewGroup) getWindow().getDecorView().getRootView();
        for (int i = 0; i < rootView.getChildCount(); i++) {
            if (rootView.getChildAt(i) instanceof TextView) {
                TextView tv = (TextView) rootView.getChildAt(i);
                if (operatorsContain(tv.getText().toString())) {
                    rootView.removeView(tv);
                    i--;
                }
            }
        }
    }

    private boolean operatorsContain(String text) {
        for (String op : operators) {
            if (op.equals(text)) return true;
        }
        return false;
    }

    private void createFallingOperator() {
        final TextView operator = new TextView(this);
        operator.setText(operators[random.nextInt(operators.length)]);
        operator.setTextSize(textSizes[random.nextInt(textSizes.length)]);

        // Set semi-transparent random color from a color set
        int[] colorSet = colorSets[random.nextInt(colorSets.length)];
        operator.setTextColor(colorSet[random.nextInt(colorSet.length)]);

        operator.setAlpha(0.7f); // Make more transparent
        operator.setElevation(1); // Lower elevation

        ViewGroup rootView = (ViewGroup) getWindow().getDecorView().getRootView();
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rootView.addView(operator, params);

        int startX = random.nextInt(rootView.getWidth() - 300) + 150;
        operator.setX(startX);
        operator.setY(-100);

        // Color transition animation
        operator.animate()
                .setDuration(1500)
                .setUpdateListener(animation -> {
                    // Smooth color transition between colors in the set
                    float fraction = animation.getAnimatedFraction();
                    int newColor = blendColors(
                            colorSet[0],
                            colorSet[1],
                            (float) (0.5 + 0.5 * Math.sin(fraction * Math.PI))
                    );
                    operator.setTextColor(newColor);
                })
                .withEndAction(() -> {
                    // Repeat color animation
                    operator.animate()
                            .setDuration(1500)
                            .setUpdateListener(animation -> {
                                float fraction = animation.getAnimatedFraction();
                                int newColor = blendColors(
                                        colorSet[1],
                                        colorSet[0],
                                        (float) (0.5 + 0.5 * Math.sin(fraction * Math.PI))
                                );
                                operator.setTextColor(newColor);
                            })
                            .start();
                })
                .start();

        // Falling animation
        operator.animate()
                .y(rootView.getHeight())
                .rotation(random.nextFloat() * 30 - 15) // Subtle rotation
                .setDuration(5000 + random.nextInt(3000)) // Slower falling speed
                .withEndAction(() -> {
                    if (operator.getParent() != null) {
                        ((ViewGroup) operator.getParent()).removeView(operator);
                    }
                })
                .start();
    }

    private int blendColors(int color1, int color2, float ratio) {
        final float inverseRatio = 1f - ratio;
        float a = (Color.alpha(color1) * inverseRatio) + (Color.alpha(color2) * ratio);
        float r = (Color.red(color1) * inverseRatio) + (Color.red(color2) * ratio);
        float g = (Color.green(color1) * inverseRatio) + (Color.green(color2) * ratio);
        float b = (Color.blue(color1) * inverseRatio) + (Color.blue(color2) * ratio);
        return Color.argb((int) a, (int) r, (int) g, (int) b);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bkgdrmsc != null) {
            bkgdrmsc.release();
            bkgdrmsc = null;
        }
        stopOperatorAnimation();
    }
}