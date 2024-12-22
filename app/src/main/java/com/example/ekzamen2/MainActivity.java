package com.example.ekzamen2;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private List<Car> carList = new ArrayList<>();
    private CarAdapter carAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load car data from raw resource (instead of file storage)
        String json = loadJsonFromRaw();
        if (json != null) {
            Gson gson = new Gson();
            Type carListType = new TypeToken<List<Car>>(){}.getType();
            carList = gson.fromJson(json, carListType);
        }

        // Setup RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        carAdapter = new CarAdapter(carList, new CarAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Car car, int position) {
                showEditCarDialog(position); // Edit car at position
            }
        });
        recyclerView.setAdapter(carAdapter);

        // Add a new car
        findViewById(R.id.addCarButton).setOnClickListener(v -> showAddCarDialog());

        // Filter cars
        findViewById(R.id.filterButton).setOnClickListener(v -> showFilterDialog());

        // Reset filter and show all cars
        findViewById(R.id.resetFilterButton).setOnClickListener(v -> {
            carAdapter.updateList(carList);
            findViewById(R.id.resetFilterButton).setVisibility(View.GONE);
        });

        // Sort cars by price
        findViewById(R.id.sortByPriceButton).setOnClickListener(v -> sortCarsByPrice());
    }



    private void saveCarsToJson(List<Car> cars) {
        Gson gson = new Gson();
        String json = gson.toJson(cars);

        try {
            FileOutputStream fos = openFileOutput("cars.json", MODE_PRIVATE);
            fos.write(json.getBytes());
            fos.close();
            Toast.makeText(this, "Cars saved to file", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving to file", Toast.LENGTH_SHORT).show();
        }
    }

    // Загрузка автомобилей из JSON
    private List<Car> loadCarsFromJson() {
        try {
            FileInputStream fis = openFileInput("cars.json");
            int size = fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            fis.close();
            String json = new String(buffer, "UTF-8");

            Gson gson = new Gson();
            Type carListType = new TypeToken<List<Car>>(){}.getType();
            return gson.fromJson(json, carListType);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading cars from file", Toast.LENGTH_SHORT).show();
        }
        return new ArrayList<>();
    }


    private String loadJsonFromRaw() {
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.cars);
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            return new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("JSON_ERROR", "Failed to load JSON file: " + e.getMessage());
            Toast.makeText(this, "Error loading JSON", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    // Диалог для добавления нового автомобиля
    private void showAddCarDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Car");

        View view = getLayoutInflater().inflate(R.layout.dialog_add_car, null);
        builder.setView(view);

        EditText manufacturerEditText = view.findViewById(R.id.manufacturerEditText);
        EditText modelEditText = view.findViewById(R.id.modelEditText);
        EditText yearEditText = view.findViewById(R.id.yearEditText);
        EditText priceEditText = view.findViewById(R.id.priceEditText);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String manufacturer = manufacturerEditText.getText().toString();
            String model = modelEditText.getText().toString();
            int year;
            double price;

            try {
                year = Integer.parseInt(yearEditText.getText().toString());
                price = Double.parseDouble(priceEditText.getText().toString());

                if (manufacturer.isEmpty() || model.isEmpty()) {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                Car newCar = new Car(manufacturer, model, year, price);
                carList.add(newCar);
                carAdapter.notifyDataSetChanged();

                // Сохраняем изменения в файл
                saveCarsToJson(carList);

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid input for year or price", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }


    // Диалог для фильтрации автомобилей
    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter Cars");

        // Inflate the filter layout
        View view = getLayoutInflater().inflate(R.layout.dialog_filter_car, null);
        builder.setView(view);

        Spinner manufacturerSpinner = view.findViewById(R.id.manufacturerSpinner);
        Spinner modelSpinner = view.findViewById(R.id.modelSpinner);

        // Load car manufacturers into the spinner
        ArrayAdapter<CharSequence> manufacturerAdapter = ArrayAdapter.createFromResource(this,
                R.array.car_brands, android.R.layout.simple_spinner_item);
        manufacturerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        manufacturerSpinner.setAdapter(manufacturerAdapter);

        // Filter models based on selected manufacturer
        manufacturerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedManufacturer = parentView.getItemAtPosition(position).toString();
                List<String> modelList = new ArrayList<>();
                for (Car car : carList) {
                    if (car.getManufacturer().equals(selectedManufacturer)) {
                        modelList.add(car.getModel());
                    }
                }

                // Remove duplicates and update the model spinner
                Set<String> uniqueModels = new HashSet<>(modelList);
                ArrayAdapter<String> modelAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, new ArrayList<>(uniqueModels));
                modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                modelSpinner.setAdapter(modelAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });

        builder.setPositiveButton("Apply", (dialog, which) -> {
            String selectedManufacturer = manufacturerSpinner.getSelectedItem().toString();
            String selectedModel = modelSpinner.getSelectedItem().toString();

            List<Car> filteredCars = new ArrayList<>();
            for (Car car : carList) {
                if (car.getManufacturer().equals(selectedManufacturer) &&
                        car.getModel().equals(selectedModel)) {
                    filteredCars.add(car);
                }
            }

            carAdapter.updateList(filteredCars);
            findViewById(R.id.resetFilterButton).setVisibility(View.VISIBLE);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }


    // Сортировка автомобилей по цене
    private void sortCarsByPrice() {
        Collections.sort(carList, (car1, car2) -> Double.compare(car1.getPrice(), car2.getPrice()));
        carAdapter.notifyDataSetChanged();
    }

    // Диалог для редактирования автомобиля
    void showEditCarDialog(int position) {
        Car car = carList.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Car");

        View view = getLayoutInflater().inflate(R.layout.dialog_add_car, null);
        builder.setView(view);

        EditText manufacturerEditText = view.findViewById(R.id.manufacturerEditText);
        EditText modelEditText = view.findViewById(R.id.modelEditText);
        EditText yearEditText = view.findViewById(R.id.yearEditText);
        EditText priceEditText = view.findViewById(R.id.priceEditText);

        manufacturerEditText.setText(car.getManufacturer());
        modelEditText.setText(car.getModel());
        yearEditText.setText(String.valueOf(car.getYear()));
        priceEditText.setText(String.valueOf(car.getPrice()));

        builder.setPositiveButton("Save", (dialog, which) -> {
            String manufacturer = manufacturerEditText.getText().toString();
            String model = modelEditText.getText().toString();
            int year;
            double price;

            try {
                year = Integer.parseInt(yearEditText.getText().toString());
                price = Double.parseDouble(priceEditText.getText().toString());

                if (manufacturer.isEmpty() || model.isEmpty()) {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                car.setManufacturer(manufacturer);
                car.setModel(model);
                car.setYear(year);
                car.setPrice(price);

                carAdapter.notifyItemChanged(position);

                // Сохраняем изменения в файл
                saveCarsToJson(carList);

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid input for year or price", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

}
