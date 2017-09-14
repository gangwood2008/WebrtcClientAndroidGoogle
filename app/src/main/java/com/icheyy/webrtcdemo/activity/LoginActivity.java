package com.icheyy.webrtcdemo.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.icheyy.webrtcdemo.R;
import com.icheyy.webrtcdemo.base.BaseAppActivity;

public class LoginActivity extends BaseAppActivity {

    private static final int CONNECTION_REQUEST = 1;

    private EditText et_user_name;
    private Button bt_login;
    private TextView tv_message;

    private SharedPreferences sharedPref;
    private String keyprefUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initPreference();// 参数初始化

        getView();

        initView();


    }

    private void getView() {
        et_user_name = (EditText) findViewById(R.id.et_user_name);
        tv_message = (TextView) findViewById(R.id.tv_login_message);
        bt_login = (Button) findViewById(R.id.bt_login);
    }

    private void initView() {
        // 用户名
        String userName = sharedPref.getString(keyprefUserName, "cheyy");
        et_user_name.setText(userName);
        et_user_name.setSelection(userName.length());
        et_user_name.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    bt_login.performClick();//在键盘中按完成键，模拟点击添加room按钮
                    return true;
                }
                return false;
            }
        });
        et_user_name.requestFocus();

        // 登入
        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = getUserName();
                if (userName.isEmpty())
                    tv_message.setText("用户名不能为空");

                goToSelectCaller();
            }
        });
    }

    private void initPreference() {
        // Get setting keys.
        // 取得设置界面中setting的key值
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        keyprefUserName = getString(R.string.pref_user_name_key);
    }

    @Override
    public void onPause() {
        super.onPause();
        // 保存userName输入框信息
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(keyprefUserName, getUserName());
        editor.commit();
    }


    /**
     * 跳转到选择被呼叫页面
     */
    private void goToSelectCaller() {

        Intent intent = new Intent(LoginActivity.this, SelectCallerActivity.class);
        startActivity(intent);

    }

    private String getUserName() {
        if (et_user_name == null)
            return "";

        String userName = et_user_name.getText().toString().trim();

        return userName;

    }



}
