package com.example.ekzamen2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {

    private List<Car> carList;
    private OnItemClickListener onItemClickListener;

    public CarAdapter(List<Car> carList, OnItemClickListener onItemClickListener) {
        this.carList = carList;
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public CarViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_car, parent, false);
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CarViewHolder holder, int position) {
        Car car = carList.get(position);
        holder.bind(car, onItemClickListener);
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    public void updateList(List<Car> newCarList) {
        carList = newCarList;
        notifyDataSetChanged();
    }

    public void sortByManufacturer(List<Car> sortedList) {
        this.carList = sortedList;
        notifyDataSetChanged();
    }

    public class CarViewHolder extends RecyclerView.ViewHolder {
        private TextView manufacturerTextView;
        private TextView modelTextView;
        private TextView priceTextView;

        public CarViewHolder(View itemView) {
            super(itemView);
            manufacturerTextView = itemView.findViewById(R.id.manufacturerTextView);
            modelTextView = itemView.findViewById(R.id.modelTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
        }

        public void bind(final Car car, final OnItemClickListener listener) {
            manufacturerTextView.setText(car.getManufacturer());
            modelTextView.setText(car.getModel());
            priceTextView.setText(String.valueOf(car.getPrice()));

            itemView.setOnClickListener(v -> listener.onItemClick(car, getAdapterPosition()));
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Car car, int position);
    }
}
