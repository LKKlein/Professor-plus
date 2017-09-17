package cn.edu.swufe.fife.professor.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cn.edu.swufe.fife.professor.R;

public class ProgressFragment extends Fragment {
    private static final String ARG_LOADING = "loading";
    private String mLoadingStr;

    public ProgressFragment() {
    }

    public static ProgressFragment newInstance(String loadingStr) {
        ProgressFragment fragment = new ProgressFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LOADING, loadingStr);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLoadingStr = getArguments().getString(ARG_LOADING);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_progress, container, false);
        TextView text = (TextView) rootView.findViewById(R.id.progress_text);
        if (!TextUtils.isEmpty(mLoadingStr)) {
            text.setText(mLoadingStr);
        }
        return rootView;
    }

}
