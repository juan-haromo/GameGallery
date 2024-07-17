package com.example.gamegallery;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class FlappyBird extends AppCompatActivity {

    private SurfaceView gameView;
    private GameThread gameThread;
    private CardView gameOverCard;
    private TextView scoreTextView;
    private int score = 0;
    Button button_menu;
    Button button_continue;
    Button button_restart;
    Button button_home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flappy_bird);

        gameView = findViewById(R.id.gameView);
        gameOverCard = findViewById(R.id.gameOverCard);
        scoreTextView = findViewById(R.id.scoreTextView);
        button_menu = findViewById(R.id.button_show_menu);
        button_continue = findViewById(R.id.button_continue);
        button_restart = findViewById(R.id.button_restart);
        button_home = findViewById(R.id.button_home);

        button_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameOverCard.setVisibility(View.VISIBLE);
                button_menu.setVisibility(View.GONE);
                gameThread.pauseGame();
            }
        });

        // boton continuar
        button_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameOverCard.setVisibility(View.GONE);
                button_menu.setVisibility(View.VISIBLE);
                gameThread.resumeGame();
            }
        });

        // uso de boton restart
        button_restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(getIntent());
            }
        });

        // uso de boton home
        button_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent load = new Intent(FlappyBird.this, MainActivity.class);
                startActivity(load);
            }
        });

        SurfaceHolder holder = gameView.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                gameThread = new GameThread(surfaceHolder);
                gameThread.setRunning(true);
                gameThread.start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                boolean retry = true;
                gameThread.setRunning(false);
                while (retry) {
                    try {
                        gameThread.join();
                        retry = false;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        gameView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    gameThread.flap();
                }
                return true;
            }
        });
    }

    public void showGameOver() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gameOverCard.setVisibility(View.VISIBLE);
            }
        });
    }

    public void updateScore(final int score) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                scoreTextView.setText("Score: " + score);
            }
        });
    }


    private class GameThread extends Thread {
        private SurfaceHolder surfaceHolder;
        private boolean running;
        private boolean paused;
        private float birdY;
        private float birdVelocity;
        private float pipeX;
        private float upperPipeHeight;
        private static final float GRAVITY = 1.0f;
        private static final float FLAP_STRENGTH = -15.0f;
        private boolean passedPipe = false;
        private Bitmap bgBitmap;
        private Bitmap birdBitmap;
        private Bitmap upperPipeBitmap;
        private Bitmap lowerPipeBitmap;

        public GameThread(SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;
            birdY = 300;
            birdVelocity = 0;
            pipeX = surfaceHolder.getSurfaceFrame().right;
            upperPipeHeight = getRandomHeight();

            bgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.fbbg);
            birdBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bird);
            upperPipeBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.tbs);
            lowerPipeBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.tbi);
        }

        public void setRunning(boolean running) {
            this.running = running;
        }

        public boolean isPaused() {
            return paused;
        }

        public void pauseGame() {
            paused = true;
        }

        public void resumeGame() {
            paused = false;
        }

        @Override
        public void run() {
            while (running) {
                if (!paused) {
                    update();
                }
                draw();
                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void update() {
            birdVelocity += GRAVITY;
            birdY += birdVelocity;
            pipeX -= 10;

            if (pipeX < -upperPipeBitmap.getWidth()) {
                pipeX = surfaceHolder.getSurfaceFrame().right;
                upperPipeHeight = getRandomHeight();
                passedPipe = false;
            }

            if (!passedPipe && pipeX + upperPipeBitmap.getWidth() < 100) {
                score += 100;
                updateScore(score);
                passedPipe = true;
            }

            if (birdY > surfaceHolder.getSurfaceFrame().bottom || birdY < 0 || checkCollision()) {
                setRunning(false);
                showGameOver();
            }
        }

        private void draw() {
            Canvas canvas = surfaceHolder.lockCanvas();
            if (canvas != null) {
                // Redimensionar el fondo para que ocupe toda la pantalla
                Bitmap scaledBg = Bitmap.createScaledBitmap(bgBitmap, canvas.getWidth(), canvas.getHeight(), true);
                canvas.drawBitmap(scaledBg, 0, 0, null);

                Paint paint = new Paint();

                // Redimensionar y dibujar el pájaro
                Bitmap scaledBird = Bitmap.createScaledBitmap(birdBitmap, birdBitmap.getWidth() / 2, birdBitmap.getHeight() / 2, true);
                canvas.drawBitmap(scaledBird, 100 - scaledBird.getWidth() / 2, birdY - scaledBird.getHeight() / 2, paint);

                // Redimensionar y dibujar las tuberías
                int pipeWidth = upperPipeBitmap.getWidth();
                int pipeHeight = upperPipeBitmap.getHeight();
                Bitmap scaledUpperPipe = Bitmap.createScaledBitmap(upperPipeBitmap, pipeWidth, pipeHeight, true);
                Bitmap scaledLowerPipe = Bitmap.createScaledBitmap(lowerPipeBitmap, pipeWidth, pipeHeight, true);

                canvas.drawBitmap(scaledUpperPipe, pipeX, upperPipeHeight - scaledUpperPipe.getHeight(), paint);
                canvas.drawBitmap(scaledLowerPipe, pipeX, upperPipeHeight + canvas.getHeight() / 3, paint);

                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }

        private void flap() {
            birdVelocity = FLAP_STRENGTH;
        }

        private float getRandomHeight() {
            int minHeight = surfaceHolder.getSurfaceFrame().bottom / 9;
            int maxHeight = 6 * surfaceHolder.getSurfaceFrame().bottom / 9;
            return (float) (Math.random() * (maxHeight - minHeight) + minHeight);
        }

        private boolean checkCollision() {
            return (100 > pipeX && 100 < pipeX + upperPipeBitmap.getWidth() &&
                    (birdY < upperPipeHeight || birdY > upperPipeHeight + surfaceHolder.getSurfaceFrame().bottom / 3));
        }
    }
}
