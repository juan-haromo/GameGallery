package com.example.gamegallery;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

public class GameView extends View implements SensorEventListener {

    private Paint paint;
    private SensorManager sensorManager;
    private Sensor lightSensor, accelerometer;
    private float lightLevel = 0;
    private float accelX = 0, accelY = 0;

    // Positions and sizes for the platforms and player
    private int playerX, playerY, playerSize;
    private int finishX, finishY, finishSize;

    // Positions for light-sensitive platforms
    private int[] platformX, platformY;
    private int platformWidth, platformHeight;

    private int[] darkPlatformX, darkPlatformY;

    // Bitmaps for textures
    private Bitmap backgroundBitmap;



    public GameView(Context context) {
        super(context);
        init(context);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        paint = new Paint();
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);

        // Load textures
        try {
            backgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background);

        } catch (Exception e) {
            Log.e("GameView", "Error loading bitmaps", e);
        }

        // Initialize platform positions based on screen size
        initializePositions();
        resetGame();
    }

    private void initializePositions() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        // Initialize the size and positions based on screen dimensions
        playerSize = screenWidth / 25;
        finishSize = screenWidth / 15;
        finishX = screenWidth - finishSize * 2;
        finishY = screenHeight / 2;

        platformWidth = screenWidth / 5;
        platformHeight = screenHeight / 20;


        platformX = new int[]{screenWidth / 10, screenWidth / 5, screenWidth / 3, screenWidth / 2, screenWidth * 3 / 5, screenWidth * 4 / 5};
        platformY = new int[]{screenHeight / 3, screenHeight / 4, screenHeight / 2, screenHeight * 2 / 3, screenHeight / 5, screenHeight / 2};

        darkPlatformX = new int[]{screenWidth / 10, screenWidth / 4, screenWidth / 2, screenWidth * 2 / 3, screenWidth * 3 / 4, screenWidth / 3};
        darkPlatformY = new int[]{screenHeight / 2, screenHeight / 3, screenHeight / 3, screenHeight * 7 / 10, screenHeight / 8, screenHeight / 10};
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw the background
        if (backgroundBitmap != null) {
            canvas.drawBitmap(backgroundBitmap, 0, 0, paint);
        }


        paint.setColor(Color.BLUE);
        canvas.drawCircle(playerX, playerY, playerSize, paint);

        paint.setColor(Color.GREEN);
        canvas.drawCircle(finishX, finishY, finishSize, paint);


        // Draw light-sensitive platforms
        if (lightLevel > 10) {
            paint.setColor(Color.WHITE);
            for (int i = 0; i < platformX.length; i++) {
                canvas.drawRect(platformX[i], platformY[i], platformX[i] + platformWidth, platformY[i] + platformHeight, paint);
                if (isColliding(playerX, playerY, playerSize, playerSize, platformX[i], platformY[i], platformWidth, platformHeight)) {
                    handleCollision();
                }
        }
        }

        // Draw dark-sensitive platforms
        if (lightLevel <= 10) {
            paint.setColor(Color.BLACK);
            for (int i = 0; i < darkPlatformX.length; i++) {
                canvas.drawRect(darkPlatformX[i], darkPlatformY[i], darkPlatformX[i] + platformWidth, darkPlatformY[i] + platformHeight, paint);
                if (isColliding(playerX, playerY, playerSize, playerSize, darkPlatformX[i], darkPlatformY[i], platformWidth, platformHeight)) {
                    handleCollision();
                }
            }
        }

        // Check if the player has reached the finish
        int dx = (playerX + playerSize / 2) - finishX;
        int dy = (playerY + playerSize / 2) - finishY;
        int distanceSquared = dx * dx + dy * dy;
        int radiusSquared = finishSize * finishSize;

        if (distanceSquared <= radiusSquared) {
            ((GameActivity) getContext()).winGame();
        }

        // Redraw the view
        invalidate();
    }

    private boolean isColliding(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2) {
        return x1 < x2 + w2 && x1 + w1 > x2 && y1 < y2 + h2 && y1 + h1 > y2;
    }

    private void handleCollision() {
        // Handle collision with a platform (e.g., reset player position)
        playerX = 100;
        playerY = 100;
    }

    public void resetGame() {
        playerX = 100;
        playerY = 100;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            lightLevel = event.values[0];
        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelX = event.values[0];
            accelY = event.values[1];

            // Update player position based on accelerometer data
            playerX += (int) accelY * 2;  // Correct the left-right movement
            playerY += (int) accelX * 2;  // Use the X-axis for up-down movement

            // Ensure the player stays within the screen bounds
            if (playerX < 0) playerX = 0;
            if (playerY < 0) playerY = 0;
            if (playerX + playerSize > getWidth()) playerX = getWidth() - playerSize;
            if (playerY + playerSize > getHeight()) playerY = getHeight() - playerSize;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }
}

