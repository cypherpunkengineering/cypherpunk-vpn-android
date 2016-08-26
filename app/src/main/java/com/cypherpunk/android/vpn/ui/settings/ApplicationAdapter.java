package com.cypherpunk.android.vpn.ui.settings;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.databinding.ListItemApplicationBinding;

import java.util.ArrayList;
import java.util.List;


public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ViewHolder> {

    private List<ApplicationSettingsActivity.AppData> items = new ArrayList<>();

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_application, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.getBinding().icon.setImageDrawable(items.get(position).icon);
        holder.getBinding().name.setText(items.get(position).name);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addAll(@NonNull List<ApplicationSettingsActivity.AppData> data) {
        items.addAll(data);
        notifyItemRangeInserted(0, items.size());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ListItemApplicationBinding binding;

        public ViewHolder(View view) {
            super(view);
            binding = DataBindingUtil.bind(view);
        }

        public ListItemApplicationBinding getBinding() {
            return binding;
        }
    }
}
