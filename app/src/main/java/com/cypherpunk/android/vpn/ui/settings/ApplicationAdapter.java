package com.cypherpunk.android.vpn.ui.settings;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.databinding.ListItemApplicationBinding;
import com.cypherpunk.android.vpn.model.AppData;
import com.cypherpunk.android.vpn.widget.ApplicationItemView;

import java.util.ArrayList;
import java.util.List;


public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ViewHolder> {

    private List<AppData> items = new ArrayList<>();

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_application, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        ApplicationItemView appItemView = holder.getBinding().applicationItem;
        appItemView.setApp(items.get(position));
        appItemView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                items.get(position).check = isChecked;
            }
        });
        holder.getBinding().executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addAll(@NonNull List<AppData> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
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
