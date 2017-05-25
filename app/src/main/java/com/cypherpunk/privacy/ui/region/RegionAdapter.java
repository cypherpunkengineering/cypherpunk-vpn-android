package com.cypherpunk.privacy.ui.region;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.domain.model.VpnSetting;
import com.cypherpunk.privacy.domain.model.vpn.VpnServer;
import com.cypherpunk.privacy.ui.common.FontCache;
import com.cypherpunk.privacy.utils.ResourceUtil;
import com.cypherpunk.privacy.widget.StarView;

import java.util.ArrayList;
import java.util.List;

// TODO: use DiffUtil
public class RegionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_VIEW_TYPE_ITEM = 1;
    private static final int ITEM_VIEW_TYPE_DIVIDER = 2;
    private static final int ITEM_VIEW_TYPE_CYPHERPLAY = 3;
    private static final int ITEM_VIEW_TYPE_FASTEST_LOCATION = 4;

    private final List<Object> items = new ArrayList<>();

    // FIXME
    @ColorInt
    private final int textColor;
    @ColorInt
    private final int selectedTextColor;

    @NonNull
    private final VpnSetting vpnSetting;

    public RegionAdapter(@NonNull Context context, @NonNull VpnSetting vpnSetting) {
        this.vpnSetting = vpnSetting;
        textColor = ContextCompat.getColor(context, android.R.color.white);
        selectedTextColor = ContextCompat.getColor(context, R.color.region_selected_text);
    }

    protected void onFavoriteButtonClicked(@NonNull String vpnServerId, boolean favorite) {
    }

    protected void onItemClicked(@NonNull String vpnServerId) {
    }

    protected void onCypherplayClicked() {
    }

    protected void onFastestClicked() {
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case ITEM_VIEW_TYPE_ITEM: {
                final RegionViewHolder holder = RegionViewHolder.create(inflater, parent);
                holder.favoriteView.setOnCheckedChangeListener(new StarView.Listener() {
                    @Override
                    public void onCheckedChanged(boolean checked) {
                        final int position = holder.getAdapterPosition();
                        final VpnServer vpnServer = (VpnServer) items.get(position);
                        onFavoriteButtonClicked(vpnServer.id(), checked);
                    }
                });
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final int position = holder.getAdapterPosition();
                        final VpnServer vpnServer = (VpnServer) items.get(position);
                        onItemClicked(vpnServer.id());
                    }
                });

                return holder;
            }
            case ITEM_VIEW_TYPE_CYPHERPLAY: {
                final ViewHolder holder = new ViewHolder(inflater.inflate(R.layout.list_item_region_cypherplay, parent, false));
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onCypherplayClicked();
                    }
                });
                return holder;
            }
            case ITEM_VIEW_TYPE_FASTEST_LOCATION: {
                final ViewHolder holder = new ViewHolder(inflater.inflate(R.layout.list_item_region_fastest_location, parent, false));
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onFastestClicked();
                    }
                });
                return holder;
            }
            case ITEM_VIEW_TYPE_DIVIDER: {
                return DividerViewHolder.create(inflater, parent);
            }
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        final int viewType = viewHolder.getItemViewType();
        switch (viewType) {
            case ITEM_VIEW_TYPE_ITEM: {
                final String currentVpnServerId = vpnSetting.regionId();
                final VpnServer vpnServer = (VpnServer) items.get(position);
                final boolean isCurrentVpn = TextUtils.equals(currentVpnServerId, vpnServer.id());

                final Context context = viewHolder.itemView.getContext();
                final RegionViewHolder holder = (RegionViewHolder) viewHolder;

                holder.nameView.setText(vpnServer.name());
                holder.nameView.setTextColor(isCurrentVpn ? selectedTextColor : textColor);
                holder.nameView.setTypeface(isCurrentVpn
                        ? FontCache.getDosisBold(context)
                        : FontCache.getDosisRegular(context));

                holder.favoriteView.setChecked(vpnServer.favorite());
                holder.flagView.setImageResource(ResourceUtil.getFlag(context, vpnServer.country()));
                holder.tagView.setLevel(vpnServer.level());
                holder.setEnabled(vpnServer.isSelectable());
                break;
            }
            case ITEM_VIEW_TYPE_DIVIDER: {
                final Divider divider = (Divider) items.get(position);
                final DividerViewHolder holder = (DividerViewHolder) viewHolder;
                holder.textView.setText(divider.nameResId);
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof VpnServer) {
            return ITEM_VIEW_TYPE_ITEM;
        }
        if (items.get(position) instanceof Cypherplay) {
            return ITEM_VIEW_TYPE_CYPHERPLAY;
        }
        if (items.get(position) instanceof FastestLocation) {
            return ITEM_VIEW_TYPE_FASTEST_LOCATION;
        }
        return ITEM_VIEW_TYPE_DIVIDER;
    }

    public void addRegionList(
            @NonNull List<VpnServer> favoriteItems,
            @NonNull List<VpnServer> recentlyConnectedItems,
            @NonNull List<VpnServer> devItems,
            @NonNull List<VpnServer> naItems,
            @NonNull List<VpnServer> saItems,
            @NonNull List<VpnServer> crItems,
            @NonNull List<VpnServer> euItems,
            @NonNull List<VpnServer> meItems,
            @NonNull List<VpnServer> afItems,
            @NonNull List<VpnServer> asItems,
            @NonNull List<VpnServer> opItems) {
        clear();
        items.add(new Cypherplay());
        items.add(new FastestLocation());
        addFavoriteItems(favoriteItems);
        addRecentlyConnectedItems(recentlyConnectedItems);
        addItems(devItems, R.string.region_list_dev);
        addItems(naItems, R.string.region_list_na);
        addItems(saItems, R.string.region_list_sa);
        addItems(crItems, R.string.region_list_cr);
        addItems(euItems, R.string.region_list_eu);
        addItems(meItems, R.string.region_list_me);
        addItems(afItems, R.string.region_list_af);
        addItems(asItems, R.string.region_list_as);
        addItems(opItems, R.string.region_list_op);
        notifyItemRangeInserted(0, items.size());
    }

    private void addFavoriteItems(@NonNull List<VpnServer> data) {
        if (!data.isEmpty()) {
            items.add(new Divider(R.string.region_list_favorite));
            items.addAll(data);
        }
    }

    private void addRecentlyConnectedItems(List<VpnServer> data) {
        if (!data.isEmpty()) {
            items.add(new Divider(R.string.region_list_recent));
            items.addAll(data);
        }
    }

    private void addItems(@NonNull List<VpnServer> data, @StringRes int dividerName) {
        if (!data.isEmpty()) {
            items.add(new Divider(dividerName));
            items.addAll(data);
        }
    }

    public void clear() {
        int size = items.size();
        items.clear();
        notifyItemRangeRemoved(0, size);
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(View itemView) {
            super(itemView);
        }
    }

    private class Divider {
        @StringRes
        private int nameResId;

        Divider(@StringRes int resId) {
            this.nameResId = resId;
        }
    }

    private class Cypherplay {
    }

    private class FastestLocation {
    }
}
