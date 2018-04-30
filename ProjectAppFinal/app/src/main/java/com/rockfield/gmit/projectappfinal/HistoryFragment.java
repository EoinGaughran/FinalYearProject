package com.rockfield.gmit.projectappfinal;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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


/**
 * A simple {@link //Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link //HistoryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link //HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Button mPastSevenDays;
    private Button mPastMonth;
    private Button mPastYear;
    private Button mAllData;
    private Button mEnterAmount;

    private EditText mEnterAmountField;
    private TextView mText;

    //private OnFragmentInteractionListener mListener;

    public HistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HistoryFragment.
     */
    // TODO: Rename and change types and number of parameters
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

        //initUI();
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

    public void initUI(){

        Button allData = (Button) getActivity().findViewById(R.id.allDataGraph);

        allData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                Intent intent = new Intent(getActivity(), GraphViewActivity.class);
                startActivity(intent);
            }
        });
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
    /*
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
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
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    /*public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }*/
}
