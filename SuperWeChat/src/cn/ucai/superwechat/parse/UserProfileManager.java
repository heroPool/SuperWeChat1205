package cn.ucai.superwechat.parse;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.domain.User;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.SuperWeChatHelper;
import cn.ucai.superwechat.SuperWeChatHelper.DataSyncListener;
import cn.ucai.superwechat.net.IUserRegisterModel;
import cn.ucai.superwechat.net.OnCompleteListener;
import cn.ucai.superwechat.net.UserRegisterModel;
import cn.ucai.superwechat.utils.PreferenceManager;
import cn.ucai.superwechat.utils.Result;
import cn.ucai.superwechat.utils.ResultUtils;

public class UserProfileManager {

    /**
     * application context
     */
    protected Context appContext = null;

    /**
     * init flag: test if the sdk has been inited before, we don't need to init
     * again
     */
    private boolean sdkInited = false;

    /**
     * HuanXin sync contact nick and avatar listener
     */
    private List<DataSyncListener> syncContactInfosListeners;

    private boolean isSyncingContactInfosWithServer = false;

    private EaseUser currentUser;
    private User currentAppUser;
    IUserRegisterModel userRegisterModel;


    public UserProfileManager() {
    }

    public synchronized boolean init(Context context) {
        if (sdkInited) {
            return true;
        }
        ParseManager.getInstance().onInit(context);
        syncContactInfosListeners = new ArrayList<DataSyncListener>();
        sdkInited = true;
        userRegisterModel = new UserRegisterModel();
        return true;
    }

    public void addSyncContactInfoListener(DataSyncListener listener) {
        if (listener == null) {
            return;
        }
        if (!syncContactInfosListeners.contains(listener)) {
            syncContactInfosListeners.add(listener);
        }
    }

    public void removeSyncContactInfoListener(DataSyncListener listener) {
        if (listener == null) {
            return;
        }
        if (syncContactInfosListeners.contains(listener)) {
            syncContactInfosListeners.remove(listener);

        }
    }

    public void asyncFetchContactInfosFromServer(List<String> usernames, final EMValueCallBack<List<EaseUser>> callback) {
        if (isSyncingContactInfosWithServer) {
            return;
        }
        isSyncingContactInfosWithServer = true;
        ParseManager.getInstance().getContactInfos(usernames, new EMValueCallBack<List<EaseUser>>() {

            @Override
            public void onSuccess(List<EaseUser> value) {
                isSyncingContactInfosWithServer = false;
                // in case that logout already before server returns,we should
                // return immediately
                if (!SuperWeChatHelper.getInstance().isLoggedIn()) {
                    return;
                }
                if (callback != null) {
                    callback.onSuccess(value);
                }
            }

            @Override
            public void onError(int error, String errorMsg) {
                isSyncingContactInfosWithServer = false;
                if (callback != null) {
                    callback.onError(error, errorMsg);
                }
            }

        });

    }

    public void notifyContactInfosSyncListener(boolean success) {
        for (DataSyncListener listener : syncContactInfosListeners) {
            listener.onSyncComplete(success);
        }
    }

    public boolean isSyncingContactInfoWithServer() {
        return isSyncingContactInfosWithServer;
    }

    public synchronized void reset() {
        isSyncingContactInfosWithServer = false;
        currentUser = null;
        PreferenceManager.getInstance().removeCurrentUserInfo();
    }

    public synchronized EaseUser getCurrentUserInfo() {
        if (currentUser == null) {
            String username = EMClient.getInstance().getCurrentUser();
            currentUser = new EaseUser(username);
            String nick = getCurrentUserNick();
            currentUser.setNick((nick != null) ? nick : username);
            currentUser.setAvatar(getCurrentUserAvatar());
        }
        return currentUser;
    }

    public synchronized User getAppCurrentUserInfo() {
        if (currentUser == null || currentAppUser.getMUserName() == null) {

            String username = EMClient.getInstance().getCurrentUser();
            currentAppUser = new User(username);
            String nick = getCurrentUserNick();
            currentAppUser.setMUserNick((nick != null) ? nick : username);
            currentAppUser.setMAvatarPath(getCurrentUserAvatar());
        }
        return currentAppUser;
    }

    public boolean updateCurrentUserNickName(final String nickname) {
        userRegisterModel.updateUserNick(appContext, EMClient.getInstance().getCurrentUser(), nickname, new OnCompleteListener<String>() {
            @Override
            public void onSuccess(String result) {
                boolean updateNick = false;
                if (result != null) {
                    Result resultFromJson = ResultUtils.getResultFromJson(result, User.class);
                    if (resultFromJson != null && resultFromJson.isRetMsg()) {
                        User user = (User) resultFromJson.getRetData();
                        if (user != null) {
                            updateNick = true;
                            setCurrentAppUserNick(user.getMUserNick());
                            SuperWeChatHelper.getInstance().saveAppContact(user);
                        }
                    }
                }
                appContext.sendBroadcast(new Intent(I.REQUEST_UPDATE_USER_NICK).putExtra(I.User.NICK, updateNick));
            }

            @Override
            public void onError(String error) {
                appContext.sendBroadcast(new Intent(I.REQUEST_UPDATE_USER_NICK).putExtra(I.User.NICK, false));
            }
        });
        return false;
    }

    public void uploadUserAvatar(File file) {

        userRegisterModel.updateAvatar(appContext, EMClient.getInstance().getCurrentUser(), file, new OnCompleteListener<String>() {
            @Override
            public void onSuccess(String result) {
                boolean isSuccess = false;
                if (result != null) {
                    Result resultFromJson = ResultUtils.getResultFromJson(result, User.class);
                    if (resultFromJson != null && resultFromJson.isRetMsg()) {
                        User user = (User) resultFromJson.getRetData();
                        if (user != null) {
                            isSuccess = true;
                            setCurrentAppUserAvatar(user.getAvatar());
                            SuperWeChatHelper.getInstance().saveAppContact(user);
                        }
                    }
                }
                appContext.sendBroadcast(new Intent(I.REQUEST_UPDATE_AVATAR).putExtra(I.Avatar.UPDATE_TIME, isSuccess));
            }

            @Override
            public void onError(String error) {
                appContext.sendBroadcast(new Intent(I.REQUEST_UPDATE_AVATAR).putExtra(I.Avatar.UPDATE_TIME, false));
            }
        });
    }

    public void asyncGetAppCurrentUserInfo() {
        userRegisterModel.loadUserInfo(appContext, EMClient.getInstance().getCurrentUser(), new OnCompleteListener<String>() {
            @Override
            public void onSuccess(String result) {
                if (result != null) {
                    Result resultFromJson = ResultUtils.getResultFromJson(result, User.class);
                    if (resultFromJson != null && resultFromJson.isRetMsg()) {
                        User user = (User) resultFromJson.getRetData();
                        Log.i("UserProfileManager", user.toString());
                        if (user != null) {
                            currentAppUser = user;
                        }
                        setCurrentAppUserNick(user.getMUserNick());
                        setCurrentAppUserAvatar(user.getAvatar());
                        SuperWeChatHelper.getInstance().saveAppContact(user);
                    }
                }
            }

            @Override
            public void onError(String error) {

            }
        });
    }

    public void asyncGetCurrentUserInfo() {
        ParseManager.getInstance().asyncGetCurrentUserInfo(new EMValueCallBack<EaseUser>() {

            @Override
            public void onSuccess(EaseUser value) {
                if (value != null) {
                    setCurrentUserNick(value.getNick());
                    setCurrentUserAvatar(value.getAvatar());
                }
            }

            @Override
            public void onError(int error, String errorMsg) {

            }
        });

    }

    public void asyncGetUserInfo(final String username, final EMValueCallBack<EaseUser> callback) {
        ParseManager.getInstance().asyncGetUserInfo(username, callback);
    }

    private void setCurrentUserNick(String nickname) {
        getCurrentUserInfo().setNick(nickname);
        PreferenceManager.getInstance().setCurrentUserNick(nickname);
    }

    private void setCurrentAppUserNick(String nickname) {
        getCurrentUserInfo().setAvatar(nickname);
        PreferenceManager.getInstance().setCurrentUserNick(nickname);

    }

    private void setCurrentAppUserAvatar(String avatar) {
        getCurrentUserInfo().setAvatar(avatar);
        PreferenceManager.getInstance().setCurrentUserAvatar(avatar);
    }

    private void setCurrentUserAvatar(String avatar) {
        getCurrentUserInfo().setAvatar(avatar);
        PreferenceManager.getInstance().setCurrentUserAvatar(avatar);
    }

    private String getCurrentUserNick() {
        return PreferenceManager.getInstance().getCurrentUserNick();
    }

    private String getCurrentUserAvatar() {
        return PreferenceManager.getInstance().getCurrentUserAvatar();
    }

}
