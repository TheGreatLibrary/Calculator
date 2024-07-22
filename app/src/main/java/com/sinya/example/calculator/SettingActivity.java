package com.sinya.example.calculator;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.content.SharedPreferences;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
public class SettingActivity extends AppCompatActivity {
    private static final String PREF_THEME = "pref_theme"; // переменная для хранения гаммы приложения
    private static final String THEME_MODE = "theme_mode"; // переменная для хранения дневной/темной темы
    private Spinner spinnerMode; // выпадающее меню для выбора дневной/ночной темы
    private Spinner spinnerTheme; // выпадающее меню для гаммы приложения
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
        setContentView(R.layout.activity_setting);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        spinnerMode = findViewById(R.id.themeModeSpinner);
        ArrayAdapter<CharSequence> adapterMode = ArrayAdapter.createFromResource(this, R.array.theme_mode_items, android.R.layout.simple_spinner_item);
        adapterMode.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMode.setAdapter(adapterMode);
        spinnerMode.setSelection(themeModeId);
        spinnerMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int savedTheme = getIdTheme(THEME_MODE);

                if (savedTheme != position) {
                    applyThemeMode(position);
                    saveThemePreference(getApplicationContext(), position, THEME_MODE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerTheme = findViewById(R.id.themeSpinner);
        ArrayAdapter<CharSequence> adapterTheme = ArrayAdapter.createFromResource(this, R.array.theme_items, android.R.layout.simple_spinner_item);
        adapterTheme.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTheme.setAdapter(adapterTheme);
        spinnerTheme.setSelection(themeId);
        spinnerTheme.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int savedTheme = getIdTheme(PREF_THEME);

                if (savedTheme != position) {
                    applyTheme(position);
                    saveThemePreference(getApplicationContext(), position, PREF_THEME);
                    recreate(); // Пересоздаем Activity для применения новой темы
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
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
                break; // дефолт тема
            case 1:
                setTheme(R.style.Base_Theme_Calculator_Green);
                break; // зеленая тема
            case 2:
                setTheme(R.style.Base_Theme_Calculator_Blue);
                break; // синяя тема
            case 3:
                setTheme(R.style.Base_Theme_Calculator_Town);
                break; // городская тема
        }
    } // выбор темы приложения
    public void saveThemePreference(Context context, int selectedTheme, String pref_name) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(pref_name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(pref_name, selectedTheme);
        editor.apply();
    } // Метод для сохранения данных
}