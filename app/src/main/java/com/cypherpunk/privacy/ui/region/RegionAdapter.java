package com.cypherpunk.privacy.ui.region;

import android.databinding.DataBindingUtil;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.databinding.ListItemRegionBinding;
import com.cypherpunk.privacy.databinding.ListItemRegionDividerBinding;
import com.cypherpunk.privacy.model.CypherpunkSetting;
import com.cypherpunk.privacy.model.Region;
import com.cypherpunk.privacy.utils.ResourceUtil;
import com.cypherpunk.privacy.widget.StarView;

import java.util.ArrayList;
import java.util.List;

import static com.os.operando.garum.utils.Cache.getContext;


public class RegionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_VIEW_TYPE_ITEM = 1;
    private static final int ITEM_VIEW_TYPE_DIVIDER = 2;
    private static final int ITEM_VIEW_TYPE_FASTEST_LOCATION = 3;

    private final List<Object> items = new ArrayList<>();

    @ColorInt
    private final int textColor;
    @ColorInt
    private final int selectedTextColor;

    public RegionAdapter() {
        textColor = ContextCompat.getColor(getContext(), android.R.color.white);
        selectedTextColor = ContextCompat.getColor(getContext(), R.color.region_selected_text);
    }

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
                return new RegionViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_region, parent, false));
            case ITEM_VIEW_TYPE_FASTEST_LOCATION:
                return new ViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_region_fastest_location, parent, false));
            default:
                return new DividerViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_region_divider, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final int viewType = holder.getItemViewType();
        switch (viewType) {
            case ITEM_VIEW_TYPE_ITEM:
                RegionViewHolder itemHolder = (RegionViewHolder) holder;
                ListItemRegionBinding binding = itemHolder.getBinding();
                final Region item = (Region) items.get(position);
                final String regionId = item.getId();
                binding.regionName.setText(item.getRegionName());
                binding.favorite.setOnCheckedChangeListener(new StarView.Listener() {
                    @Override
                    public void onCheckedChanged(boolean checked) {
                        onFavorite(regionId, checked);

                    }
                });
                binding.favorite.setChecked(item.isFavorited());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onItemClick(regionId);
                    }
                });

                binding.nationalFlag.setImageResource(ResourceUtil.getFlagDrawableByKey(getContext(), item.getCountryCode().toLowerCase()));

                CypherpunkSetting setting = new CypherpunkSetting();
                binding.regionName.setTextColor(!TextUtils.isEmpty(setting.regionId) && regionId.equals(setting.regionId) ?
                        selectedTextColor : textColor);
                break;
            case ITEM_VIEW_TYPE_FASTEST_LOCATION:
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onFastestLocationClick();
                    }
                });
                break;
            case ITEM_VIEW_TYPE_DIVIDER:
                DividerViewHolder dividerHolder = (DividerViewHolder) holder;
                ListItemRegionDividerBinding dividerBinding = dividerHolder.getBinding();
                final Divider dividerItem = (Divider) items.get(position);
                dividerBinding.text.setText(dividerItem.title);
                break;
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
        items.add(new FastestLocation());
        addFavoriteItems(favoriteItems);
        addRecentlyConnectedItems(recentlyConnectedItems);
        addAllItems(otherItems);
        notifyItemRangeInserted(0, items.size());
    }

    private void addFavoriteItems(@NonNull List<Region> data) {
        if (!data.isEmpty()) {
            items.add(new Divider(getContext().getString(R.string.region_list_favorite)));
        }
        items.addAll(data);
    }

    private void addRecentlyConnectedItems(List<Region> data) {
        if (!data.isEmpty()) {
            items.add(new Divider(getContext().getString(R.string.region_list_recent)));
        }
        items.addAll(data);
    }

    private void addAllItems(@NonNull List<Region> data) {
        items.add(new Divider(getContext().getString(R.string.region_list_all)));
        items.addAll(data);
    }

    public void clear() {
        int size = items.size();
        items.clear();
        notifyItemRangeRemoved(0, size);
    }

    private static class RegionViewHolder extends RecyclerView.ViewHolder {

        private ListItemRegionBinding binding;

        RegionViewHolder(View view) {
            super(view);
            binding = DataBindingUtil.bind(view);
        }

        public ListItemRegionBinding getBinding() {
            return binding;
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    private static class DividerViewHolder extends RecyclerView.ViewHolder {

        private ListItemRegionDividerBinding binding;

        DividerViewHolder(View view) {
            super(view);
            binding = DataBindingUtil.bind(view);
        }

        public ListItemRegionDividerBinding getBinding() {
            return binding;
        }

    }

    private class Divider {
        private String title;

        public Divider(String title) {
            this.title = title;
        }
    }

    private class FastestLocation {
    }
}
