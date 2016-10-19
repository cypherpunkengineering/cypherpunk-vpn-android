package com.cypherpunk.android.vpn.ui.main;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.databinding.ListItemLocationBinding;
import com.cypherpunk.android.vpn.model.Location;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static com.os.operando.garum.utils.Cache.getContext;


public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {

    private List<Location> items = new ArrayList<>();

    protected void onFavorite(String locationId, boolean favorite) {
    }

    protected void onItemClick(Location location) {
    }

    LocationAdapter(List<Location> items) {
        this.items = items;
    }

    @Override
    public LocationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new LocationAdapter.ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_location, parent, false));
    }

    @Override
    public void onBindViewHolder(LocationAdapter.ViewHolder holder, int position) {
        ListItemLocationBinding binding = holder.getBinding();

        final Location item = items.get(position);
        final String id = item.getId();
        binding.location.setText(item.getRegionName());
        binding.favorite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, final boolean b) {
                    onFavorite(id, b);
            }
        });
        binding.favorite.setChecked(item.isFavorited());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClick(item);
            }
        });

        Picasso.with(getContext()).load(item.getNationalFlagUrl()).into(binding.nationalFlag);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addAll(@NonNull List<Location> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private ListItemLocationBinding binding;

        ViewHolder(View view) {
            super(view);
            binding = DataBindingUtil.bind(view);
        }

        public ListItemLocationBinding getBinding() {
            return binding;
        }
    }
}
