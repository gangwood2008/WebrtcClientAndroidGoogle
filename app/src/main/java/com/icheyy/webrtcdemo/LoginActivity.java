package com.icheyy.webrtcdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity {

    private EditText et_user_name;
    private Button bt_login;
    private TextView tv_message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        et_user_name = (EditText) findViewById(R.id.et_user_name);
        tv_message = (TextView) findViewById(R.id.tv_login_message);
        bt_login = (Button) findViewById(R.id.bt_login);
        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = getUserName();
                if(userName.isEmpty())
                    tv_message.setText("用户名不能为空");

                Intent intent = new Intent(LoginActivity.this, SelectCallerActivity.class);
                startActivity(intent);
            }
        });


    }

    private String getUserName() {
        if (et_user_name == null)
            return "";

        String userName = et_user_name.getText().toString().trim();

        return userName;

    }


}
