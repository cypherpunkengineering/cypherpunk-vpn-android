package com.cypherpunk.android.vpn.ui.main;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.databinding.ListItemRegionBinding;
import com.cypherpunk.android.vpn.model.Region;

import java.util.ArrayList;
import java.util.List;

import static com.os.operando.garum.utils.Cache.getContext;


public class RegionAdapter extends RecyclerView.Adapter<RegionAdapter.ViewHolder> {

    private List<Region> items = new ArrayList<>();

    protected void onFavorite(@NonNull String regionId, boolean favorite) {
    }

    protected void onItemClick(@NonNull String regionId) {
    }

    RegionAdapter(List<Region> items) {
        this.items = items;
    }

    @Override
    public RegionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RegionAdapter.ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_region, parent, false));
    }

    @Override
    public void onBindViewHolder(RegionAdapter.ViewHolder holder, int position) {
        ListItemRegionBinding binding = holder.getBinding();

        final Region item = items.get(position);
        final String regionId = item.getId();
        binding.regionName.setText(item.getRegionName());
        binding.favorite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, final boolean b) {
                onFavorite(regionId, b);
            }
        });
        binding.favorite.setChecked(item.isFavorited());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClick(regionId);
            }
        });

        binding.nationalFlag.setImageResource(getFlagDrawableByKey(item.getCountryCode().toLowerCase()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addAll(@NonNull List<Region> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    private int getFlagDrawableByKey(String key) {
        String packageName = getContext().getPackageName();
        return getContext().getResources().getIdentifier("flag_" + key, "drawable", packageName);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private ListItemRegionBinding binding;

        ViewHolder(View view) {
            super(view);
            binding = DataBindingUtil.bind(view);
        }

        public ListItemRegionBinding getBinding() {
            return binding;
        }
    }
}
