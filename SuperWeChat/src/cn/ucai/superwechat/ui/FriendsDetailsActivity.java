package cn.ucai.superwechat.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.domain.User;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.easeui.widget.EaseTitleBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatHelper;
import cn.ucai.superwechat.db.InviteMessgeDao;
import cn.ucai.superwechat.domain.InviteMessage;
import cn.ucai.superwechat.net.IUserRegisterModel;
import cn.ucai.superwechat.net.OnCompleteListener;
import cn.ucai.superwechat.net.UserRegisterModel;
import cn.ucai.superwechat.utils.Result;
import cn.ucai.superwechat.utils.ResultUtils;


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
    IUserRegisterModel model;
    InviteMessage msg;
    boolean isFriend = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_details);
        ButterKnife.bind(this);
        initView();
        initData();
    }

    private void initData() {
        model = new UserRegisterModel();
        user = (User) getIntent().getSerializableExtra(I.User.TABLE_NAME);
        Log.e(FriendsDetailsActivity.class.getSimpleName(), user.toString());
        if (user == null) {
            msg = (InviteMessage) getIntent().getSerializableExtra(I.User.NICK);
            if (msg != null) {
                user = new User(msg.getFrom());
                user.setMUserNick(msg.getNickname());
                user.setAvatar(msg.getAvatar());
            }
        }
        if (user == null) {
            String username = getIntent().getStringExtra(I.User.USER_NAME);
            if (username != null) {
                user = new User(username);
            }
        }
        if (user == null) {
            finish();

        } else {
            showUserInfo();
            syncUserInfo();
        }
    }

    private void showUserInfo() {
        isFriend = SuperWeChatHelper.getInstance().getAppContactList().containsKey(user.getMUserName());
        Log.e(FriendsDetailsActivity.class.getSimpleName(), "isFriend+" + isFriend);
        if (isFriend && user.getMUserNick() != null) {
            SuperWeChatHelper.getInstance().saveAppContact(user);
        }
        textFriendsdetaiUsername.setText(user.getMUserName());
        textFriendsdetailsNick.setText(user.getMUserNick());
        EaseUserUtils.setAppUserAvatar(FriendsDetailsActivity.this, user.getMUserName(), imageFriendsdetailsAvatar);
//        EaseUserUtils.setAppUserNick(user.getMUserName(), textFriendsdetailsNick);
        showFriend(isFriend);
//        syncUserInfo();//从服务器异步加载用户的最新信息,填充到好友列表或者新的朋友列表
    }

    private void syncUserInfo() {
        model.loadUserInfo(FriendsDetailsActivity.this, user.getMUserName(),
                new OnCompleteListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        if (s != null) {
                            Result result = ResultUtils.getResultFromJson(s, User.class);
                            if (result != null && result.isRetMsg()) {
                                User u = (User) result.getRetData();
                                if (u != null) {
                                    if (msg != null) {
                                        //update msg
                                        ContentValues values = new ContentValues();
                                        values.put(InviteMessgeDao.COLUMN_NAME_NICK, u.getMUserNick());
                                        values.put(InviteMessgeDao.COLUMN_NAME_AVATAR, u.getAvatar());
                                        InviteMessgeDao dao = new InviteMessgeDao(FriendsDetailsActivity.this);
                                        dao.updateMessage(msg.getId(), values);
                                    } else if (isFriend) {
                                        //update user
                                        SuperWeChatHelper.getInstance().saveAppContact(u);
                                    }
                                    user = u;
                                    showUserInfo();

                                }
                            }
                        }
                    }

                    @Override
                    public void onError(String error) {

                    }
                });
    }

    private void showFriend(boolean isFriend) {
        Log.e(FriendsDetailsActivity.class.getSimpleName(), "isFriend+" + isFriend);
        btnAddContact.setVisibility(isFriend ? View.GONE : View.VISIBLE);
        btnSendMsg.setVisibility(isFriend ? View.VISIBLE : View.GONE);
        btnSendVideo.setVisibility(isFriend ? View.VISIBLE : View.GONE);
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
                    Log.e("FriendDetailsActivity", "user.name=" + user.getMUserName());
                    startActivity(new Intent(this, SearchUserActivity.class).putExtra(I.User.TABLE_NAME, user.getMUserName()));

                } else {

                }

                break;
            case R.id.btn_send_msg:
                finish();
                startActivity(new Intent(this, ChatActivity.class).putExtra("userId", user.getMUserName()));
                break;
            case R.id.btn_send_video:
                startVideoCall();
                break;
        }
    }

    protected void startVideoCall() {
        if (!EMClient.getInstance().isConnected())
            Toast.makeText(this, R.string.not_connect_to_server, Toast.LENGTH_SHORT).show();
        else {
            startActivity(new Intent(this, VideoCallActivity.class).putExtra("username", user.getMUserName())
                    .putExtra("isComingCall", false));
            // videoCallBtn.setEnabled(false);
//            inputMenu.hideExtendMenuContainer();
        }
    }
}
