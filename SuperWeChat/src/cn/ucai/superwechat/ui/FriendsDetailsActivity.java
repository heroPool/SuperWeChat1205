package cn.ucai.superwechat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hyphenate.easeui.domain.User;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.easeui.widget.EaseTitleBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatHelper;


public class FriendsDetailsActivity extends BaseActivity {

    @BindView(R.id.image_friendsdetails_avatar)
    ImageView imageFriendsdetailsAvatar;
    @BindView(R.id.text_friendsdetails_nick)
    TextView textFriendsdetailsNick;
    @BindView(R.id.text_friendsdetai_username)
    TextView textFriendsdetaiUsername;
    @BindView(R.id.layout_setting_notes)
    RelativeLayout layoutSettingNotes;
    @BindView(R.id.btn_add_contact)
    Button btnAddContact;
    @BindView(R.id.btn_send_msg)
    Button btnSendMsg;
    @BindView(R.id.btn_send_video)
    Button btnSendVideo;
    @BindView(R.id.title_bar)
    EaseTitleBar titleBar;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_details);
        ButterKnife.bind(this);
        initView();
        initData();
    }

    private void initData() {
        user = (User) getIntent().getSerializableExtra(I.User.TABLE_NAME);
        Log.e(FriendsDetailsActivity.class.getSimpleName(), user.toString());
        if (user != null) {
            showUserInfo();
        } else {
            finish();
        }
    }

    private void showUserInfo() {
        boolean isFriend = SuperWeChatHelper.getInstance().getAppContactList().containsKey(user.getMUserName());
        if (isFriend) {
            SuperWeChatHelper.getInstance().saveAppContact(user);
        }
        textFriendsdetaiUsername.setText(user.getMUserName());
        EaseUserUtils.setAppUserAvatar(FriendsDetailsActivity.this, user.getMUserName(), imageFriendsdetailsAvatar);
        EaseUserUtils.setAppUserNick(user.getMUserName(), textFriendsdetailsNick);
        showFriend(isFriend);
    }

    private void showFriend(boolean isFriend) {
        btnAddContact.setVisibility(isFriend ? View.VISIBLE : View.GONE);
        btnSendMsg.setVisibility(isFriend ? View.GONE : View.VISIBLE);
        btnSendVideo.setVisibility(isFriend ? View.GONE : View.VISIBLE);
    }

    private void initView() {
        getActionBar().hide();
        titleBar.setLeftLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

            }
        });
    }

    @OnClick({R.id.layout_setting_notes, R.id.btn_add_contact, R.id.btn_send_msg, R.id.btn_send_video})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.layout_setting_notes:
                break;
            case R.id.btn_add_contact:
                boolean isConfirm = true;
                if (isConfirm) {
                    startActivity(new Intent(this, SearchUserActivity.class).putExtra(I.User.TABLE_NAME, user.getMUserName()));

                } else {

                }

                break;
            case R.id.btn_send_msg:
                break;
            case R.id.btn_send_video:
                break;
        }
    }
}
