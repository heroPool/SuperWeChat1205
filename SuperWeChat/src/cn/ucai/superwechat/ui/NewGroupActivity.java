/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.ucai.superwechat.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMGroupManager.EMGroupOptions;
import com.hyphenate.chat.EMGroupManager.EMGroupStyle;
import com.hyphenate.easeui.domain.User;
import com.hyphenate.easeui.widget.EaseAlertDialog;
import com.hyphenate.exceptions.HyphenateException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatHelper;
import cn.ucai.superwechat.db.GroupModel;
import cn.ucai.superwechat.db.IGroupModel;
import com.hyphenate.easeui.domain.Group;
import cn.ucai.superwechat.net.OnCompleteListener;
import cn.ucai.superwechat.utils.L;
import cn.ucai.superwechat.utils.Result;
import cn.ucai.superwechat.utils.ResultUtils;

import static cn.ucai.superwechat.ui.UserProfileActivity.getAvatarPath;

public class NewGroupActivity extends BaseActivity {
    @BindView(R.id.group_avatar)
    LinearLayout groupAvatar;
    @BindView(R.id.image_groupavatar)
    ImageView imageGroupavatar;
    private EditText groupNameEditText;
    private ProgressDialog progressDialog;
    private EditText introductionEditText;
    private CheckBox publibCheckBox;
    private CheckBox memberCheckbox;
    private TextView secondTextView;
    private static final int REQUESTCODE_PICK = 1;
    private static final int REQUESTCODE_CUTTING = 2;
    IGroupModel model;
    User user;

    File avatarFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_new_group);
        ButterKnife.bind(this);
        model = new GroupModel();
        groupNameEditText = (EditText) findViewById(R.id.edit_group_name);
        introductionEditText = (EditText) findViewById(R.id.edit_group_introduction);
        publibCheckBox = (CheckBox) findViewById(R.id.cb_public);
        memberCheckbox = (CheckBox) findViewById(R.id.cb_member_inviter);
        secondTextView = (TextView) findViewById(R.id.second_desc);
        user = SuperWeChatHelper.getInstance().getUserProfileManager().getAppCurrentUserInfo();
        publibCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    secondTextView.setText(R.string.join_need_owner_approval);
                } else {
                    secondTextView.setText(R.string.Open_group_members_invited);
                }
            }
        });

    }

    /**
     * @param v
     */
    public void save(View v) {
        String name = groupNameEditText.getText().toString();
        if (TextUtils.isEmpty(name)) {
            new EaseAlertDialog(this, R.string.Group_name_cannot_be_empty).show();
        } else {
            // select from contact list
            startActivityForResult(new Intent(this, GroupPickContactsActivity.class).putExtra("groupName", name), I.REQUEST_CODE_PICK_CONTACT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String st1 = getResources().getString(R.string.Is_to_create_a_group_chat);
        final String st2 = getResources().getString(R.string.Failed_to_create_groups);

        if (resultCode == RESULT_OK) {
            //new group

            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(st1);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    final String groupName = groupNameEditText.getText().toString().trim();
                    String desc = introductionEditText.getText().toString();
                    String[] members = data.getStringArrayExtra("newmembers");
                    try {
                        EMGroupOptions option = new EMGroupOptions();
                        option.maxUsers = 200;
                        option.inviteNeedConfirm = true;

                        String reason = NewGroupActivity.this.getString(R.string.invite_join_group);
                        reason = EMClient.getInstance().getCurrentUser() + reason + groupName;

                        if (publibCheckBox.isChecked()) {
                            option.style = memberCheckbox.isChecked() ? EMGroupStyle.EMGroupStylePublicJoinNeedApproval : EMGroupStyle.EMGroupStylePublicOpenJoin;
                        } else {
                            option.style = memberCheckbox.isChecked() ? EMGroupStyle.EMGroupStylePrivateMemberCanInvite : EMGroupStyle.EMGroupStylePrivateOnlyOwnerInvite;
                        }
                        EMGroup emGroup = EMClient.getInstance().groupManager().createGroup(groupName, desc, members, reason, option);

                        createAppGroup(emGroup, members);
                    } catch (final HyphenateException e) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                progressDialog.dismiss();
                                Toast.makeText(NewGroupActivity.this, st2 + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                }
            }).start();


        }
    }

    private void createAppGroup(EMGroup emGroup, final String[] members) {
        if (emGroup != null) {
            File file = null;
            model.newGroup(this, emGroup.getGroupId(), emGroup.getGroupName(), emGroup.getDescription(), emGroup.getOwner(), emGroup.isPublic(), emGroup.isAllowInvites(), avatarFile, new OnCompleteListener<String>() {
                @Override
                public void onSuccess(String s) {
                    boolean success = false;
                    if (s != null) {
                        Result result = ResultUtils.getResultFromJson(s, Group.class);
                        if (result != null && result.isRetMsg()) {
                            Group group = (Group) result.getRetData();

                            if (group != null) {
                                if (members.length > 0) {
                                    addMembers(group.getMGroupHxid(), getMembers(members));

                                } else {
                                    success = true;
                                }
                            }
                        }
                    }
                    if (members.length <= 0) {
                        createSuccess(success);
                    }
                }

                @Override
                public void onError(String error) {
                    createSuccess(false);
                }
            });
        }
    }

    private void addMembers(String hxid, String members) {
        model.addMembers(this, members, hxid, new OnCompleteListener<String>() {
            @Override
            public void onSuccess(String result) {
                boolean success = false;
                if (result != null) {
                    Result resultFromJson = ResultUtils.getResultFromJson(result, Group.class);
                    if (resultFromJson != null && resultFromJson.isRetMsg()) {
                        success = true;
                    }
                }
                createSuccess(success);

            }

            @Override
            public void onError(String error) {
                createSuccess(false);
            }
        });
    }

    private String getMembers(String[] members) {

        String s = Arrays.toString(members).toString();
        StringBuffer str = new StringBuffer();
        for (String member : members) {
            str.append(member).append(",");
        }
        return str.toString();
    }

    private void createSuccess(final boolean success) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                if (success) {
                    setResult(RESULT_OK);
                    finish();

                } else {
                    Toast.makeText(NewGroupActivity.this, "创建群组失败！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setPicToView(Intent picdata) {
        Bundle extras = picdata.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            Drawable drawable = new BitmapDrawable(getResources(), photo);
            imageGroupavatar.setImageDrawable(drawable);
            saveBitmapFile(photo);

        }

    }

    private void saveBitmapFile(Bitmap bitmap) {
        if (bitmap != null) {
            String imagePath = getAvatarPath(NewGroupActivity.this, I.AVATAR_TYPE) + "/" + getAvatarName() + ".jpg";
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
            avatarFile = file;
        }

    }

    String avatarName;

    private String getAvatarName() {
        avatarName = I.AVATAR_TYPE_GROUP_PATH + System.currentTimeMillis();
        return avatarName;
    }

    private void uploadUserAvatar(File file) {

        progressDialog = ProgressDialog.show(this, getString(R.string.dl_update_photo), getString(R.string.dl_waiting));
        SuperWeChatHelper.getInstance().getUserProfileManager().uploadUserAvatar(file);
        progressDialog.show();
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

    public void back(View view) {
        finish();
    }

    @OnClick(R.id.group_avatar)
    public void onViewClicked() {
        Intent pickIntent = new Intent(Intent.ACTION_PICK, null);
        pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(pickIntent, REQUESTCODE_PICK);
    }
}
