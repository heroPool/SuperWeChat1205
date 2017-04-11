package cn.ucai.superwechat.db;

import android.content.Context;

import java.io.File;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.net.OnCompleteListener;
import cn.ucai.superwechat.utils.OkHttpUtils;


/**
 * Created by Administrator on 2017/4/10.
 */

public class GroupModel implements IGroupModel {
    @Override
    public void newGroup(Context context, String hxid, String groupName, String des, String owner, boolean ispublic, boolean isInvites, File file, OnCompleteListener<String> listener) {
        OkHttpUtils<String> okhttps = new OkHttpUtils<>(context);
        okhttps.setRequestUrl(I.REQUEST_CREATE_GROUP)
                .addParam(I.Group.NAME, groupName)
                .addParam(I.Group.HX_ID, hxid)
                .addParam(I.Group.DESCRIPTION, des)
                .addParam(I.Group.OWNER, owner)
                .addParam(I.Group.IS_PUBLIC, String.valueOf(ispublic))
                .addParam(I.Group.ALLOW_INVITES, String.valueOf(isInvites))
                .addFile2(file)
                .targetClass(String.class)
                .post()
                .execute(listener);

    }

    @Override
    public void addMembers(Context context, String members, String grouphxid, OnCompleteListener<String> listener) {
        OkHttpUtils<String> okhttps = new OkHttpUtils<>(context);
        okhttps.setRequestUrl(I.REQUEST_ADD_GROUP_MEMBERS)
                .addParam(I.Member.USER_NAME, members)
                .addParam(I.Member.GROUP_HX_ID, grouphxid)
                .targetClass(String.class)
                .execute(listener);
    }
}
