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
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatHelper;
import cn.ucai.superwechat.net.IUserRegisterModel;
import cn.ucai.superwechat.net.OnCompleteListener;
import cn.ucai.superwechat.net.UserRegisterModel;
import cn.ucai.superwechat.utils.CommonUtils;
import cn.ucai.superwechat.utils.MD5;
import cn.ucai.superwechat.utils.Result;
import cn.ucai.superwechat.utils.ResultUtils;

/**
 * register screen
 */
public class RegisterActivity extends BaseActivity {
    @BindView(R.id.image_login_luancher)
    ImageView imageLoginLuancher;
    @BindView(R.id.iv_nick)
    ImageView ivNick;
    @BindView(R.id.nick)
    EditText userNickEditText;
    @BindView(R.id.iv_username)
    ImageView ivUsername;
    @BindView(R.id.username)
    EditText usernameEditText;
    @BindView(R.id.iv_password)
    ImageView ivPassword;
    @BindView(R.id.password)
    EditText passwordEditText;
    @BindView(R.id.iv_password2)
    ImageView ivPassword2;
    @BindView(R.id.confirm_password)
    EditText confirmPwdEditText;

    IUserRegisterModel usergisterModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_register);
        ButterKnife.bind(this);

        usergisterModel = new UserRegisterModel();
    }

    private static final String TAG = "RegisterActivity";

    public void register(View view) {
        final String usernick = userNickEditText.getText().toString().trim();
        final String username = usernameEditText.getText().toString().trim();
        final String pwd = passwordEditText.getText().toString().trim();
        final String password = MD5.getMessageDigest(pwd);
        String confirm_pwd = confirmPwdEditText.getText().toString().trim();

        if (TextUtils.isEmpty(usernick)) {
            CommonUtils.showShortToast("昵称为空！");
            userNickEditText.requestFocus();
            return;
        } else if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, getResources().getString(R.string.User_name_cannot_be_empty), Toast.LENGTH_SHORT).show();
            usernameEditText.requestFocus();
            return;
        } else if (TextUtils.isEmpty(pwd)) {
            Toast.makeText(this, getResources().getString(R.string.Password_cannot_be_empty), Toast.LENGTH_SHORT).show();
            passwordEditText.requestFocus();
            return;
        } else if (TextUtils.isEmpty(confirm_pwd)) {
            Toast.makeText(this, getResources().getString(R.string.Confirm_password_cannot_be_empty), Toast.LENGTH_SHORT).show();
            confirmPwdEditText.requestFocus();
            return;
        } else if (!pwd.equals(confirm_pwd)) {
            Toast.makeText(this, getResources().getString(R.string.Two_input_password), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(pwd)) {
            final ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage(getResources().getString(R.string.Is_the_registered));
            pd.show();

            //进行superwechat端注册
            usergisterModel.register(RegisterActivity.this, username, usernick, password, new OnCompleteListener<String>() {
                @Override
                public void onSuccess(String result) {
                    if (result != null) {
                        Result resultFromJson = ResultUtils.getResultFromJson(result, Result.class);
                        if (resultFromJson.isRetMsg()) {
                            Log.i(TAG, "超级微信注册，" + resultFromJson.toString());
                            //进行环信注册
                            EMCClientregister(username, password, pd);

                        } else if (resultFromJson.getRetCode() == I.MSG_REGISTER_USERNAME_EXISTS) {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.User_already_exists), Toast.LENGTH_SHORT).show();

                        } else if (resultFromJson.getRetCode() == I.MSG_REGISTER_FAIL) {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.Registration_failed), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.Registration_failed), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();

                }
            });
        }
    }

    private void EMCClientregister(final String username, final String password, final ProgressDialog pd) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // call method in SDK
                    EMClient.getInstance().createAccount(username, password);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!RegisterActivity.this.isFinishing())
                                pd.dismiss();
                            // save current user
                            SuperWeChatHelper.getInstance().setCurrentUserName(username);
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.Registered_successfully), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });


                } catch (final HyphenateException e) {

                    //环信注册失败，超级微信端取消注册
                    SuperWeChatClientungister(username);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!RegisterActivity.this.isFinishing())
                                Log.i(TAG, "环信注册失败:" + e.getErrorCode());
                            pd.dismiss();
                            int errorCode = e.getErrorCode();
                            if (errorCode == EMError.NETWORK_ERROR) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_anomalies), Toast.LENGTH_SHORT).show();
                            } else if (errorCode == EMError.USER_ALREADY_EXIST) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.User_already_exists), Toast.LENGTH_SHORT).show();
                            } else if (errorCode == EMError.USER_AUTHENTICATION_FAILED) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.registration_failed_without_permission), Toast.LENGTH_SHORT).show();
                            } else if (errorCode == EMError.USER_ILLEGAL_ARGUMENT) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.illegal_user_name), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.Registration_failed), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                }
            }
        }).start();

    }

    private void SuperWeChatClientungister(String username) {
        usergisterModel.unregister(RegisterActivity.this, username, new OnCompleteListener<String>() {
            @Override
            public void onSuccess(String result) {
                Toast.makeText(RegisterActivity.this, "超级微信取消注册", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {

            }
        });
    }


    public void back(View view) {
        finish();
    }

}
