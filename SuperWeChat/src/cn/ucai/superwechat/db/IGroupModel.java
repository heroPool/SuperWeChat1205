package cn.ucai.superwechat.db;

import android.content.Context;

import java.io.File;

import cn.ucai.superwechat.net.OnCompleteListener;

/**
 * Created by Administrator on 2017/4/10.
 */

public interface IGroupModel {
    void newGroup(Context context, String hxid,String groupName, String des, String owner, boolean ispublic, boolean isInvites, File file, OnCompleteListener<String> listener);

    void addMembers(Context context, String members, String grouphxid, OnCompleteListener<String> listener);

}
