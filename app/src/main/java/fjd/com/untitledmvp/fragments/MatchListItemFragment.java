package fjd.com.untitledmvp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import com.firebase.client.Firebase;

import fjd.com.untitledmvp.R;
import fjd.com.untitledmvp.activities.ChatActivity;
import fjd.com.untitledmvp.dummy.DummyContent;
import fjd.com.untitledmvp.helper.MatchListAdapter;
import fjd.com.untitledmvp.util.Constants;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class MatchListItemFragment extends ListFragment {

    private OnFragmentInteractionListener mListener;
    private MatchListAdapter mMatchListAdapter;
    private Firebase mFBRef = null;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MatchListItemFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(getActivity().getApplicationContext());
        mFBRef = new Firebase(Constants.FBURL);

        mMatchListAdapter = new MatchListAdapter(mFBRef, String.class, R.layout.match_item, getActivity());

        // TODO: Change Adapter to display your content
        setListAdapter(mMatchListAdapter);
    }


    @Override
     public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMatchListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        String convoId = (String) v.getTag();
        Intent intent = new Intent(getActivity().getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.CONVO_KEY, convoId);
                startActivity(intent);
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
        }
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
        public void onFragmentInteraction(String id);
    }

}
