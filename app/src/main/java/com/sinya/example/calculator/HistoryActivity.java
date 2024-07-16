package com.sinya.example.calculator;

import android.adservices.common.AdData;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {
    private static final String PREF_THEME = "pref_theme"; // переменная для хранения гаммы приложения
    private static final String THEME_MODE = "theme_mode"; // переменная для хранения дневной/темной темы
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeModeId = getIdTheme(THEME_MODE), themeId = getIdTheme(PREF_THEME);
        applyThemeMode(themeModeId);
        applyTheme(themeId);

        ActionBar actionBar = getSupportActionBar();
        actionBar.show();
        actionBar.setDisplayHomeAsUpEnabled(true);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        MyAdapter myAdapter = new MyAdapter(readFromFile(this), new MyAdapter.OnStateClickListener() {
            @Override
            public void onStateClick(int position) {
                Toast.makeText(getApplicationContext(), "Был выбран пункт "+ position, Toast.LENGTH_SHORT).show();
            }});
        recyclerView.setAdapter(myAdapter);
    }

    private ArrayList<String> readFromFile(Context context) {
        ArrayList<String> data = new ArrayList<>();
        try {
            FileInputStream fis = context.openFileInput("saved_examples.txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
               data.add(line);
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    private int getIdTheme(String pref_name) {
        SharedPreferences preferences = getSharedPreferences(pref_name, MODE_PRIVATE);
        return preferences.getInt(pref_name, 0);
    } // получает данные сохраненной темы
    private void applyThemeMode(int theme) {
        switch (theme) {
            case 0:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case 1:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }
    } // выбор светлой/темной темы
    private void applyTheme(int theme) {
        switch (theme) {
            case 0:
                setTheme(R.style.Base_Theme_Calculator);
                break;
            case 1:
                setTheme(R.style.Base_Theme_Calculator_Green);
                break;
            case 2:
                setTheme(R.style.Base_Theme_Calculator_Blue);
                break;
            case 3:
                setTheme(R.style.Base_Theme_Calculator_Town);
                break;
        }
    } // выбор темы приложения
}