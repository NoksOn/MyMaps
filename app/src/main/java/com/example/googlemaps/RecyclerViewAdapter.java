package com.example.googlemaps;

import android.location.Address;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.Marker;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private List<Address> addresses;
    private ItemClickListener mClickListener;

    public List<Address> getAddresses() {
        return addresses;
    }

    public RecyclerViewAdapter(List<Address> addresses){
        this.addresses = addresses;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_recycler_view_item,viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
         Address address = addresses.get(i);
         if(address.getThoroughfare()!=null && !address.getThoroughfare().equals(address.getFeatureName())){
             viewHolder.Place.setText(address.getThoroughfare()+" "+address.getFeatureName());
         }
         else {
             viewHolder.Place.setText(address.getFeatureName());
         }
         viewHolder.PlacInfo.setText(address.getCountryName()+" "+address.getAdminArea()+" "+address.getLocality());
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView Place,PlacInfo,Distance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            Place = (TextView) itemView.findViewById(R.id.Place);
            PlacInfo = (TextView) itemView.findViewById(R.id.Place_Info);
            Distance = (TextView) itemView.findViewById(R.id.Item_distance);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(mClickListener!=null)
           mClickListener.OnItemClick(v,getAdapterPosition());
        }
    }

    public Address getItem(int position){
        return addresses.get(position);
    }

    public void setClickListener(ItemClickListener itemClickListener){
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener{
        public void OnItemClick(View v,int id);
    }
}
