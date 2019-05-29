package com.example.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * <pre>
 *     author : summerly
 *     e-mail : yangbinyhbn@gmail.com
 *     time   : 2017/6/29
 *     desc   :
 * </pre>
 */

public class UserGuideFragment extends Fragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_PREVIOUS = "previous";
    private static final String ARG_NEXT = "next";

    private String title;
    private String previous;
    private String next;

    private OnGuideFragmentInteractionListener interactionListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnGuideFragmentInteractionListener) {
            interactionListener = (OnGuideFragmentInteractionListener) context;
        } else {
            throw new RuntimeException("activity must implement OnGuideFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        title = bundle.getString(ARG_TITLE);
        previous = bundle.getString(ARG_PREVIOUS);
        next = bundle.getString(ARG_NEXT);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        @SuppressLint("InflateParams") View root = inflater.inflate(R.layout.fragment_guide, null);
        TextView textTitle = (TextView) root.findViewById(R.id.textTitle);
        textTitle.setText(title);

        Button buttonNext = (Button) root.findViewById(R.id.buttonNext);
        buttonNext.setText(next);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                interactionListener.onNext();
            }
        });

        Button buttonPrevious = (Button) root.findViewById(R.id.buttonPrevious);
        buttonPrevious.setText(previous);
        buttonPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                interactionListener.onPrevious();
            }
        });

        return root;
    }

    public static UserGuideFragment newInstance(String title, String previous, String next) {
        Bundle args = new Bundle();
        UserGuideFragment fragment = new UserGuideFragment();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_PREVIOUS, previous);
        args.putString(ARG_NEXT, next);
        fragment.setArguments(args);
        return fragment;
    }

    interface OnGuideFragmentInteractionListener {
        void onNext();

        void onPrevious();
    }
}
