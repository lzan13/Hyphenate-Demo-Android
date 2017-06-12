package com.hyphenate.easeui.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chatuidemo.Constant;
import com.hyphenate.chatuidemo.chat.ConversationExtUtils;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.adapter.EaseConversationListAdapter;
import com.hyphenate.easeui.model.EaseUser;
import com.hyphenate.easeui.utils.EaseUserUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by wei on 2016/9/28.
 * Conversation list view, which extends RecyclerView
 */
public class EaseConversationListView extends RecyclerView {
    protected final int MSG_REFRESH_ADAPTER_DATA = 0;

    protected Context mContext;
    protected List<EMConversation> mConversationList;
    protected EaseConversationListAdapter mAdapter;

    protected List<String> mHiddenList;

    public EaseConversationListView(Context context) {
        this(context, null);
    }

    public EaseConversationListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public EaseConversationListView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.mContext = context;
    }

    /**
     * Init this view, which use a default sorted mConversation list.
     * If you want to show list with your own sort, use {@link #init(Comparator)}
     */
    public void init() {
        init(null);
    }

    /**
     * Init list view with the passed Comparator
     *
     * @param comparator
     */
    public void init(Comparator<EMConversation> comparator) {
        mConversationList = loadConversationList();
        //LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        LinearLayoutManager layoutManager = new MyLinearLayoutManager(getContext());
        setLayoutManager(layoutManager);

        mAdapter = new EaseConversationListAdapter(getContext(), mConversationList);
        setAdapter(mAdapter);

        mAdapter.refreshList();
    }

    /**
     * filter mConversation list with passed string
     * @param cs
     */
    public void filter(String cs) {
        if(cs == null)
            cs = "";
        mAdapter.getFilter().filter(cs);
    }

    Handler mHandler = new Handler(){
        @Override public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == MSG_REFRESH_ADAPTER_DATA){
                mAdapter.refreshList();
            }
        }
    };

    /**
     * Refresh conversations list view
     */
    public void refresh() {
        if(mConversationList == null){
            mConversationList = loadConversationList();
            mAdapter = new EaseConversationListAdapter(getContext(), mConversationList);
            setAdapter(mAdapter);
        }else{
            mConversationList.clear();
            mConversationList.addAll(loadConversationList());
            mHandler.sendEmptyMessage(MSG_REFRESH_ADAPTER_DATA);
        }
    }

    /**
     * get list item entity
     * @param position
     */
    public EMConversation getItem(int position){
        return mConversationList.get(position);
    }

    /**
     * set list item onclick listener
     * @param onItemClickListener EaseListItemClickListener
     */
    public void setOnItemClickListener(EaseListItemClickListener onItemClickListener){
        mAdapter.setOnItemClickListener(onItemClickListener);
    }

    /**
     * load mConversation list
     * @return
     */
    protected synchronized List<EMConversation> loadConversationList() {
        // get all conversations
        //Map<String, EMConversation> conversations = EMClient.getInstance().chatManager().getAllConversations();
        //if (mConversationList == null) {
        //    mConversationList = new ArrayList<>(conversations.values());
        //} else {
        //    mConversationList.clear();
        //    mConversationList.addAll(conversations.values());
        //}
        //Iterator iterator = mConversationList.iterator();
        //while (iterator.hasNext()){
        //    EMConversation conversation = (EMConversation) iterator.next();
        //    if(conversation.getAllMessages().size() == 0 || (mHiddenList != null && mHiddenList.contains(conversation.conversationId()))){
        //        //remove the conversation which messages size == 0
        //        iterator.remove();
        //    }
        //}
        //return mConversationList;

        Map<String, EMConversation> conversations =
                EMClient.getInstance().chatManager().getAllConversations();
        if (conversations.containsKey(Constant.CONVERSATION_NAME_APPLY)) {
            conversations.remove(Constant.CONVERSATION_NAME_APPLY);
        }
        List<EMConversation> list = new ArrayList<>();
        list.addAll(conversations.values());
        //Iterator iterator = list.iterator();
        //while (iterator.hasNext()){
        //    EMConversation conversation = (EMConversation) iterator.next();
        //    if(conversation.getAllMessages().size() == 0 || (mHiddenList != null && mHiddenList.contains(conversation.conversationId()))){
        //        //remove the conversation which messages size == 0
        //        iterator.remove();
        //    }
        //}
        // 使用Collectons的sort()方法 对会话列表进行排序
        Collections.sort(list, new Comparator<EMConversation>() {
            @Override public int compare(EMConversation lhs, EMConversation rhs) {
                /**
                 * 根据会话扩展中的时间进行排序
                 * 通过{@link ConversationExtUtils#getConversationLastTime(EMConversation)} 获取时间
                 */
                if (lhs.getLastMessage().getMsgTime()>rhs.getLastMessage().getMsgTime()) {
                    return -1;
                } else if (lhs.getLastMessage().getMsgTime()<rhs.getLastMessage().getMsgTime()) {
                    return 1;
                }
                return 0;
            }
        });
        // 将列表排序之后，要重新将置顶的item设置到顶部
        List<EMConversation> conversationList = new ArrayList<>();
        int count = 0;
        for (int i = 0; i < list.size(); i++) {
            if (ConversationExtUtils.getConversationPushpin(list.get(i))) {
                conversationList.add(count, list.get(i));
                count++;
            } else {
                conversationList.add(list.get(i));
            }
        }
        return conversationList;
    }

    protected synchronized  List<EMConversation> getFilterList(String query){
        List<EMConversation> list = new ArrayList<>();
        Iterator iterator = mConversationList.iterator();
        while (iterator.hasNext()){
            EMConversation conversation = (EMConversation) iterator.next();
            String username = conversation.conversationId();
            EMGroup group = EMClient.getInstance().groupManager().getGroup(username);
            //add group name or user nick
            if(group != null){
                username = group.getGroupName();
            }else{
                EaseUser user = EaseUserUtils.getUserInfo(username);
                if(user != null && user.getEaseNickname() != null)
                    username = user.getEaseNickname();
            }

            if(username.contains(query)){
                list.add(conversation);
            }
        }
        return list;
    }

    /**
     * set a list you want not to show in conversation list
     * @param hiddenList
     */
    public void setHiddenList(List<String> hiddenList){
        mHiddenList = hiddenList;
    }

}
