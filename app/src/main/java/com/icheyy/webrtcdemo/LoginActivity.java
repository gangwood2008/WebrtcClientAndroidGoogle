package com.icheyy.webrtcdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity {

    private static final int CONNECTION_REQUEST = 1;

    private EditText et_user_name;
    private Button bt_login;
    private TextView tv_message;

    private SharedPreferences sharedPref;
    private String keyprefP2PServerUrl;
    private String keyprefUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Get setting keys.
        // 取得设置界面中setting的key值
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        keyprefP2PServerUrl = getString(R.string.pref_p2p_server_url_key);
        keyprefUserName = getString(R.string.pref_user_name_key);




        et_user_name = (EditText) findViewById(R.id.et_user_name);
        tv_message = (TextView) findViewById(R.id.tv_login_message);
        bt_login = (Button) findViewById(R.id.bt_login);



        String userName = sharedPref.getString(keyprefUserName, "cheyy");
        et_user_name.setText(userName);
        et_user_name.setSelection(userName.length());


        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = getUserName();
                if(userName.isEmpty())
                    tv_message.setText("用户名不能为空");

                goToSelectCaller();
            }
        });



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

        String p2pServerUrl = sharedPref.getString(
                keyprefP2PServerUrl, getString(R.string.pref_p2p_server_url_default));

        if (validateUrl(p2pServerUrl)) {
            Uri uri = Uri.parse(p2pServerUrl);
            Intent intent = new Intent(LoginActivity.this, SelectCallerActivity.class);
            intent.setData(uri);
            intent.putExtra(SelectCallerActivity.EXTRA_USER_NAME, getUserName());


            startActivityForResult(intent, CONNECTION_REQUEST);
        }
    }

    private String getUserName() {
        if (et_user_name == null)
            return "";

        String userName = et_user_name.getText().toString().trim();

        return userName;

    }

    /**
     * 验证url
     * @param url
     * @return
     */
    private boolean validateUrl(String url) {
        if (URLUtil.isHttpsUrl(url) || URLUtil.isHttpUrl(url)) {
            return true;
        }

        // url错误
        new AlertDialog.Builder(this)
                .setTitle(getText(R.string.invalid_url_title))
                .setMessage(getString(R.string.invalid_url_text, url))
                .setCancelable(false)
                .setNeutralButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                .create()
                .show();
        return false;
    }


}
