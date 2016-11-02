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

    public void addFavoriteItems(@NonNull List<Region> data) {
        items.add(new FastestLocation());
        items.addAll(data);
        if (!data.isEmpty()) {
            items.add(new FavoriteDivider());
            notifyItemRangeInserted(0, items.size());
        }
    }

    public void addRecentlyConnectedItems(ArrayList<Region> data) {
        items.addAll(data);
        if (!data.isEmpty()) {
            items.add(new ConnectedDivider());
            notifyItemRangeInserted(0, items.size());
        }
    }

    public void addFavoriteItem(@NonNull Region data) {
        for (int i = 0; i < items.size(); i++) {
            Object item = items.get(i);
            if (item instanceof Region) {
                Region item1 = (Region) item;
                if (data.getId().equals(item1.getId())) {
                    items.remove(i);
                    int favoritePosition = getFavoritePosition();
                    if (favoritePosition == -1) {
                        items.add(0, new FavoriteDivider());
                        notifyItemInserted(0);
                        favoritePosition = 0;
                    }
                    items.add(favoritePosition, data);
                    notifyItemMoved(i, favoritePosition);
                    break;
                }
            }
        }
    }

    public void addConnectedItem(@NonNull Region data) {
        if (data.isFavorited()) {
            for (int i = 0; i < items.size(); i++) {
                Object item = items.get(i);
                if (item instanceof Region) {
                    Region item1 = (Region) item;
                    if (data.getId().equals(item1.getId())) {
                        items.remove(i);
                        items.add(0, data);
                        notifyItemMoved(i, 0);
                        return;
                    }
                }
            }
        } else {
            for (int i = 0; i < items.size(); i++) {
                Object item = items.get(i);
                if (item instanceof Region) {
                    Region item1 = (Region) item;
                    if (data.getId().equals(item1.getId())) {
                        items.remove(i);
                        int connectedPosition = getConnectedPosition();
                        if (connectedPosition == -1) {
                            items.add(getFavoritePosition() != -1 ? getFavoritePosition() : 0, new ConnectedDivider());
                            notifyItemInserted(getFavoritePosition() != -1 ? getFavoritePosition() : 0);
                            connectedPosition = 0;
                        }
                        items.add(connectedPosition, data);
                        notifyItemMoved(i, connectedPosition);
                        break;
                    }
                }
            }
        }
    }


    public void removeFavoriteItem(@NonNull Region data) {
        for (int i = 0; i < items.size(); i++) {
            Object item = items.get(i);
            if (item instanceof Region) {
                Region item1 = (Region) item;
                if (data.getId().equals(item1.getId())) {
                    items.remove(i);
                    notifyItemRemoved(i);
                    items.add(data);
                    notifyItemInserted(items.size());
                    break;
                }
            }
        }
    }

    private int getFavoritePosition() {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) instanceof FavoriteDivider) {
                return i;
            }
        }
        return -1;
    }

    private int getConnectedPosition() {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) instanceof ConnectedDivider) {
                return i;
            }
        }
        return -1;
    }


    public void addAllItems(@NonNull List<Region> data) {
        int size = items.size();
        items.addAll(data);
        notifyItemRangeInserted(size, data.size());
    }

    public void clear() {
        items.clear();
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

    static class DividerViewHolder extends RecyclerView.ViewHolder {

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
