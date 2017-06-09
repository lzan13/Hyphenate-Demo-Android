package com.hyphenate.chatuidemo.sign;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.hyphenate.chatuidemo.PreferenceManager;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.ui.BaseActivity;

/**
 * Created by lzan13 on 2017/6/1.
 * Private information configuration activity
 */
public class PrivateConfigActivity extends BaseActivity {

    @BindView(R.id.edit_im_server) EditText imServerView;
    @BindView(R.id.edit_im_port) EditText imPortView;
    @BindView(R.id.edit_rest_server_and_port) EditText restServerView;
    @BindView(R.id.edit_custom_appkey) EditText customAppkeyView;

    private PrivateConfigActivity activity;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_private_config);

        ButterKnife.bind(this);

        initView();
    }

    /**
     * init view
     */
    private void initView() {
        activity = this;

        getActionBarToolbar().setNavigationIcon(R.drawable.em_ic_back);
        getActionBarToolbar().setTitle("Private config");
        getActionBarToolbar().setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                finish();
            }
        });

        if (!TextUtils.isEmpty(PreferenceManager.getInstance().getIMServer())) {
            imServerView.setText(PreferenceManager.getInstance().getIMServer());
        }
        if (!TextUtils.isEmpty(PreferenceManager.getInstance().getIMPort())) {
            imPortView.setText(PreferenceManager.getInstance().getIMPort());
        }
        if (!TextUtils.isEmpty(PreferenceManager.getInstance().getRestServer())) {
            restServerView.setText(PreferenceManager.getInstance().getRestServer());
        }
        if (!TextUtils.isEmpty(PreferenceManager.getInstance().getCustomAppkey())) {
            customAppkeyView.setText(PreferenceManager.getInstance().getCustomAppkey());
        }
    }

    @OnClick(R.id.btn_save) void onClick() {

        PreferenceManager.getInstance().setIMServer(imServerView.getText().toString().trim());
        PreferenceManager.getInstance().setIMPort(imPortView.getText().toString().trim());
        PreferenceManager.getInstance().setRestServer(restServerView.getText().toString().trim());
        PreferenceManager.getInstance().setCustomAppkey(customAppkeyView.getText().toString().trim());
        Toast.makeText(activity, "Config save success!", Toast.LENGTH_LONG).show();
    }
}