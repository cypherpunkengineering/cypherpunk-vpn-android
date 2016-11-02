package com.cypherpunk.privacy.ui.main;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.databinding.ListItemRegionBinding;
import com.cypherpunk.privacy.model.Region;

import java.util.ArrayList;
import java.util.List;

import static com.os.operando.garum.utils.Cache.getContext;


public class RegionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_VIEW_TYPE_ITEM = 1;
    private static final int ITEM_VIEW_TYPE_DIVIDER = 2;
    private static final int ITEM_VIEW_TYPE_FASTEST_LOCATION = 3;

    private List<Object> items = new ArrayList<>();

    protected void onFavorite(@NonNull String regionId, boolean favorite) {
    }

    protected void onItemClick(@NonNull String regionId) {
    }

    protected void onFastestLocationClick() {
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_VIEW_TYPE_ITEM:
                return new ViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_region, parent, false));
            case ITEM_VIEW_TYPE_FASTEST_LOCATION:
                return new DividerViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_region_fastest_location, parent, false));
            default:
                return new DividerViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_region_divider, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final int viewType = holder.getItemViewType();
        if (viewType == ITEM_VIEW_TYPE_ITEM) {

            ViewHolder itemHolder = (ViewHolder) holder;
            ListItemRegionBinding binding = itemHolder.getBinding();
            final Region item = (Region) items.get(position);
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
        } else if (viewType == ITEM_VIEW_TYPE_FASTEST_LOCATION) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onFastestLocationClick();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof Region) {
            return ITEM_VIEW_TYPE_ITEM;
        }
        if (items.get(position) instanceof FastestLocation) {
            return ITEM_VIEW_TYPE_FASTEST_LOCATION;
        }
        return ITEM_VIEW_TYPE_DIVIDER;
    }

    public void addRegionList(@NonNull List<Region> favoriteItems,
                              @NonNull List<Region> recentlyConnectedItems,
                              @NonNull List<Region> otherItems) {
        clear();
        addFavoriteItems(favoriteItems);
        addRecentlyConnectedItems(recentlyConnectedItems);
        addAllItems(otherItems);
        notifyItemRangeInserted(0, items.size());
    }

    private void addFavoriteItems(@NonNull List<Region> data) {
        items.add(new FastestLocation());
        items.addAll(data);
        if (!data.isEmpty()) {
            items.add(new FavoriteDivider());
        }
    }

    private void addRecentlyConnectedItems(List<Region> data) {
        items.addAll(data);
        if (!data.isEmpty()) {
            items.add(new ConnectedDivider());
        }
    }

    private void addAllItems(@NonNull List<Region> data) {
        items.addAll(data);
    }

    public void clear() {
        int size = items.size();
        items.clear();
        notifyItemRangeRemoved(0, size);
    }

    private int getFlagDrawableByKey(String key) {
        String packageName = getContext().getPackageName();
        return getContext().getResources().getIdentifier("flag_" + key, "drawable", packageName);
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        private ListItemRegionBinding binding;

        ViewHolder(View view) {
            super(view);
            binding = DataBindingUtil.bind(view);
        }

        public ListItemRegionBinding getBinding() {
            return binding;
        }
    }

    private static class DividerViewHolder extends RecyclerView.ViewHolder {

        DividerViewHolder(View view) {
            super(view);
        }
    }

    private class FavoriteDivider {
    }

    private class ConnectedDivider {
    }

    private class FastestLocation {
    }
}
