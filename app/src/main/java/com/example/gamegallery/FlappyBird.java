package com.example.gamegallery;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FlappyBird extends AppCompatActivity {

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameView = new GameView(this);
        setContentView(gameView);
    }

    private class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

        private Thread gameThread;
        private boolean isPlaying;
        private int screenWidth, screenHeight;
        private Paint paint;
        private Rect bird;
        private int birdY;
        private int birdSpeed;
        private int gravity = 3;
        private int jump = -30;
        private boolean isJumping;
        private List<Rect> pipes;
        private int pipeWidth = 200;
        private int pipeHeight = 600;
        private int pipeGap = 400;
        private int pipeSpeed = 10;
        private int score;
        private Random random;

        public GameView(Context context) {
            super(context);
            getHolder().addCallback(this);
            paint = new Paint();
            bird = new Rect(100, 100, 200, 200);
            pipes = new ArrayList<>();
            random = new Random();
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            screenWidth = getWidth();
            screenHeight = getHeight();
            startGame();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            stopGame();
        }

        private void startGame() {
            isPlaying = true;
            birdY = screenHeight / 2 - bird.height() / 2;
            birdSpeed = 0;
            pipes.clear();
            score = 0;
            generatePipes();
            gameThread = new Thread(this);
            gameThread.start();
        }

        private void stopGame() {
            isPlaying = false;
            try {
                restartGame();
                gameThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while (isPlaying) {
                update();
                draw();
                control();
            }
        }

        private void update() {
            if (isJumping) {
                birdSpeed = jump;
                isJumping = false;
            }
            birdY += birdSpeed;
            birdSpeed += gravity;

            if (birdY < 0) birdY = 0;
            if (birdY + bird.height() > screenHeight) {
                birdY = screenHeight - bird.height();
                birdSpeed = 0;
            }

            bird.set(100, birdY, 200, birdY + 100);

            List<Rect> newPipes = new ArrayList<>();
            for (Rect pipe : pipes) {
                pipe.offset(-pipeSpeed, 0);
                if (pipe.right > 0) {
                    newPipes.add(pipe);
                } else {
                    score++;
                }
            }
            pipes = newPipes;

            if (pipes.isEmpty() || pipes.get(pipes.size() - 1).left < screenWidth - 400) {
                generatePipes();
            }

            checkCollisions();
        }

        private void generatePipes() {
            int position = random.nextBoolean() ? screenHeight - pipeHeight : 0;
            pipes.add(new Rect(screenWidth, position, screenWidth + pipeWidth, position + pipeHeight));
            if (position == 0) {
                pipes.add(new Rect(screenWidth, screenHeight - pipeHeight, screenWidth + pipeWidth, screenHeight));
            } else {
                pipes.add(new Rect(screenWidth, 0, screenWidth + pipeWidth, pipeHeight));
            }
        }

        private void checkCollisions() {
            for (Rect pipe : pipes) {
                if (Rect.intersects(pipe, bird)) {
                    stopGame();
                    break;
                }
            }
        }

        private void restartGame() {
            startActivity(getIntent());
        }

        private void draw() {
            if (getHolder().getSurface().isValid()) {
                Canvas canvas = getHolder().lockCanvas();
                canvas.drawColor(Color.CYAN);

                paint.setColor(Color.YELLOW);
                canvas.drawRect(bird, paint);

                paint.setColor(Color.GREEN);
                for (Rect pipe : pipes) {
                    canvas.drawRect(pipe, paint);
                }

                paint.setColor(Color.BLACK);
                paint.setTextSize(100);
                canvas.drawText("Score: " + score, 50, 100, paint);

                getHolder().unlockCanvasAndPost(canvas);
            }
        }

        private void control() {
            try {
                Thread.sleep(17); // ~60 FPS
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                isJumping = true;
            }
            return true;
        }
    }
}
