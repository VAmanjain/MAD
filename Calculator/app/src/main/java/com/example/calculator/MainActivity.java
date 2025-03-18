package com.example.calculator;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {
    EditText num1, num2;
    TextView result;
    Button btnAdd, btnSub, btnMul, btnDiv;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        num1 = findViewById(R.id.num1);
        num2 = findViewById(R.id.num2);
        result = findViewById(R.id.result);
        btnAdd = findViewById(R.id.btnAdd);
        btnSub = findViewById(R.id.btnSub);
        btnMul = findViewById(R.id.btnMul);
        btnDiv = findViewById(R.id.btnDiv);

        btnAdd.setOnClickListener(v -> calculate('+'));
        btnSub.setOnClickListener(v -> calculate('-'));
        btnMul.setOnClickListener(v -> calculate('*'));
        btnDiv.setOnClickListener(v -> calculate('/'));
    }

    private void calculate(char operator) {
        String n1 = num1.getText().toString();
        String n2 = num2.getText().toString();

        if (n1.isEmpty() || n2.isEmpty()) {
            result.setText("Result: Please enter both numbers");
            return;
        }

        double number1, number2;

        try {
            number1 = Double.parseDouble(n1);
            number2 = Double.parseDouble(n2);
        } catch (NumberFormatException e) {
            result.setText("Result: Invalid number format");
            return;
        }

        double res = 0;
        switch (operator) {
            case '+':
                res = number1 + number2;
                break;
            case '-':
                res = number1 - number2;
                break;
            case '*':
                res = number1 * number2;
                break;
            case '/':
                if (number2 == 0) {
                    result.setText("Result: Cannot divide by zero");
                    return;
                }
                res = number1 / number2;
                break;
        }

        DecimalFormat df = new DecimalFormat("0.00");
        result.setText("Result: " + df.format(res));
    }
}
