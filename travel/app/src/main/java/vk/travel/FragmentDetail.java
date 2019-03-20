package vk.travel;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentDetail extends Fragment {

    Activity mActivity;

    // ON FRAGMENT ATTACHED ------------------------------------------------------------------------
    @Override
    public void onAttach(Context ctx) {                                                             // save context activity when fragment attached
        super.onAttach(ctx);
        mActivity = (Activity) ctx;
    }

    // ON CREATE -----------------------------------------------------------------------------------
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);                                                                    // allows to handle menu item clicks
    }

    // ON CREATE VIEW ------------------------------------------------------------------------------
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_detail, container, false);          // inflate fragment with layout
        return view;                                                                                // view object is ready to return
    }

    // ON VIEW CREATED -----------------------------------------------------------------------------
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

    }

}

