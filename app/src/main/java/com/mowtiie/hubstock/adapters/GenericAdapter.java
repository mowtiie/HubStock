package com.mowtiie.hubstock.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mowtiie.hubstock.databinding.ItemRowBinding;

import java.util.List;

public class GenericAdapter<T> extends RecyclerView.Adapter<GenericAdapter.Holder> {

    public interface Binder<T> {
        void bind(ItemRowBinding binding, T item, int position);
    }

    public static class Holder extends RecyclerView.ViewHolder {
        final ItemRowBinding binding;

        Holder(ItemRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private List<T> items;
    private final Binder<T> binder;

    public GenericAdapter(List<T> items, Binder<T> binder) {
        this.items = items;
        this.binder = binder;
    }

    public void setItems(List<T> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        binder.bind(holder.binding, items.get(position), position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
