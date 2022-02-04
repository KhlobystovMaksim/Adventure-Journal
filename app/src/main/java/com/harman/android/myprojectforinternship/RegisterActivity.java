package com.harman.android.myprojectforinternship;

import static android.content.ContentValues.TAG;
import static com.google.android.material.textfield.TextInputLayout.END_ICON_NONE;
import static com.google.android.material.textfield.TextInputLayout.END_ICON_PASSWORD_TOGGLE;
import static com.harman.android.myprojectforinternship.R.anim.*;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    Button register;
    private TextInputEditText name;
    private TextInputEditText email;
    private TextInputEditText password;
    private TextInputLayout registerPasswordLayout;

    UserLocalStore userLocalStore;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        register = findViewById(R.id.btnReg);
        name = findViewById(R.id.nameReg);
        email = findViewById(R.id.emailReg);
        password = findViewById(R.id.passwordReg);
        registerPasswordLayout = findViewById(R.id.registerPasswordInputLayout);

        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        userLocalStore = new UserLocalStore(this);

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
             if(registerPasswordLayout.getEndIconMode() == END_ICON_NONE) {
                 registerPasswordLayout.setEndIconMode(END_ICON_PASSWORD_TOGGLE);
             }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

       register.setOnClickListener(this);


        final Button btnLogRegister = findViewById(R.id.btnLogRegister);
        btnLogRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(slide_from_left, slide_to_right);
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof TextInputEditText
            && !inViewInBounds(password, (int) event.getRawX(), (int) event.getRawY())
            && !inViewInBounds(email, (int) event.getRawX(), (int) event.getRawY())
            && !inViewInBounds(name, (int) event.getRawX(), (int) event.getRawY()))  {
                    v.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

            }
        }

        return super.dispatchTouchEvent(event);
    }

    Rect outRect = new Rect();
    int[] location = new int[2];

    private boolean inViewInBounds(View view, int x, int y) {
        view.getDrawingRect(outRect);
        view.getLocationOnScreen(location);
        outRect.offset(location[0], location[1]);
        return outRect.contains(x, y);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnReg:
               if (registerUser()) {
                   User user = new User(name.getText().toString().trim(),
                           email.getText().toString().trim(),
                           password.getText().toString().trim());
                   userLocalStore.storeUserData(user);
                   userLocalStore.setUserLoggedIn(true);

                   Intent intent = new Intent();
                   intent.putExtra("email", email.getText().toString());
                   intent.putExtra("password", password.getText().toString());
                   setResult(RESULT_OK, intent);
                   finish();
                   break;
               }
        }
    }

    private boolean registerUser() {
        String nameUser = name.getText().toString().trim();
        String emailUser = email.getText().toString().trim();
        String passwordUser = password.getText().toString().trim();
        boolean result = true;

        if(nameUser.isEmpty()) {
            name.setError("Name is required!");
            name.requestFocus();
           result = false;
        }
        if (emailUser.isEmpty()) {
            email.setError("Email is required!");
            email.requestFocus();
            result = false;

        }else if(userLocalStore.getLoggedInUser(emailUser)!= null) {
            email.setError("User already registered!");
            email.requestFocus();
            result = false;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(emailUser).matches()) {
            email.setError("Please provide valid email!");
            email.requestFocus();
            result = false;

        }
        if (passwordUser.isEmpty()) {
            password.setError("Password is required!");
            password.requestFocus();
            registerPasswordLayout.setEndIconMode(END_ICON_NONE);
            result = false;
        }
        if(passwordUser.length() < 6) {
            password.setError("Min password length should be 6 characters!");
            password.requestFocus();
            registerPasswordLayout.setEndIconMode(END_ICON_NONE);
            result= false;
        }else{
            registerPasswordLayout.setEndIconMode(END_ICON_PASSWORD_TOGGLE);
        }
        return result;


    }
}