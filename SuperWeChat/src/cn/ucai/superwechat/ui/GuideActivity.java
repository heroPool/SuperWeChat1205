package cn.ucai.superwechat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.R;


public class GuideActivity extends BaseActivity {

    @BindView(R.id.iv_splash_logo)
    ImageView ivSplashLogo;
    @BindView(R.id.guide_btn_login)
    Button guideBtnLogin;
    @BindView(R.id.guide_btn_register)
    Button guideBtnRegister;
    @BindView(R.id.splash_root)
    RelativeLayout splashRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.guide_btn_login, R.id.guide_btn_register})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.guide_btn_login:
                startActivity(new Intent(GuideActivity.this, LoginActivity.class));
                break;
            case R.id.guide_btn_register:
                startActivity(new Intent(GuideActivity.this, RegisterActivity.class));
                break;
        }
    }
}
