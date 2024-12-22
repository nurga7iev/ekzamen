package com.example.ekzamen2;
import android.content.Context;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CarLoader {
    public static List<Car> loadCarsFromJson(Context context) {
        List<Car> cars = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(context.getResources().openRawResource(R.raw.cars))
            );
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            reader.close();

            Gson gson = new Gson();
            Car[] carArray = gson.fromJson(stringBuilder.toString(), Car[].class);
            cars = Arrays.asList(carArray);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cars;
    }
}
