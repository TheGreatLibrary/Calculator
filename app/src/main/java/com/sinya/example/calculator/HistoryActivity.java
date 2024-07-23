package com.sinya.example.calculator;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
            public void onStateClick(int position, String example) {
                Intent intent = new Intent(HistoryActivity.this, MainActivity.class);
                intent.putExtra("text_key", example);
                startActivity(intent);
            }});  // при нажатии на элемент истории данные с ячейки записываются в Intent и загружают MainActivity вместо вкладки истории
        recyclerView.setAdapter(myAdapter);
    }


    /**
     * Методы обрабатывают действия с кнопкой по очистке истории.
     * Кнопка находится в ActionBar, при клике должно появиться предупреждение,
     * а затем удаление истории в случае согласия
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.clean_history_btn, menu); // прикрепляем стиль кнопки-меню
        return true;
    } // кнопка в actionBar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_button) {// Ваше событие при нажатии на кнопку
            showDialog(); // окно предупреждения
            return true;
        }
        return super.onOptionsItemSelected(item);
    } // метод нажатия на меню

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(HistoryActivity.this, R.style.CustomAlertDialogTheme);
        builder.setTitle("Предупреждение");
        builder.setMessage("Если вы продолжите, вся история калькулятора будет утеряна без возможности восстановления");

        builder.setPositiveButton("ОК", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearFileWithHistory(HistoryActivity.this); // метод очистки истории
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Назад", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Действие при нажатии на кнопку "Cancel"
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    } // обрабатывает действия кнопок и создает окно предупреждения

    /**
     * Методы отвечают за чтение и удаление данных в файле. То есть отвечают за
     * работу с файлом
     */
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
    } // записывает данные в список из файла
    private void clearFileWithHistory(Context context) {
        try {
            // контекст добавления убран, чтобы при добавлении данных файл перезаписался и очистился
            FileOutputStream writer = context.openFileOutput("saved_examples.txt", Context.MODE_PRIVATE);
            writer.write(("").getBytes()); // записывает пустую строку
            writer.close(); // закрывает поток
            Toast.makeText(this, "Очистка истории успешно завершена", Toast.LENGTH_LONG).show();
            recreate(); // перезапускает страницу для отображения пустой истории
        } catch (IOException e) {
            Toast.makeText(this, "Ошибка очистки истории", Toast.LENGTH_LONG).show();
        }
    } // очищает историю калькулятора


    /**
     * методы отвечают за установку нужной сохраненной темы перед запуском приложения
     */
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