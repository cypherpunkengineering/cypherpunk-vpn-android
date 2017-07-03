package com.cypherpunk.privacy.ui.region;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.datasource.vpn.VpnServer;

import java.util.ArrayList;
import java.util.List;

// TODO: use DiffUtil
class RegionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_VIEW_TYPE_ITEM = 1;
    private static final int ITEM_VIEW_TYPE_DIVIDER = 2;
    private static final int ITEM_VIEW_TYPE_CYPHER_PLAY = 3;
    private static final int ITEM_VIEW_TYPE_FASTEST_LOCATION = 4;

    private final List<Object> items = new ArrayList<>();

    protected void onItemClicked(@NonNull VpnServer vpnServer, boolean isCypherPlay) {
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case ITEM_VIEW_TYPE_ITEM: {
                final ViewHolderRegion holder = ViewHolderRegion.create(inflater, parent);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final int position = holder.getAdapterPosition();
                        final VpnServer vpnServer = (VpnServer) items.get(position);
                        onItemClicked(vpnServer, false);
                    }
                });

                return holder;
            }
            case ITEM_VIEW_TYPE_CYPHER_PLAY: {
                final ViewHolderCypherPlay holder = ViewHolderCypherPlay.create(inflater, parent);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final int position = holder.getAdapterPosition();
                        final CypherPlay cypherplay = (CypherPlay) items.get(position);
                        onItemClicked(cypherplay.vpnServer, true);
                    }
                });
                return holder;
            }
            case ITEM_VIEW_TYPE_FASTEST_LOCATION: {
                final ViewHolderFastestLocation holder = ViewHolderFastestLocation.create(inflater, parent);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final int position = holder.getAdapterPosition();
                        final FastestLocation fastestLocation = (FastestLocation) items.get(position);
                        onItemClicked(fastestLocation.vpnServer, false);
                    }
                });
                return holder;
            }
            case ITEM_VIEW_TYPE_DIVIDER: {
                return ViewHolderDivider.create(inflater, parent);
            }
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final Context context = viewHolder.itemView.getContext();

        final int viewType = viewHolder.getItemViewType();
        switch (viewType) {
            case ITEM_VIEW_TYPE_FASTEST_LOCATION: {
                final FastestLocation fastestLocation = (FastestLocation) items.get(position);
                final ViewHolderFastestLocation holder = (ViewHolderFastestLocation) viewHolder;
                holder.flagView.setCountry(fastestLocation.vpnServer.country());
                break;
            }
            case ITEM_VIEW_TYPE_ITEM: {
                final VpnServer vpnServer = (VpnServer) items.get(position);
                final ViewHolderRegion holder = (ViewHolderRegion) viewHolder;
                holder.nameView.setText(vpnServer.name());
                holder.flagView.setCountry(vpnServer.country());
                holder.tagView.setLevel(vpnServer.level(), vpnServer.isAvailable());
                final long latency = vpnServer.latency();
                if (latency <= 0) {
                    holder.latencyErrorView.setVisibility(View.VISIBLE);
                    holder.latencyView.setVisibility(View.GONE);
                    holder.latencyView.setText("");
                } else {
                    holder.latencyErrorView.setVisibility(View.GONE);
                    holder.latencyView.setVisibility(View.VISIBLE);
                    holder.latencyView.setText(context.getString(R.string.region_latency_format, latency));
                }
                holder.setEnabled(vpnServer.isSelectable());
                break;
            }
            case ITEM_VIEW_TYPE_DIVIDER: {
                final Divider divider = (Divider) items.get(position);
                final ViewHolderDivider holder = (ViewHolderDivider) viewHolder;
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
        if (items.get(position) instanceof CypherPlay) {
            return ITEM_VIEW_TYPE_CYPHER_PLAY;
        }
        if (items.get(position) instanceof FastestLocation) {
            return ITEM_VIEW_TYPE_FASTEST_LOCATION;
        }
        return ITEM_VIEW_TYPE_DIVIDER;
    }

    void addRegionList(@Nullable VpnServer fastestVpnServer,
                       @NonNull List<VpnServer> devItems,
                       @NonNull List<VpnServer> naItems,
                       @NonNull List<VpnServer> saItems,
                       @NonNull List<VpnServer> crItems,
                       @NonNull List<VpnServer> euItems,
                       @NonNull List<VpnServer> meItems,
                       @NonNull List<VpnServer> afItems,
                       @NonNull List<VpnServer> asItems,
                       @NonNull List<VpnServer> opItems) {

        items.clear();
        if (fastestVpnServer != null) {
            items.add(new CypherPlay(fastestVpnServer));
            items.add(new FastestLocation(fastestVpnServer));
        }
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

    private void addItems(@NonNull List<VpnServer> data, @StringRes int dividerName) {
        if (!data.isEmpty()) {
            items.add(new Divider(dividerName));
            items.addAll(data);
        }
    }

    List<VpnServer> getVpnServers() {
        final List<VpnServer> result = new ArrayList<>();
        for (Object obj : items) {
            if (obj instanceof VpnServer) {
                result.add((VpnServer) obj);
            }
        }
        return result;
    }

    private class CypherPlay {
        @NonNull
        final VpnServer vpnServer;

        private CypherPlay(@NonNull VpnServer vpnServer) {
            this.vpnServer = vpnServer;
        }
    }

    private class FastestLocation {
        @NonNull
        final VpnServer vpnServer;

        private FastestLocation(@NonNull VpnServer vpnServer) {
            this.vpnServer = vpnServer;
        }
    }

    private class Divider {
        @StringRes
        private final int nameResId;

        private Divider(@StringRes int resId) {
            this.nameResId = resId;
        }
    }
}
