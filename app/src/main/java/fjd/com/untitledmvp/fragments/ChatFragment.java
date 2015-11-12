package fjd.com.untitledmvp.fragments;

import android.content.Intent;
import android.database.DataSetObserver;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;

import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;

import fjd.com.untitledmvp.R;
import fjd.com.untitledmvp.helper.ChatListAdapter;
import fjd.com.untitledmvp.models.ChatMessage;
import fjd.com.untitledmvp.models.User;
import fjd.com.untitledmvp.state.GlobalState;
import fjd.com.untitledmvp.util.Constants;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChatFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private ListView mChatListView;
    private android.content.Context mContext = null;
    private Firebase mFBChatRef = null;
    private ChatListAdapter mChatListAdapter;
    private ValueEventListener mConnectedListener;
    private GlobalState mState;

    private static final String ARG_PARAM1 = "myUserName";
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param myUserName Parameter 1.
     * @return A new instance of fragment ChatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatFragment newInstance(String myUserName) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();

        args.putString(ARG_PARAM1, myUserName);
        fragment.setArguments(args);

        return fragment;
    }

    public ChatFragment() {
        // Required empty public constructor
    }

    private void sendMessage(View v) {
        EditText inputText = (EditText) v.findViewById(R.id.messageInput);
        String input = inputText.getText().toString();
        if (!input.equals("") && mState != null && mFBChatRef != null && v != null) {
            // Create our 'model', a Chat object
            ChatMessage chat = new ChatMessage(mState.getCurrFn(), input);
            // Create a new, auto-generated child of that chat location, and save our chat data there
            mFBChatRef.push().setValue(chat);
            inputText.setText("");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(mContext == null){
            mContext = getActivity().getApplicationContext();
        }
        Firebase.setAndroidContext(mContext);
        Intent intent = getActivity().getIntent();
        String convoID = intent.getStringExtra(Constants.CONVO_KEY);

        mFBChatRef = new Firebase(Constants.FBURL)
                .child("messages")
                .child(convoID);
        mState = (GlobalState) mContext;

        HashMap<String,String> serializedUser = (HashMap) intent.getSerializableExtra(Constants.CURR_USER_KEY);
        if(serializedUser != null){
            mState.SetCurrUser(User.FromHashMap(serializedUser));
        }


    }

    @Override
    public void onStart() {
        super.onStart();
        mChatListAdapter = new ChatListAdapter(mFBChatRef.limitToLast(50), this.getActivity(), R.layout.chat_message, mState.getCurrFn());
        mChatListView.setAdapter(mChatListAdapter);
        mChatListAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                mChatListView.setSelection(mChatListAdapter.getCount() - 1);
            }
        });
        // Finally, a little indication of connection status
        mConnectedListener = mFBChatRef.getRoot().child(".info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = (Boolean) dataSnapshot.getValue();
                if (connected) {
                    Toast.makeText(ChatFragment.this.getActivity(), "Connected to Firebase", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ChatFragment.this.getActivity(), "Disconnected from Firebase", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                // No-op
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        mFBChatRef.getRoot().child(".info/connected").removeEventListener(mConnectedListener);
        mChatListAdapter.cleanup();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_chat, container, false);
        mChatListView = (ListView) v.findViewById(R.id.listView);

        EditText inputText = (EditText) v.findViewById(R.id.messageInput);
        inputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_NULL && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    sendMessage(v);
                }
                return true;
            }
        });

        v.findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(v);
            }
        });

        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }


}
