package com.rockfield.gmit.projectappfinal;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class HistoryFragment extends Fragment implements View.OnClickListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private Button mPastSevenDays;
    private Button mPastMonth;
    private Button mPastYear;
    private Button mAllData;
    private Button mEnterAmount;

    private EditText mEnterAmountField;
    private TextView mText;

    public HistoryFragment() {
        // Required empty public constructor
    }

    public static HistoryFragment newInstance(String param1, String param2) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_history, container, false);

        mPastSevenDays = view.findViewById(R.id.pastSevenDays);
        mAllData = view.findViewById(R.id.allDataGraph);
        mEnterAmount = view.findViewById(R.id.enterAmount);
        mPastMonth = view.findViewById(R.id.pastMonth);
        mPastYear = view.findViewById(R.id.pastYear);

        mEnterAmountField = view.findViewById(R.id.enterAmountField);
        mText = view.findViewById(R.id.textView);

        mPastSevenDays.setOnClickListener(this);
        mAllData.setOnClickListener(this);
        mEnterAmount.setOnClickListener(this);
        mPastMonth.setOnClickListener(this);
        mPastYear.setOnClickListener(this);

        // Inflate the layout for this fragment
        return view;

    }

    @Override
    public void onClick(View view) {

        Intent intent = new Intent(getActivity(), GraphViewActivity.class);

        switch (view.getId()) {
            case R.id.allDataGraph:

                intent.putExtra("amount", 7);
                startActivity(intent);
                Log.i("HistoryFragment", "onClick:" + view.getId());

                break;
            case R.id.pastSevenDays:

                intent.putExtra("amount", 7);
                startActivity(intent);
                Log.i("HistoryFragment", "onClick:" + view.getId());
                break;

            case R.id.pastMonth:

                intent.putExtra("amount", 32);
                startActivity(intent);
                Log.i("HistoryFragment", "onClick:" + view.getId());
                break;

            case R.id.pastYear:

                intent.putExtra("amount", 365);
                startActivity(intent);
                Log.i("HistoryFragment", "onClick:" + view.getId());
                break;

            case R.id.enterAmount:

                String amount = mEnterAmountField.getText().toString();

                if(TextUtils.isEmpty(amount))
                    Toast.makeText(getActivity(), "Enter a number", Toast.LENGTH_SHORT).show();

                else {
                    int intentAmount = Integer.parseInt(amount);

                    if (intentAmount < 1 || intentAmount > 999)
                        Toast.makeText(getActivity(), "The number must be 1-999", Toast.LENGTH_SHORT).show();

                    else {

                        intent.putExtra("amount", intentAmount);
                        startActivity(intent);
                        Log.i("HistoryFragment", "onClick:" + view.getId());
                    }
                }
                break;
        }
    }
}
