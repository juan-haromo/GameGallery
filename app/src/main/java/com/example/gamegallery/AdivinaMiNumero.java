package com.example.gamegallery;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class AdivinaMiNumero extends AppCompatActivity {

    private int generatedNumber;
    private EditText inputNumber;
    private TextView lastGuess;
    private TextView attempsText;

    private TextView resultTextView;

    private int attemps = 0;

    private RelativeLayout menu;
    Button button_menu;
    Button button_continue;
    Button button_restart;
    Button button_home;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adivina_mi_numero);

        inputNumber = findViewById(R.id.inputNumber);
        lastGuess = findViewById(R.id.lastGuess);
        attempsText = findViewById(R.id.attempsText);
        Button guessButton = findViewById(R.id.guessButton);
        resultTextView = findViewById(R.id.resultTextView);

        // Generar un número aleatorio entre 1 y 100 al inicio
        Iniciar();

        guessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkGuess();
            }
        });

        menu = findViewById(R.id.menu);
        button_menu = findViewById(R.id.button_show_menu);
        button_continue = findViewById(R.id.button_continue);
        button_restart = findViewById(R.id.button_restart);
        button_home = findViewById(R.id.button_home);

        button_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.setVisibility(View.VISIBLE);
                button_menu.setVisibility(View.GONE);
            }
        });

        // boton continuar
        button_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.setVisibility(View.GONE);
                button_menu.setVisibility(View.VISIBLE);
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
                Intent load = new Intent(AdivinaMiNumero.this, MainActivity.class);
                startActivity(load);
            }
        });

    }

    public  void Iniciar(){
        generatedNumber = generateRandomNumber(1, 100);
        attemps = 0;
        lastGuess.setText("Ultima adivinanza: ");
        attempsText.setText("Intentos: ");
        inputNumber.setText("");
        resultTextView.setText("");
    }

    private int generateRandomNumber(int min, int max) {
        Random random = new Random();
        return random.nextInt((max - min) + 1) + min;
    }

    private void checkGuess() {
        String userInput = inputNumber.getText().toString();
        if (!userInput.isEmpty()) {
            int userNumber = Integer.parseInt(userInput);
            attemps++;
            if (userNumber < generatedNumber) {
                resultTextView.setText("Tu número es muy pequeño.");
            } else if (userNumber > generatedNumber) {
                resultTextView.setText("Tu número es muy grande.");
            } else {
                resultTextView.setText("¡Bien hecho! Adivinaste el número.");
                String toasty = "Intentos: " + attemps;
                Toast.makeText(getApplicationContext(),toasty,Toast.LENGTH_LONG).show();
            }
            lastGuess.setText("Ultima adivinanza: " + userInput);
            attempsText.setText("Intentos: " + attemps);
            inputNumber.setText("");
        } else {
            resultTextView.setText("Por favor, ingresa un número.");
        }
    }

}