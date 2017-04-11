package cn.ucai.superwechat.ui;

import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.domain.User;
import com.hyphenate.easeui.utils.EaseUserUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatHelper;
import cn.ucai.superwechat.utils.L;

public class UserProfileActivity extends BaseActivity {

    private static final int REQUESTCODE_PICK = 1;
    private static final int REQUESTCODE_CUTTING = 2;
    @BindView(R.id.user_head_avatar)
    ImageView userHeadAvatar;
    @BindView(R.id.user_head_headphoto_update)
    ImageView userHeadHeadphotoUpdate;
    @BindView(R.id.user_username)
    TextView userUsername;
    @BindView(R.id.layout_myavatar)
    RelativeLayout layoutMyavatar;
    @BindView(R.id.text_nick)
    TextView textNick;
    @BindView(R.id.layout_nick)
    RelativeLayout layoutNick;
    @BindView(R.id.text_wechat_num)
    TextView textWechatNum;
    @BindView(R.id.layout_wechat_num)
    RelativeLayout layoutWechatNum;
    @BindView(R.id.layout_qrcode)
    RelativeLayout layoutQrcode;
    @BindView(R.id.layout_myaddress)
    RelativeLayout layoutMyaddress;
    @BindView(R.id.layout_usersex)
    RelativeLayout layoutUsersex;
    @BindView(R.id.layout_area)
    RelativeLayout layoutArea;
    @BindView(R.id.layout_sign)
    RelativeLayout layoutSign;
    UpdateNickReceiver receiver;
    UpdateAvatarReceiver avatarreceiver;
    String avatarName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_user_profile);
        ButterKnife.bind(this);
        initView();
        initData();
        initBroadcast();
//        initListener();
    }

    private void initBroadcast() {
        receiver = new UpdateNickReceiver();
        IntentFilter intentFilter = new IntentFilter(I.REQUEST_UPDATE_USER_NICK);
        registerReceiver(receiver, intentFilter);

        avatarreceiver = new UpdateAvatarReceiver();
        IntentFilter intentFilter1 = new IntentFilter(I.REQUEST_UPDATE_AVATAR);
        registerReceiver(avatarreceiver, intentFilter1);
    }

    private void initView() {

    }

    User user;

    private void initData() {
        user = SuperWeChatHelper.getInstance().getUserProfileManager().getAppCurrentUserInfo();
        if (user == null) {
            finish();
        } else {
            showUserInfo();
        }
    }

    private void showUserInfo() {
        textWechatNum.setText(user.getMUserName());
        EaseUserUtils.setAppUserNick(user.getMUserName(), textNick);
        EaseUserUtils.setAppUserAvatar(this, user.getMUserName(), userHeadAvatar);
    }

//    private void initListener() {
//        Intent intent = getIntent();
//        String username = intent.getStringExtra("username");
//        boolean enableUpdate = intent.getBooleanExtra("setting", false);
//        if (enableUpdate) {
//            headPhotoUpdate.setVisibility(View.VISIBLE);
//            iconRightArrow.setVisibility(View.VISIBLE);
//            rlNickName.setOnClickListener(this);
//            headAvatar.setOnClickListener(this);
//        } else {
//            headPhotoUpdate.setVisibility(View.GONE);
//            iconRightArrow.setVisibility(View.INVISIBLE);
//        }
//        if (username != null) {
//            if (username.equals(EMClient.getInstance().getCurrentUser())) {
//                tvUsername.setText(EMClient.getInstance().getCurrentUser());
//                EaseUserUtils.setAppUserNick(username, tvNickName);
//                EaseUserUtils.setAppUserAvatar(this, username, headAvatar);
//            } else {
//                tvUsername.setText(username);
//                EaseUserUtils.setAppUserNick(username, tvNickName);
//                EaseUserUtils.setAppUserAvatar(this, username, headAvatar);
//                asyncFetchUserInfo(username);
//            }
//        }
//    }

    @OnClick({R.id.user_head_avatar, R.id.user_head_headphoto_update, R.id.layout_myavatar, R.id.layout_nick, R.id.layout_wechat_num, R.id.layout_qrcode, R.id.layout_myaddress, R.id.layout_usersex, R.id.layout_area, R.id.layout_sign})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.user_head_avatar:
                break;
            case R.id.user_head_headphoto_update:
                break;
            case R.id.layout_myavatar:
                uploadHeadPhoto();
                break;
            case R.id.layout_nick:
                updateNick();
                break;
            case R.id.layout_wechat_num:
                Toast.makeText(this, "微信号不可修改哦~", Toast.LENGTH_SHORT).show();
                break;
            case R.id.layout_qrcode:
                break;
            case R.id.layout_myaddress:
                break;
            case R.id.layout_usersex:
                break;
            case R.id.layout_area:
                break;
            case R.id.layout_sign:
                break;
        }
    }

    private void updateNick() {
        final EditText editText = new EditText(this);
        editText.setText(user.getMUserNick());
        editText.setSelectAllOnFocus(true);

        new Builder(this).setTitle(R.string.setting_nickname).setIcon(android.R.drawable.ic_dialog_info).setView(editText)
                .setPositiveButton(R.string.dl_ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String nickString = editText.getText().toString();
                        if (TextUtils.isEmpty(nickString)) {
                            Toast.makeText(UserProfileActivity.this, getString(R.string.toast_nick_not_isnull), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (nickString.equals(user.getMUserNick())) {
                            Toast.makeText(UserProfileActivity.this, getString(R.string.toast_nick_not_isnull) + "", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        updateRemoteNick(nickString);
                    }
                }).setNegativeButton(R.string.dl_cancel, null).show();
    }


    public void asyncFetchUserInfo(String username) {
        SuperWeChatHelper.getInstance().getUserProfileManager().asyncGetUserInfo(username, new EMValueCallBack<EaseUser>() {

            @Override
            public void onSuccess(EaseUser user) {
                if (user != null) {
                    SuperWeChatHelper.getInstance().saveContact(user);
                    if (isFinishing()) {
                        return;
                    }
                    textNick.setText(user.getNick());

                    if (!TextUtils.isEmpty(user.getAvatar())) {
                        Glide.with(UserProfileActivity.this).load(user.getAvatar()).placeholder(R.drawable.em_default_avatar).into(userHeadAvatar);
                    } else {
                        Glide.with(UserProfileActivity.this).load(R.drawable.em_default_avatar).into(userHeadAvatar);
                    }
                }
            }

            @Override
            public void onError(int error, String errorMsg) {
            }
        });
    }


    private void uploadHeadPhoto() {
        Builder builder = new Builder(this);
        builder.setTitle(R.string.dl_title_upload_photo);
        builder.setItems(new String[]{getString(R.string.dl_msg_take_photo), getString(R.string.dl_msg_local_upload)},
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        switch (which) {
                            case 0:
                                Toast.makeText(UserProfileActivity.this, getString(R.string.toast_no_support),
                                        Toast.LENGTH_SHORT).show();
                                break;
                            case 1:
                                Intent pickIntent = new Intent(Intent.ACTION_PICK, null);
                                pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                                startActivityForResult(pickIntent, REQUESTCODE_PICK);
                                break;
                            default:
                                break;
                        }
                    }
                });
        builder.create().show();
    }

    ProgressDialog dialog;

    private void updateRemoteNick(final String nickName) {
        dialog = ProgressDialog.show(this, getString(R.string.dl_update_nick), getString(R.string.dl_waiting));
        SuperWeChatHelper.getInstance().getUserProfileManager().updateCurrentUserNickName(nickName);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUESTCODE_PICK:
                if (data == null || data.getData() == null) {
                    return;
                }
                startPhotoZoom(data.getData());
                break;
            case REQUESTCODE_CUTTING:
                if (data != null) {
                    setPicToView(data);
                }
                break;
            default:
                break;
        }

    }

    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("return-data", true);
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, REQUESTCODE_CUTTING);
    }

    /**
     * save the picture data
     *
     * @param picdata
     */
    private void setPicToView(Intent picdata) {
        Bundle extras = picdata.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            Drawable drawable = new BitmapDrawable(getResources(), photo);
            userHeadAvatar.setImageDrawable(drawable);
            uploadUserAvatar(saveBitmapFile(photo));
        }

    }

    private File saveBitmapFile(Bitmap bitmap) {
        if (bitmap != null) {
            String imagePath = getAvatarPath(UserProfileActivity.this, I.AVATAR_TYPE) + "/" + getAvatarName() + ".jpg";
            File file = new File(imagePath);//将要保存图片的路径
            L.e("file path=" + file.getAbsolutePath());
            try {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                bos.flush();
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return file;
        }
        return null;
    }

    private String getAvatarName() {
        avatarName = user.getMUserName() + System.currentTimeMillis();
        return avatarName;
    }

    public static String getAvatarPath(Context context, String path) {
        File dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File folder = new File(dir, path);
        if (!folder.exists()) {
            folder.mkdir();
        }
        return folder.getAbsolutePath();
    }

    private void uploadUserAvatar(File file) {
        dialog = ProgressDialog.show(this, getString(R.string.dl_update_photo), getString(R.string.dl_waiting));
        SuperWeChatHelper.getInstance().getUserProfileManager().uploadUserAvatar(file);
        dialog.show();
    }

    class UpdateNickReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean success = intent.getBooleanExtra(I.User.NICK, false);
            updateNickView(success);

        }
    }

    class UpdateAvatarReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isSuccess = intent.getBooleanExtra(I.Avatar.UPDATE_TIME, false);
            updateAvatarView(isSuccess);
        }
    }

    private void updateAvatarView(boolean isSuccess) {
        dialog.dismiss();
        if (isSuccess) {
            Toast.makeText(this, "修改头像成功！", Toast.LENGTH_SHORT).show();
            user = SuperWeChatHelper.getInstance().getUserProfileManager().getAppCurrentUserInfo();
            EaseUserUtils.setAppUserAvatar(UserProfileActivity.this, user.getMUserName(), userHeadAvatar);
        } else {
            Toast.makeText(this, "修改头像失败!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateNickView(boolean success) {
        if (!success) {
            Toast.makeText(this, getString(R.string.toast_updatenick_fail), Toast.LENGTH_SHORT).show();
            dialog.dismiss();

        } else {
            dialog.dismiss();
            Toast.makeText(this, "修改昵称成功", Toast.LENGTH_SHORT).show();
            user = SuperWeChatHelper.getInstance().getUserProfileManager().getAppCurrentUserInfo();
            textNick.setText(user.getMUserNick());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);

        }
        if (avatarreceiver != null) {
            unregisterReceiver(avatarreceiver);

        }
    }
}
