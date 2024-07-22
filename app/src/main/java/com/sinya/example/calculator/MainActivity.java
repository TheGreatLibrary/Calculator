package com.sinya.example.calculator;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String PREF_THEME = "pref_theme"; // переменная для хранения гаммы приложения
    private static final String THEME_MODE = "theme_mode"; // переменная для хранения дневной/темной темы
    private static int nightModeId = 1; // переменная для подрузки ночной темы
    private boolean comma = true; // если true - можно ставить запятую, иначе нельзя
    private EditText exampleAndAnswer; // сюда набирается текст, здесь же будет выведен ответ
    private EditText example; // сюда выводится пример исполненной программы
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeModeId = getIdTheme(THEME_MODE), themeId = getIdTheme(PREF_THEME); // получает id сохраненной темы и гаммы
        applyTheme(themeId); // ставит гамму
        applyThemeMode(themeModeId); // ставит тему

        ActionBar actionBar = getSupportActionBar(); // объявляет объект
        actionBar.hide(); // скрывает actionBar в приложении

        if (themeModeId == nightModeId) {
            nightModeId = 0; // меняет значение, чтобы устранить бесконечный цикл (костыльно, но это лучшее, что можно сделать так коротко и просто)
            recreate(); // перезагружает layout для прогрузки
        } // срабатывает только тогда, когда установлена ночная тема перед первым запуском приложения, в дальнейшем перезагрузка не требуется

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        example = findViewById(R.id.textView);
        example.setInputType(InputType.TYPE_NULL); // убирает возможность работать с клавиатурой и кареткой

        exampleAndAnswer = findViewById(R.id.textBox);
        exampleAndAnswer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.onTouchEvent(event);
                InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                return true;
            }
        }); // метод необходим для скрытия клавиатуры и работы каретки одновременно

        // Восстановление состояния
        if (savedInstanceState != null) {
            setText(savedInstanceState.getString("textview_text"));
            example.setText(savedInstanceState.getString("example_text"));
        }
        else {
            SharedPreferences preferences = getPreferences(MODE_PRIVATE);
            setText(preferences.getString("textview_text", ""));
            example.setText(preferences.getString("example_text", ""));
        }

        // Получение данных из Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("text_key")) {
            String text = intent.getStringExtra("text_key");
            if (text != null && !text.isEmpty()) {
                exampleAndAnswer.setText(text);
                exampleAndAnswer.setSelection(text.length());
                example.setText(" ");
            }
        }

        ImageButton menuButton = findViewById(R.id.menuButton); // три точки
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        }); // метод клика по трем точкам
    }


    /**
     * Методы отвечающие за получение и сохранение данных в калькуляторе
     * Срабатыают при: 1) переходе между страницами, 2) при выходе из приложения, 3) при повороте экрана
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("textview_text", exampleAndAnswer.getText().toString());
        outState.putString("example_text", example.getText().toString());
    } // сохраняет данные перед смертью слоя
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        setText(savedInstanceState.getString("textview_text"));
        example.setText(savedInstanceState.getString("example_text"));
    } // загружает данные при загрузке слоя
    @Override
    protected void onPause() {
        super.onPause();
        saveDataToPreferences();
    } // при выключении приложения
    @Override
    protected void onResume() {
        super.onResume();
        loadDataFromPreferences();
    } // при восстановлении приложения (слоя)
    @Override
    protected void onStop() {
        super.onStop();
        saveDataToPreferences();
    } // пауза...
    private void saveDataToPreferences() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("textview_text", exampleAndAnswer.getText().toString());
        editor.putString("example_text", example.getText().toString());
        editor.apply();
    } // срабатывает, когда activity перестает быть активной (переход между слоями, при выходе)
    private void loadDataFromPreferences() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        if (exampleAndAnswer.getText().toString().isEmpty()) {
            setText(preferences.getString("textview_text", ""));
        }
        if (example.getText().toString().isEmpty()) {
            example.setText(preferences.getString("example_text", ""));
        }
    } // срабатывает при подключении к activity


    /**
     * Отвечает за получение сохраненных данных, а также за установку этих тем
     * в приложении перед запуском
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
    } // выставляет светлую/темную тему
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
    } // выставляет выбранную гамму приложения


    /**
     * Метод отвечает за инициализацию, объявление и визуализацию (и работу) пунктов
     * меню настроек (3 точки вертикальных) в приложении. Ведет на другие слои калькулятора
     */
    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.poput_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId(); // получает индекс нажатого элемента меню
                if (id == R.id.action_settings) {
                    startActivity(new Intent(MainActivity.this, SettingActivity.class)); // переход в настройки
                }
                else if (id == R.id.action_history) {
                    startActivity(new Intent(MainActivity.this, HistoryActivity.class)); // переход в историю калькулятора
                }
                else return false;
                return true;
            } // клик по меню
        });
        popupMenu.show();
    } // открытие меню с настройками (3 точки)


    /**
     * Записывает данные в специальный файл построчно в конце вычисления примера
     */
    private void writeToFile(String example, Context context) {
        try {
            // не перезаписыает данные, а добавляет в конец, открывает поток
            FileOutputStream writer = context.openFileOutput("saved_examples.txt", Context.MODE_APPEND | Context.MODE_PRIVATE);
            writer.write((example+"\n").getBytes()); // записывает байты данных в файл
            writer.close(); // закрывает поток
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    } // записывает в текстовый файл информацию


    /**
     * Дальше идет основной костяк всего программирования калькулятора.
     * Здесь выполняется вся логика нажатий на кнопки, на текстовые поля.
     * Здесь же выполняется обработка самой строки при нажатии на "=".
     */
    public void Example_Click(View v) {
        String exampleText = example.getText().toString();
        if (!exampleText.equals("Ошибка")) // "Ошибка в выражении"?
        {
            exampleAndAnswer.setText(example.getText()); // вставляем новую строку
            exampleAndAnswer.setSelection(exampleText.length()); // передвигаем каретку
            example.setText(""); // очищаем пример

            String str = exampleAndAnswer.getText().toString(); // берем текст примера и сохраняем, как строку, для поиска знаков
            int lastSing = 0, lastComma = str.lastIndexOf('.');

            for (int i = 1; i<str.length(); i++) {
                if (Found("+*-/()%", i))
                {
                    lastSing = str.length() - i;
                    break;
                }
            } // ищем индекс последнего знака

            if (lastComma > -1 && lastComma < lastSing) comma = true; // если знак стоит позже запятой, можно разрешить ставить запятую
        }
    } // при нажатии на example пример возвращается в exampleAndAnswer для редактирования
    public void Numbers_Click(View v) {
        String btn = ((Button)v).getText().toString(); // текст из кнопки
        CheckExample(); // проверка строки

        if ("00".contains(btn)) {
            if (CaretInd()==0 || Found("+*-/()%^√", 1)) {
                setText("0");
            } // если строка пустая или слева знак, то добавляется 0, а не 00
            else if (!(exampleAndAnswer.getText().toString().equals("0") || (Found("+*-/()%", 2) && Found("0", 1)))) setText(btn);
        } // 00 и 0
        else setText(btn); // остальные цифры
    } // цифры
    public void Sqrt_Click(View v) {
        String btn = ((Button)v).getText().toString(); // текст из кнопки
        CheckExample(); // проверка строки
        setText(btn);
        comma = true;
    } // корень
    public void Pow_Click(View v) {
        String btn = ((Button)v).getText().toString(); // текст из кнопки
        CheckExample(); // проверка строки

        if (CaretInd() > 0 && !Found("+*-/(√^", 1)) {
            setText(btn);
            comma = true;
        } // если каретка не в начале строки и нет знаков рядом
    } // возведение в степень
    public void Signs_Click(View v) {
        String btn = ((Button)v).getText().toString(); // текст из кнопки
        CheckExample(); // проверка строки

        if (CaretInd() == 0 || Found("√(^", 1)) {
            if (btn.equals("-")) setText(btn);
        } // когда строка пуста или открыта скобка, ставить можно только "-"
        else {
            if (!(exampleAndAnswer.getText().toString().equals("-") || Found("(√^", 2) && Found("-", 1))) {
                if (Found("*/", 2) && Found("-", 1)) {
                    SubText(2);
                } // если стоит /- или *-, идет замена на введенный символ
                else if (Found("+/-*", 1)) {
                    if (!btn.equals("-") || Found("-+", 1)) {
                        SubText(1);
                    }
                } // если стоит уже знак, идет замена на введенный символ
                setText(btn); // добавляем знак
                comma = true; // разрешаем ставить запятую
            } // если строка не "-" и последние 2 знака не "(-", то можно
        }
    } // знаки *+/-
    public void Comma_Click(View v) {
        CheckExample(); // проверка строки
        if (comma)
        {
            if (CaretInd() == 0 || Found("*-/+()%√^", 1)) setText("0,");
            else setText(",");
            comma = false;
        }
    } // знак запятой
    public void Percent_Click(View v) {
        CheckExample(); // проверка строки
        if (Found("1234567890,)", 1))
        {
            setText("%");
            comma = true;
        } // % можно ставить только после цифр или запятой
    } // знак %
    public void Brackets_Click(View v) {
        String btn = ((Button)v).getText().toString(); // текст из кнопки
        CheckExample(); // проверка строки

        if (btn.equals("(") || btn.equals(")") && CanPutBracket() && !Found("+*-/(^√", 1))
        {
            setText(btn);
            comma = true;
        } // Если (, просто ставим; если ), то проверяем есть ли доступ к постановке
    } // знаки ( и )
    public void Delete_Click(View v) {
        String btn = ((Button)v).getText().toString(); // текст из кнопки
        CheckExample(); // проверка строки

        if (btn.equals("C")) {
            exampleAndAnswer.setText("");
            comma = true;
        } // полное стирание
        else if (btn.equals("D") && CaretInd() > 0)
        {
            if (Found(",", 1)) comma = true;
            SubText(1);
        } // стирание 1 символа
    } // знаки С и D
    public void Equals_Click(View v) {
        try {
            if (!Found("/-+*(√^", 1)) {
                while (CanPutBracket()) exampleAndAnswer.append(")");
                String num = ""; // строка для построения чисел
                char[] str = (exampleAndAnswer.getText().toString().replace(',', '.')+"=").toCharArray();
                ArrayList<String> arr = new ArrayList<>(); // список, в котором будет храниться пример

                for (int i = 0; i < str.length; i++)
                {
                    if ("1234567890.E".indexOf(str[i]) > -1) {
                        if (i > 0 && "%)".indexOf(str[i-1])>-1) arr.add("*"); // 5%5 -> 5%*5
                        num += str[i];
                    } // цифры и запятая собираются в число num
                    else if ("(√^)%/+*=".indexOf(str[i]) > -1) {
                        if (!num.isEmpty()) {
                            arr.add(num);
                            num = "";
                        } // если выставлен знак и num не пустой, то мы добавляем его в список и опустошаем
                        if (i > 0 && "1234567890.%)".indexOf(str[i-1]) > -1 && "√(".indexOf(str[i])>-1) arr.add("*"); // 12( -> 12*(
                        if (str[i] != '=') arr.add(String.valueOf(str[i])); // = никуда не идет
                    }
                    else if (str[i] == '-') {
                        if (!num.isEmpty()) {
                            arr.add(num);
                            num = "";
                        } // если выставлен знак и num не пустой, то мы добавляем его в список и опустошаем

                        if (i == 0 || ("/*(".indexOf(str[i-1]) > -1)) {
                            num += str[i];
                        } // если строка пуста или /- *- (
                        else arr.add(String.valueOf(str[i]));
                    } // отдельное условие для -
                } // преобразование строки

                if (!arr.isEmpty()) Execution(arr); // решаем пример, если в списке хотя бы есть 1 элемент

                comma = !exampleAndAnswer.getText().toString().contains(".");
            }
        }
       catch (Exception e) {
            example.setText("Ошибка");
        } // при отлове ошибки выводится сообщение
    } // знак =


    /**
     * Методы отвечают за вычисление примера. По цепочке они вызывают
     * друг друга (это сделано для удобства чтения кода).
     * */
    private void Execution(ArrayList<String> arr) {
        example.setText(exampleAndAnswer.getText()); // заданный пример ставится в другое поле
        if (arr.size() > 2)
        {
            for (int i = 0; i < arr.size() - 2; i++)
            {
                if (arr.get(i).contains("E") && arr.get(i + 1).equals("+"))
                {
                    arr.set(i, String.format(Locale.getDefault(),"%.12f", Double.parseDouble( arr.get(i) + arr.get(i + 1) + arr.get(i + 2))));
                    RemoveRange(arr, i+1, 2);
                } // конвертирует число с E
            } // ищем все строки с числами, где есть Е, чтобы конвертировать их число(строку) нужного формата
        }
        while (arr.contains("(")) CalculationWithBrackets(arr, InBrackets(arr)); // производятся действия со скобками
        exampleAndAnswer.setText(Calculation(arr)); // ответ в текстбокс
        exampleAndAnswer.setSelection(exampleAndAnswer.length());

        writeToFile(example.getText()+"="+ exampleAndAnswer.getText(), this); // записывает данные с выполненного примера в файл
    }
    private String Calculation(ArrayList<String> arr) {
        while (arr.size() != 1)
        {
            PowSqrt(arr); // √^
            MultyDivide(arr); // */
            Percent(arr); // %
            PlusMinus(arr); // +-
        }
        return arr.get(0);
    } // производит операции по вычислению всех действий в примере

    /**
     * Выполняется действие в скобках. Находятся индексы ( и ). Затем копируется список между скобками.
     * После этого новый список отправляется на вычисление и ответ ставится на место ( и удаляются лишние элементы.
     */
    private int InBrackets(ArrayList<String> arr) {
        int startInd = arr.lastIndexOf("("); // индекс последней открывающей скобки
        int endInd = IndexOfStart(arr, ")", startInd+1); // индекс первой открывающейся скобки ПОСЛЕ (
        ArrayList<String> newArr = new  ArrayList<>(arr.subList(startInd+1, endInd));
        arr.set(startInd, Calculation(newArr));  // вычисляем данный массив и ставим на место "("
        RemoveRange(arr, startInd + 1, endInd - startInd); // удаляем оставшиеся элементы от а+1 до ")"
        return startInd;
    } // метод находит самые глубокие в примере скобки и вырезает пример в них дял вычисления
    private void CalculationWithBrackets(ArrayList<String> arr, int startInd) {
        if (arr.size()-1 > startInd && arr.get(startInd + 1).equals("%")) {
            arr.set(startInd, String.valueOf(Double.parseDouble(arr.get(startInd))/100));
            RemoveRange(arr,startInd+1, 1);
        } // пример (12+2)%
        if ((startInd == 1 || (startInd > 1 && (arr.get(startInd - 2).equals("*") || arr.get(startInd - 2).equals("/")))) && arr.get(startInd - 1).equals("-")) {
            arr.set(startInd-1, String.valueOf(Double.parseDouble(arr.get(startInd))*-1));
            RemoveRange(arr, startInd, 1);
        } // пример -(12+4)
    } // действия рядом со скобками


    /**
     * Методы отвечают за действия со знаками в примере. Проходятся слева направо по примеру
     * и выполняют определенный метод по вычислению куска примера, удаляя затем лишние элементы списка
     */
    private void PowSqrt(ArrayList<String> arr) {
        for (int i = 0; i < arr.size(); i++) {
            if (arr.get(i).equals("√")) {
                Sqrt(arr, i);
                i = 0;
            }
            if (arr.get(i).equals("^")) {
                Pow(arr, i);
                i = 0;
            }
        }
    } // вызывает действие-вычисление для корня и степени
    private void Sqrt(ArrayList<String> arr, int i) {
        if (arr.size()>i+2 && arr.get(i+2).equals("%")) {
            arr.set(i, String.valueOf(sqrt(Double.parseDouble(arr.get(i+1)))/100));
            RemoveRange(arr, i+1, 2);
        }
        else {
            arr.set(i, String.valueOf(sqrt(Double.parseDouble(arr.get(i+1)))));
            RemoveRange(arr, i+1, 1);
        }
    } // вычисление корня
    private void Pow(ArrayList<String> arr, int i) {
        if (arr.size()>i+2 && arr.get(i+2).equals("%")) {
            arr.set(i+1, String.valueOf(Double.parseDouble(arr.get(i+1))/100));
            arr.set(i-1, String.valueOf(pow(Double.parseDouble(arr.get(i-1)), Double.parseDouble(arr.get(i+1)))));
            RemoveRange(arr, i, 3);
        }
        else if (arr.size()>i+1 && arr.get(i+1).equals("√")) {
            Sqrt(arr, i+1); // вычислит корень числа
            arr.set(i-1, String.valueOf(pow(Double.parseDouble(arr.get(i-1)), Double.parseDouble(arr.get(i+1)))));
            RemoveRange(arr, i, 2);
        }
        else {
            arr.set(i-1, String.valueOf(pow(Double.parseDouble(arr.get(i-1)), Double.parseDouble(arr.get(i+1)))));
            RemoveRange(arr, i, 2);
        }
    } // вычисление степени
    private void MultyDivide(ArrayList<String> arr) {
        for (int i = 0; i < arr.size(); i++) {
            if (arr.get(i).equals("*")) {
                Multiply(arr, i);
                i = 0;
            }
            if (arr.get(i).equals("/")) {
                Divide(arr, i);
                i = 0;
            }
        }
    } // вызывает действие-вычисление для умножения и деления
    private void Divide(ArrayList<String> arr, int i) {
        if ((arr.get(i - 1).equals("%")) && (arr.size() - i - 2 > 0 && arr.get(i+2).equals("%"))) {
            arr.set(i-2, String.valueOf(Double.parseDouble(arr.get(i-2))/Double.parseDouble(arr.get(i+1))));
            RemoveRange(arr, i - 1, 4);
        }
        else if (arr.get(i - 1).equals("%")) {
            arr.set(i-2, String.valueOf(Double.parseDouble(arr.get(i-2))/Double.parseDouble(arr.get(i+1))/100));
            RemoveRange(arr, i - 1, 3);
        }
        else if (arr.size() - i - 2 > 0 && arr.get(i+2).equals("%")) {
            arr.set(i-1, String.valueOf(Double.parseDouble(arr.get(i-1))/Double.parseDouble(arr.get(i+1))*100));
            RemoveRange(arr, i, 3);
        }
        else {
            arr.set(i-1, String.valueOf(Double.parseDouble(arr.get(i-1))/Double.parseDouble(arr.get(i+1))));
            RemoveRange(arr, i, 2);
        }
    } // вычисление деления
    private void Multiply(ArrayList<String> arr, int i) {
        if ((arr.get(i - 1).equals("%")) && (arr.size() - i - 2 > 0 && arr.get(i + 2).equals("%"))) {
            arr.set(i-2, String.valueOf(Double.parseDouble(arr.get(i-2))*Double.parseDouble(arr.get(i+1))/10000));
            RemoveRange(arr, i - 1, 4);
        }
        else if (arr.get(i - 1).equals("%")) {
            arr.set(i-2, String.valueOf(Double.parseDouble(arr.get(i-2))*Double.parseDouble(arr.get(i+1))/100));
            RemoveRange(arr, i - 1, 3);
        }
        else if (arr.size() - i - 2 > 0 && arr.get(i+2).equals("%")) {
            arr.set(i-1, String.valueOf(Double.parseDouble(arr.get(i-1))*Double.parseDouble(arr.get(i+1))/100));
            RemoveRange(arr, i, 3);
        }
        else {
            arr.set(i-1, String.valueOf(Double.parseDouble(arr.get(i-1))*Double.parseDouble(arr.get(i+1))));
            RemoveRange(arr, i, 2);
        }
    } // вычисление умножения
    private void Percent(ArrayList<String> arr) {
        for (int i = 0; i < arr.size(); i++) {
            if (arr.get(i).equals("%")) {
                if (i > 1 && "+-*/".contains(arr.get(i - 2))) return;
                else {
                    arr.set(i - 1, String.valueOf((Double.parseDouble(arr.get(i-1))) / 100));
                    RemoveRange(arr, i, 1);
                }
            }
        }
    } // вычисление процента
    private void PlusMinus(ArrayList<String> arr) {
        for (int i = 0; i < arr.size(); i++) {
            if (arr.get(i).equals("+")) {
                Addition(arr, i);
                i = 0;
            }
            if (arr.get(i).equals("-")) {
                Subtraction(arr, i);
                i = 0;
            }
        }
    } // вызывает действие-вычисление для сложения и вычитаения
    private void Addition(ArrayList<String> arr, int i) {
        if (i + 2 < arr.size() && arr.get(i + 2).equals("%")) {
            arr.set(i - 1, String.valueOf(Double.parseDouble(arr.get(i - 1)) * (1 + Double.parseDouble(arr.get(i + 1)) / 100)));
            RemoveRange(arr, i, 3);
        }
        else {
            arr.set(i - 1, String.valueOf(Double.parseDouble(arr.get(i - 1)) + Double.parseDouble(arr.get(i + 1))));
            RemoveRange(arr, i, 2);
        }
    } // вычисление сложения
    private void Subtraction(ArrayList<String> arr, int i) {
        if (i + 2 < arr.size() && arr.get(i + 2).equals("%")) {
            arr.set(i - 1, String.valueOf(Double.parseDouble(arr.get(i - 1)) * (1 - Double.parseDouble(arr.get(i + 1)) / 100)));
            RemoveRange(arr, i, 3);
        }
        else {
            arr.set(i - 1, String.valueOf(Double.parseDouble(arr.get(i - 1)) - Double.parseDouble(arr.get(i + 1))));
            RemoveRange(arr, i, 2);
        }
    } // вычисление вычитания


    /**
     * Данные методы специализируются на вспомогательных функциях для упрощения
     * работы калькулятора, улучшения читаемости кода, оптимизации и реюзинга кода.
     */
    private boolean CanPutBracket() {
        String text = exampleAndAnswer.getText().toString();
        int countOpen = 0, countClose = 0; // количество открытых и закрытых скобок

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i); // для удобства присваивается символ из строки в переменную
            if (c == '(') {
                countOpen++;
            }
            else if (c == ')') {
                countClose++;
            }
        }
        return countOpen > countClose;
    } // проверка на возможность ставить в примере закрывающую скобку
    private boolean Found(String signs, int i) {
        char[] str = exampleAndAnswer.getText().toString().toCharArray(); // переводит пример из строки в массив символов
        if (exampleAndAnswer.getSelectionStart()-i>=0) return signs.indexOf(str[exampleAndAnswer.getSelectionStart() - i]) > -1; // производится поиск
        return false;
    } // метод для поиска символа (одного из поданной строки) в строке примера
    private void RemoveRange(ArrayList<String> arr, int index, int count) {
        for (int i = 0; i<count; i++) {
            arr.remove(index);
        }
    } // метод создан для удаления перечня элементов списка
    private int IndexOfStart(ArrayList<String> arr, String str, int startInd) {
        for (int i=startInd; i<arr.size(); i++) {
            if (arr.get(i).equals(str)) return i;
        }
        return -1;
    } // метод находит подстроку начиная с какого-то определенного индекса списка
    private void setText(String textToInsert) {
        int cursorPosition = exampleAndAnswer.getSelectionStart(); // сохраняем для удобства индекс каретки в переменную
        StringBuilder newText = new StringBuilder(exampleAndAnswer.getText().toString()); // создаем новую строку
        newText.insert(cursorPosition, textToInsert); // вставляем текст в строку
        exampleAndAnswer.setText(newText.toString());
        exampleAndAnswer.setSelection(cursorPosition + textToInsert.length());
    } // вставляет текст в текстовое поле после каретки-курсора
    private void SubText(int numCut) {
        int cursorPosition = exampleAndAnswer.getSelectionStart();
        StringBuilder newText = new StringBuilder(exampleAndAnswer.getText().toString());
        newText.delete(cursorPosition-numCut, cursorPosition);
        exampleAndAnswer.setText(newText.toString());
        exampleAndAnswer.setSelection(cursorPosition-numCut);
    } // метод для удаления N-символов из строки, начиная с каретки
    private void CheckExample() {
        String example = exampleAndAnswer.getText().toString();
        if (example.equals(Double.toString(Double.NaN)) || example.contains("∞")) {
            exampleAndAnswer.setText("");
            exampleAndAnswer.setSelection(0);
            comma = true;
        } // строка очищается, если там есть 2 этих значения
    } // проверяет пример на содержание недопустимых значений
    private int CaretInd() {
        return exampleAndAnswer.getSelectionStart();
    } // короткий метод для получения индекса каретки
}