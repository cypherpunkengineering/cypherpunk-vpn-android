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
    private static final int ITEM_VIEW_TYPE_FAVORITE_ITEM = 3;
    private static final int ITEM_VIEW_TYPE_DIVIDER = 2;

    private List<Region> favoriteItems = new ArrayList<>();
    private List<Region> items = new ArrayList<>();

    protected void onFavorite(@NonNull String regionId, boolean favorite) {
    }

    protected void onItemClick(@NonNull String regionId) {
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_VIEW_TYPE_ITEM:
            case ITEM_VIEW_TYPE_FAVORITE_ITEM:
                return new ViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_region, parent, false));
            default:
                return new DividerViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_region_divider, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final int viewType = holder.getItemViewType();
        if (viewType == ITEM_VIEW_TYPE_ITEM || viewType == ITEM_VIEW_TYPE_FAVORITE_ITEM) {

            ViewHolder itemHolder = (ViewHolder) holder;
            ListItemRegionBinding binding = itemHolder.getBinding();
            final Region item;
            if (viewType == ITEM_VIEW_TYPE_ITEM) {
                item = items.get(position - (favoriteItems.size() == 0 ? favoriteItems.size() : favoriteItems.size() + 1));
            } else {
                item = favoriteItems.get(position);
            }
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
    }

    @Override
    public int getItemCount() {
        int size = favoriteItems.size() + items.size();
        if (favoriteItems.size() != 0) {
            size += 1;
        }
        return size;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < favoriteItems.size()) {
            return ITEM_VIEW_TYPE_FAVORITE_ITEM;
        }

        if (favoriteItems.size() != 0 && position == favoriteItems.size()) {
            return ITEM_VIEW_TYPE_DIVIDER;
        }
        return ITEM_VIEW_TYPE_ITEM;
    }

    public void addFavoriteItems(@NonNull List<Region> data) {
        favoriteItems.addAll(data);
        notifyItemRangeInserted(0, favoriteItems.size() + 1);
    }

    public void addFavoriteItem(@NonNull Region data) {
        for (int i = 0; i < items.size(); i++) {
            Region region = items.get(i);
            if (region.getId().equals(data.getId())) {
                items.remove(i);

                int size = favoriteItems.size();
                if (size == 0) {
                    notifyItemInserted(0);
                }
                favoriteItems.add(data);
                notifyItemMoved(i + (size == 0 ? size : size + 1), size);
            }
        }
    }

    public void removeFavoriteItem(@NonNull Region data) {
        for (int i = 0; i < favoriteItems.size(); i++) {
            Region region = favoriteItems.get(i);
            if (region.getId().equals(data.getId())) {
                favoriteItems.remove(i);
                notifyItemRemoved(i);
                if (favoriteItems.size() == 0) {
                    notifyItemRemoved(0);
                }
                items.add(data);

                int size = favoriteItems.size() + items.size();
                if (favoriteItems.size() != 0) {
                    size += 1;
                }
                notifyItemInserted(size);
            }
        }
    }

    public void addAllItems(@NonNull List<Region> data) {
        items.addAll(data);
        notifyItemRangeInserted(
                favoriteItems.size() == 0 ? favoriteItems.size() : favoriteItems.size() + 1, items.size());
    }

    public void clear() {
        items.clear();
        favoriteItems.clear();
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
}
