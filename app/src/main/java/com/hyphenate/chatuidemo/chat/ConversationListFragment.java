package com.hyphenate.chatuidemo.chat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chatuidemo.Constant;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.apply.ApplyActivity;
import com.hyphenate.chatuidemo.ui.MainActivity;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.widget.EaseConversationListView;
import com.hyphenate.easeui.widget.EaseListItemClickListener;
import com.hyphenate.util.NetUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.hyphenate.easeui.EaseConstant.CHATTYPE_GROUP;

/**
 * A fragment which shows mConversation list
 */
public class ConversationListFragment extends Fragment {

    private Unbinder mUnbinder;
    private int mItemLongClickPos;

    @BindView(R.id.list_view) EaseConversationListView mConversationListView;
    @BindView(R.id.layout_disconnected_indicator) LinearLayout mIndicatorLayout;
    @BindView(R.id.tv_connect_errormsg) TextView mDisconnectErrorView;

    public ConversationListFragment() {
        // Required empty public constructor
    }

    public static ConversationListFragment newInstance() {
        ConversationListFragment fragment = new ConversationListFragment();
        return fragment;
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.em_fragment_conversation_list, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        EMClient.getInstance().addConnectionListener(connectionListener);

        // init ConversationListView
        mConversationListView.init();
        List<String> hiddenList = new ArrayList<>();
        hiddenList.add(Constant.CONVERSATION_NAME_APPLY);
        mConversationListView.setHiddenList(hiddenList);

        mConversationListView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override public void onCreateContextMenu(ContextMenu menu, View v,
                    ContextMenu.ContextMenuInfo menuInfo) {
                getActivity().getMenuInflater().inflate(R.menu.em_delete_conversation, menu);
            }
        });
        // set item click listener
        mConversationListView.setOnItemClickListener(new EaseListItemClickListener() {
            @Override public void onItemClick(View view, int position) {
                EMConversation conversation = mConversationListView.getItem(position);
                if (conversation.conversationId().equals(Constant.CONVERSATION_NAME_APPLY)) {
                    startActivity(new Intent(getActivity(), ApplyActivity.class));
                } else {
                    //enter to chat activity
                    if(conversation.getType() == EMConversation.EMConversationType.GroupChat){
                        startActivity(new Intent(getActivity(), ChatActivity.class)
                                .putExtra(Constant.EXTRA_USER_ID, conversation.conversationId())
                                .putExtra(EaseConstant.EXTRA_CHAT_TYPE, CHATTYPE_GROUP));
                    }else if(conversation.getType() == EMConversation.EMConversationType.Chat){
                        startActivity(new Intent(getActivity(), ChatActivity.class).putExtra(
                                Constant.EXTRA_USER_ID, conversation.conversationId()));
                    }
                }
            }

            @Override public void onItemLongClick(View view, int position) {
                mItemLongClickPos = position;
                itemLongClick();
            }
        });
    }
    /**
     * Item项长按事件
     */
    public void itemLongClick() {
        final EMConversation conversation = mConversationListView.getItem(mItemLongClickPos);
        final boolean isTop = ConversationExtUtils.getConversationPushpin(conversation);
        // 根据当前会话不同的状态来显示不同的长按菜单
        List<String> menuList = new ArrayList<String>();
        if (isTop) {
            menuList.add("Conversation pushpin cancel");
        } else {
            menuList.add("Conversation pushpin");
        }
        menuList.add("Delete conversation");

        String[] menus = new String[menuList.size()];
        menuList.toArray(menus);

        // 创建并显示 ListView 的长按弹出菜单，并设置弹出菜单 Item的点击监听
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        // 弹出框标题
        // alertDialogBuilder.setTitle(R.string.dialog_title_conversation);
        alertDialogBuilder.setItems(menus, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        // 根据当前状态设置会话是否置顶
                        if (isTop) {
                            ConversationExtUtils.setConversationPushpin(conversation, false);
                        } else {
                            ConversationExtUtils.setConversationPushpin(conversation, true);
                        }
                        refresh();
                        break;
                    case 1:
                        // 删除当前会话，第二个参数表示是否删除此会话的消息
                        EMClient.getInstance()
                                .chatManager()
                                .deleteConversation(conversation.conversationId(), false);
                        refresh();
                        break;
                }
            }
        });
        alertDialogBuilder.show();
    }

    protected EMConnectionListener connectionListener = new EMConnectionListener() {

        @Override
        public void onDisconnected(final int error) {
            if (error == EMError.USER_REMOVED || error == EMError.USER_LOGIN_ANOTHER_DEVICE) {

            } else {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        mIndicatorLayout.setVisibility(View.VISIBLE);
                        if (NetUtils.hasNetwork(getActivity())){
                            mDisconnectErrorView.setText(R.string.can_not_connect_chat_server_connection);
                        } else {
                            mDisconnectErrorView.setText(R.string.current_network_unavailable);
                        }
                    }

                });
            }
        }

        @Override
        public void onConnected() {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    mIndicatorLayout.setVisibility(View.GONE);
                }

            });
        }
    };

    @Override public void onResume() {
        super.onResume();
        //refresh list
        mConversationListView.refresh();
    }

    public void refresh() {
        mConversationListView.refresh();
    }

    /**
     * filter Conversation list with passed query string
     */
    public void filter(String str) {
        mConversationListView.filter(str);
    }

    @Override public void onDestroy() {
        super.onDestroy();
        EMClient.getInstance().removeConnectionListener(connectionListener);
        mUnbinder.unbind();
    }
}
