package com.example.googlemaps;

import android.content.Context;
import android.location.Address;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class SearchFragment extends Fragment implements RecyclerViewAdapter.ItemClickListener {
    private static final String ARG_PARAM1 = "context";
    private static final String ARG_PARAM2 = "object";


    private Context mContext;
    private MainActivity mainActivity;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;

    public static SearchFragment newInstance(Context mContext, MainActivity mainActivity) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, (Serializable) mContext);
        args.putSerializable(ARG_PARAM2,(Serializable)mainActivity);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search,container,false);
        try {
            if (getArguments() != null) {
                mContext = (Context) getArguments().getSerializable(ARG_PARAM1);
                mainActivity = (MainActivity) getArguments().getSerializable(ARG_PARAM2);
            }
        } catch (Exception se){
            Log.d("Exception",se.getMessage());
        }

        recyclerView = (RecyclerView) view.findViewById(R.id.ListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        return view;
    }

    public void CreateRecyclerView(List<Address> addresses){
         adapter = new RecyclerViewAdapter(addresses);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }




    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("Fragment","OnDetach");

        mainActivity.setFragmentIsOpened(false);
        mainActivity.getAddMarker().setVisibility(View.VISIBLE);
    }

    @Override
    public void OnItemClick(View v, int id) {
        //TODO find more than 1
        if (mainActivity.isPointIsCreated()) {
            Log.d("Text", "Last Point changed");
            mainActivity.getMarkers().get(mainActivity.getMarkers().size() - 1)
                    .setPosition(new LatLng(adapter.getItem(id).getLatitude(), adapter.getItem(id).getLongitude()));
            mainActivity.getMapCallback().MoveCamera(mainActivity.getMarkers().get(mainActivity.getMarkers().size() - 1).getPosition());
            mainActivity.setPointIsCreated(false);
            mainActivity.setFragmentIsOpened(false);
            getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_to_down,R.anim.exit_to_up)
                    .remove(this).addToBackStack(null).commit();
        } else {
            Log.d("Text", "Yo didnt create last point it was created)");
            Marker marker = mainActivity.getMapCallback()
                    .addMarker(new LatLng(adapter.getItem(id).getLatitude(), adapter.getItem(id).getLongitude()), true, "My Point");
            mainActivity.getMapCallback().MoveCamera(marker.getPosition());
            mainActivity.getMarkers().add(marker);
        }
    }

}
