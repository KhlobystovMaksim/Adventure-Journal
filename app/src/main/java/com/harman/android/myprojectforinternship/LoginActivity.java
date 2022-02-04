package com.harman.android.myprojectforinternship;

import static com.google.android.material.textfield.TextInputLayout.END_ICON_NONE;
import static com.google.android.material.textfield.TextInputLayout.END_ICON_PASSWORD_TOGGLE;

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
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
      private Button login;
      private TextInputEditText email;
      private TextInputEditText password;
      private TextInputLayout loginPasswordLayout;
      private User registerUser;
      private UserLocalStore userLocalStore;


    ActivityResultLauncher<Intent> activityLauncher = registerForActivityResult(new
                    ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult activityResult) {
                    if(activityResult != null && activityResult.getResultCode() == RESULT_OK){
                        if(activityResult.getData() != null && activityResult.getData().getStringExtra("email") !=null &&
                        activityResult.getData().getStringExtra("password")!=null);
                            email.setText(activityResult.getData().getStringExtra("email"));
                            password.setText(activityResult.getData().getStringExtra("password"));
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.emailLog);
        password = findViewById(R.id.passwordLog);
        login = findViewById(R.id.btnLog);
        loginPasswordLayout = findViewById(R.id.loginPasswordLayout);
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        login.setOnClickListener(this);

        userLocalStore = new UserLocalStore(this);

        final Button btnRegLogin = findViewById(R.id.btnRegLogin);
        btnRegLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
               activityLauncher.launch(intent);
               overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
            }
        });
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(loginPasswordLayout.getEndIconMode() == END_ICON_NONE){
                    loginPasswordLayout.setEndIconMode(END_ICON_PASSWORD_TOGGLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }



    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof TextInputEditText
                    && !inViewInBounds(password, (int) event.getRawX(), (int) event.getRawY())
                    && !inViewInBounds(email, (int) event.getRawX(), (int) event.getRawY())) {
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
            case R.id.btnLog:
              if(userLogin()){
                   startActivity(new Intent(v.getContext(),MapActivity.class));
              }else{
                  email.setError("Not valid Email or Password!");
              }

         break;
        }
    }

    private boolean userLogin() {
        String emailUser = email.getText().toString().trim();
        String passwordUser = password.getText().toString().trim();

        if(userLocalStore.getUserLoginIn()){
            registerUser = userLocalStore.getLoggedInUser(emailUser);
        }

        if(emailUser.isEmpty()) {
            email.setError("Email is required!");
            email.requestFocus();

        }
        if (!Patterns.EMAIL_ADDRESS.matcher(emailUser).matches()) {
            email.setError("Please enter a valid email!");
            email.requestFocus();

        }
        if(passwordUser.isEmpty()){
            password.setError("Password is required!");
            password.requestFocus();
            loginPasswordLayout.setEndIconMode(END_ICON_NONE);
            return false;
        }
        if(passwordUser.length() < 6){
            password.setError("Min password should be 6 characters!");
            password.requestFocus();
            loginPasswordLayout.setEndIconMode(END_ICON_NONE);
            return false;
        }
        else{
            loginPasswordLayout.setEndIconMode(END_ICON_PASSWORD_TOGGLE);
        }
        return userLocalStore.getUserLoginIn()
                && registerUser != null
                &&  registerUser.email.equalsIgnoreCase(emailUser)
                && registerUser.password.equalsIgnoreCase(passwordUser);
    }
}