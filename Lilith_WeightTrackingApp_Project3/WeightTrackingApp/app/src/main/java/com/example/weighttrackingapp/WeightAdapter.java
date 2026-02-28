package com.example.weighttrackingapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for the weight grid/list.
 * - Tap row to edit weight
 * - Tap delete to remove row
 */
public class WeightAdapter extends RecyclerView.Adapter<WeightAdapter.VH> {

    public interface Listener {
        void onEdit(WeightEntry entry);
        void onDelete(WeightEntry entry);
    }

    private final Listener listener;
    private final List<WeightEntry> items = new ArrayList<>();

    public WeightAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<WeightEntry> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weight_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        WeightEntry e = items.get(position);
        holder.tvDate.setText(e.date);
        holder.tvWeight.setText(String.valueOf(e.weight));

        holder.btnDelete.setOnClickListener(v -> listener.onDelete(e));
        holder.itemView.setOnClickListener(v -> listener.onEdit(e));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDate;
        TextView tvWeight;
        MaterialButton btnDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvWeight = itemView.findViewById(R.id.tvWeight);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
