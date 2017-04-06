package cn.ucai.superwechat.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.widget.EaseAlertDialog;
import com.hyphenate.easeui.widget.EaseTitleBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatHelper;


public class SearchUserActivity extends BaseActivity {

    @BindView(R.id.btn_send)
    Button btnSend;
    @BindView(R.id.title_bar)
    EaseTitleBar titleBar;
    @BindView(R.id.et_msg)
    EditText etMsg;
    String toAddUserName = null;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);
        ButterKnife.bind(this);
        initView();
        initData();
    }

    private void initData() {
        toAddUserName = getIntent().getStringExtra(I.User.USER_NAME);
        if (toAddUserName != null) {
            finish();
        }
    }
    private void initView() {
        titleBar.setLeftLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        etMsg.setText(getString(R.string.addcontact_send_msg_prefix) +
                SuperWeChatHelper.getInstance().getUserProfileManager().
                        getAppCurrentUserInfo().getMUserNick());

    }

    @OnClick({R.id.btn_send, R.id.title_bar, R.id.et_msg})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_send:
                if (toAddUserName != null) {
                    addContact();
                }
                break;
            case R.id.title_bar:
                break;
            case R.id.et_msg:
                break;
        }
    }


    public void addContact() {
        if (EMClient.getInstance().getCurrentUser().equals(toAddUserName)) {
            new EaseAlertDialog(this, R.string.not_add_myself).show();
            return;
        }

        if (SuperWeChatHelper.getInstance().getAppContactList().containsKey(toAddUserName)) {
            //let the user know the contact already in your contact list
            if (EMClient.getInstance().contactManager().getBlackListUsernames().contains(toAddUserName)) {
                new EaseAlertDialog(this, R.string.user_already_in_contactlist).show();
                return;
            }
            new EaseAlertDialog(this, R.string.This_user_is_already_your_friend).show();
            return;
        }

        progressDialog = new ProgressDialog(this);
        String stri = getResources().getString(R.string.Is_sending_a_request);
        progressDialog.setMessage(stri);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        new Thread(new Runnable() {
            public void run() {

                try {
                    //demo use a hardcode reason here, you need let user to input if you like
                    String s = etMsg.getText().toString();
                    EMClient.getInstance().contactManager().addContact(toAddUserName, s);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            String s1 = getResources().getString(R.string.send_successful);
                            Toast.makeText(getApplicationContext(), s1, Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            String s2 = getResources().getString(R.string.Request_add_buddy_failure);
                            Toast.makeText(getApplicationContext(), s2 + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }
}

