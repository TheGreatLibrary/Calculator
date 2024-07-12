package com.sinya.example.calculator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String PREF_THEME = "pref_theme"; // переменная для хранения гаммы приложения
    private static final String THEME_MODE = "theme_mode"; // переменная для хранения дневной/темной темы
    private TextView textView; // основное поле для ввода текста
    private TextView example; // поле для ответа
    private boolean comma = true; // если true - можно ставить запятую, иначе нельзя
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeModeId = getIdTheme(THEME_MODE), themeId = getIdTheme(PREF_THEME);
        applyThemeMode(themeModeId);
        applyTheme(themeId);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textBox);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setHorizontallyScrolling(true);

        example = findViewById(R.id.textView);
        example.setMovementMethod(new ScrollingMovementMethod());
        example.setHorizontallyScrolling(true);

        ImageButton menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
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
    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.poput_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.action_settings) {
                    startActivity(new Intent(MainActivity.this, SettingActivity.class));
                } else if (id == R.id.action_history) {
                    startActivity(new Intent(MainActivity.this, HistoryActivity.class));
                } else return false;
                return true;
            }
        });
        popupMenu.show();
    } // открытие меню с настройками



    /// работа с историей...
    private void saveExample(String example) {
        try(FileWriter writer = new FileWriter("saved_examples.txt", false))
        {
            writer.write(example);
            writer.flush();
            Toast.makeText(getApplicationContext(), "Запись произошла", Toast.LENGTH_SHORT).show();
        }
        catch(IOException ex){
            Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_SHORT).show();
        }
    } // записывает в текстовый файл информацию
    public void Example_Click(View v) {
        if (example.getText() != "Ошибка в выражении")
        {
            textView.setText(example.getText().subSequence(0, example.getText().length()-1));
            String str = textView.getText().toString();
            int lastSing = 0, lastComma = str.lastIndexOf('.');

            for (int i = 1; i<str.length(); i++)
            {
                if (Found("+*-/()%", i))
                {
                    lastSing = str.length() - i;
                    break;
                } // ищем индекс последнего знака
            }
            if (lastComma > -1 && lastComma < lastSing) comma = true; // если знак стоит позже запятой, можно разрешить ставить запятую
        }
    } // При нажатии на пример, ответ заменяется обратно на пример




    public void Numbers_Click(View v) {
        String btn = ((Button)v).getText().toString(); // текст из кнопки
        if (textView.getText() == Double.toString(Double.NaN) || textView.getText().toString().contains("∞"))
        {
            textView.setText("");
            comma = true;
        } // строка очищается, если там есть 2 этих значения

        int len = textView.getText().toString().length(); // длина строки примера

        if ("00".indexOf(btn) > -1)
        {
            if (len == 0 || Found("+*-/()%", 1)) textView.setText(textView.getText()+"0"); // если строка пустая или слева знак, то пишется 0, а не 00
            else if (textView.getText() == "0" || (len > 1 && Found("+*-/()%", 2) && Found("0", 1))) { } // ничего не делаем
            else textView.setText(textView.getText()+btn);
        } // у 00 и 0 свои условия
        else
        {
            textView.setText(textView.getText()+btn);
        }

    } // цифры
    public void Signs_Click(View v) {
        String btn = ((Button)v).getText().toString(); // текст из кнопки

        if (textView.getText() == Double.toString(Double.NaN) || textView.getText().toString().contains("∞"))
        {
            textView.setText("");
            comma = true;
        } // строка очищается, если там есть 2 этих значения

        int len = textView.getText().toString().length(); // длина строки примера

        if (len == 0 || (len > 0 && Found("(", 1)))
        {
            if (btn.equals("-")) textView.setText(textView.getText()+btn);
        } // когда строка пуста или открыта скобка, ставить можно только "-"
        else
        {
            if (textView.getText() != "-" && !(len > 1 && Found("(", 2) && Found("-", 1)))
            {
                if (len > 1 && Found("*/", 2) && Found("-", 1))
                {
                    textView.setText(textView.getText().subSequence(0, len-2));
                } // если стоит /- или *-, идет замена на введенный символ
                else if (Found("+/-*", 1))
                {
                    if (!btn.equals("-") || (Found("-+", 1) && btn.equals("-")))
                    {
                        textView.setText(textView.getText().subSequence(0, len-1));
                    }
                } // если стоит уже знак, идет замена на введенный символ
                textView.setText(textView.getText()+btn); // в иной ситуации ставим знак
                comma = true;
            } // если строка не "-" и последние 2 знака не "(-", то можно
        }
    } // знаки *+/-
    public void Comma_Click(View v) {
        if (textView.getText() == Double.toString(Double.NaN) || textView.getText().toString().contains("∞"))
        {
            textView.setText("");
            comma = true;
        } // строка очищается, если там есть 2 этих значения

        int len = textView.getText().toString().length(); // длина строки примера

        if (comma)
        {
            if (len == 0 || Found("*-/+()%", 1)) textView.setText(textView.getText()+"0,");
            else textView.setText(textView.getText()+",");
            comma = false;
        }
    } // знак запятой
    public void Percent_Click(View v) {
        if (textView.getText() == Double.toString(Double.NaN) || textView.getText().toString().contains("∞"))
        {
            textView.setText("");
            comma = true;
        } // строка очищается, если там есть 2 этих значения

        if (textView.getText().length() > 0 && (Found("1234567890,)", 1)))
        {
            textView.setText(textView.getText()+"%");
            comma = true;
        } // % можно ставить только после цифр или запятой
    } // знак %
    public void Brackets_Click(View v) {
        String btn = ((Button)v).getText().toString(); // текст из кнопки

        if (textView.getText() == Double.toString(Double.NaN) || textView.getText().toString().contains("∞"))
        {
            textView.setText("");
            comma = true;
        } // строка очищается, если там есть 2 этих значения

        if (btn.equals("(") || (btn.equals(")") && (CanPutBracket()) && !Found("+*-/(", 1)))
        {
            textView.setText(textView.getText()+btn);
            comma = true;
        } // Если (, просто ставим; если ), то проверяем есть ли доступ к постановке
    } // знаки ( и )
    public void Delete_Click(View v) {
        String btn = ((Button)v).getText().toString(); // текст из кнопки
        int len = textView.getText().toString().length(); // длина строки примера

        if (textView.getText() == Double.toString(Double.NaN) || textView.getText().toString().contains("∞") || btn.equals("C"))
        {
            textView.setText("");
            comma = true;
        } // строка очищается, если там есть 2 этих значения
        else if (btn.equals("D") && len > 0)
        {
            if (Found(",", 1)) comma = true;
            textView.setText(textView.getText().subSequence(0, len-1));
        } // стирание 1 символа
    } // знаки С и <=
    public void Equals_Click(View v) {
        //try
        //{
            if (textView.getText().length() > 0 && Found("/-+*(", 1)){}// throw new Exception(); // если пример не окончен - ошибка
            else
            {
                while (CanPutBracket()) textView.setText(textView.getText()+")"); // если скобки не доставлены - ставим их, чтобы закончить пример
                textView.setText(textView.getText()+"="); // чтобы проще проверять следом идущий элемент

                String num = ""; // строка для построения чисел
                String str1 = textView.getText().toString().replace(',', '.'); // короткая переменная для сохранения текста из textbox
                char[] str = str1.toCharArray();
                int len = textView.getText().length();  // длина примера
                ArrayList<String> arr = new ArrayList<>(); // список, в котором будет храниться пример

                for (int i = 0; i < len; i++)
                {
                    if ("1234567890.E".indexOf(str[i]) > -1)
                    {
                        if (i > 0 && str[i - 1] == '%') arr.add("*"); // 5%5 -> 5%*5
                        if (i>0 && str[i-1] == ')') arr.add("*"); // ..)12 -> ..)*12
                        num += str[i];
                    } // цифры и запятая собираются в число num
                    else if ("()%/+*=".indexOf(str[i]) > -1)
                    {
                        if (!num.equals(""))
                        {
                            arr.add(num);
                            num = "";
                        } // если выставлен знак и num не пустой, то мы добавляем его в список и опустошаем
                        if (i > 0 && "1234567890.%)".indexOf(str[i-1]) > -1 && str[i] == '(') arr.add("*"); // 12( -> 12*(
                        if (str[i] != '=') arr.add(String.valueOf(str[i])); // = никуда не идет

                    }
                    else if (str[i] == '-')
                    {
                        if (!num.equals(""))
                        {
                            arr.add(num);
                            num = "";
                        } // если выставлен знак и num не пустой, то мы добавляем его в список и опустошаем

                        if (i == 0 || (i > 0 && "/*(".indexOf(str[i-1]) > -1))
                        {
                            num += str[i];
                        } // если строка пуста или /- *- (
                        else arr.add(String.valueOf(str[i]));
                    } // отдельное условие для -
                } // преобразование строки

               // for (String str1 : arr) example.setText(str1);
                if (arr.size()>0) Execution(arr); // решаем пример, если в списке хотя бы есть 1 элемент
              //  else throw new Exception();

                if (textView.getText().toString().indexOf(".") > -1) comma = false;
                else comma = true;
            }
       // }
//       catch (Exception e) {
//            //throw new RuntimeException(e);
//            example.setText(e.toString());
//            if (Found("=", 1)) textView.setText(textView.getText().subSequence(0, textView.getText().length()-1));
//
//        } // при отлове ошибки в label выводится сообщение, а также стирает =, если оно стоит
    }
    private void Execution(ArrayList<String> arr) {
        example.setText(textView.getText());// старый пример записывается в label
        if (arr.size() > 2)
        {
            for (int i = 0; i < arr.size() - 2; i++)
            {
                if (arr.get(i).contains("E") && arr.get(i + 1).equals("+"))
                {
                    arr.set(i, String.format("%.12f", Double.parseDouble(arr.get(i) + arr.get(i + 1) + arr.get(i + 2))));
                         //   toString("F12"));
                    RemoveRange(arr, i+1, 2);
                }
            }
        }
        while (arr.contains("(")) CalculationWithBrackets(arr, InBrackets(arr)); // производятся действия со скобками
        textView.setText(Calculation(arr)); // ответ в текстбокс

        saveExample(example.getText()+"=" +textView.getText());
    }
    private String Calculation(ArrayList<String> arr) {
        while (arr.size() != 1)
        {
            MultyDivide(arr); // */
            Percent(arr); // %
            PlusMinus(arr); // +-
        }
        return arr.get(0);
    } // производит операции по вычислению всех действий в примере

    /// <summary>
    /// Выполняется действие в скобках. Находятся индексы ( и ). Затем копируется список между скобками.
    /// После этого новый список отправляется на вычисление и ответ ставится на место ( и удаляются лишние элементы.
    /// </summary>
    /// <param name="arr"></param>
    /// <returns> возвращает индекс на ( </returns>
    private int InBrackets(ArrayList<String> arr) {
        int startInd = arr.lastIndexOf("("); // индекс последней открывающей скобки
        int endInd = IndexOfStart(arr, ")", startInd+1); // индекс первой открывающейся скобки ПОСЛЕ (
        ArrayList<String> newArr = new  ArrayList<>(arr.subList(startInd+1, endInd));
              //  GetRange(startInd + 1, endInd - startInd - 1); // делаем срез списка от ( до ), не включая их
        arr.set(startInd, Calculation(newArr));  // вычисляем данный массив и ставим на место "("
        RemoveRange(arr, startInd + 1, endInd - startInd); // удаляем оставшиеся элементы от а+1 до ")"
        return startInd;
    }
    private int IndexOfStart(ArrayList arr, String str, int startInd) {
        for (int i=startInd; i<arr.size(); i++)
        {
           if (arr.get(i).equals(str)) return i;
        }
        return -1;
    }
    private void CalculationWithBrackets(ArrayList<String> arr, int startInd) {
        if (arr.size()-1>startInd && arr.get(startInd + 1).equals("%"))
        {
            arr.set(startInd, String.valueOf(Double.parseDouble(arr.get(startInd))/100));
            //arr[startInd] = (Convert.ToDouble(arr[startInd]) / 100).ToString();
            RemoveRange(arr,startInd+1, 1);
        } // (12+2)%
        if ((startInd == 1 || (startInd > 1 && (arr.get(startInd - 2).equals("*") || arr.get(startInd - 2).equals("/")))) && arr.get(startInd - 1).equals("-"))
        {
            arr.set(startInd-1, String.valueOf(Double.parseDouble(arr.get(startInd))*-1));
           // arr[startInd - 1] = (Convert.ToDouble(arr[startInd]) * -1).ToString();
            RemoveRange(arr, startInd, 1);
        } // -(12+4)
    } // действия рядом со скобками

    /// <summary>
    /// Метод отвечает за действие */. Цикл идет по примеру от 0 индекса и при нахождении
    /// знака запускает определенный метод. После этого индекс обнуляется к началу примера и
    /// приходится по нему снова. Так пример не ломается.
    /// </summary>
    /// <param name="arr"></param>
    private void MultyDivide(ArrayList<String> arr) {
        for (int i = 0; i < arr.size(); i++)
        {
            if (arr.get(i).equals("*"))
            {
                //MessageBox.Show("*");
                Multiply(arr, i);
                i = 0;
            }
            if (arr.get(i).equals("/"))
            {
                //MessageBox.Show("/");
                Divide(arr, i);
                i = 0;
            }
        }
    }
    private void Divide(ArrayList<String> arr, int i) {
        if ((arr.get(i - 1).equals("%")) && (arr.size() - i - 2 > 0 && arr.get(i+2).equals("%")))
        {
            arr.set(i-2, String.valueOf(Double.parseDouble(arr.get(i-2))/Double.parseDouble(arr.get(i+1))));
            RemoveRange(arr, i - 1, 4);
           // arr[i - 2] = ((Convert.ToDouble(arr[i - 2])) / (Convert.ToDouble(arr[i + 1]))).ToString();
        }
        else if (arr.get(i - 1).equals("%"))
        {
            arr.set(i-2, String.valueOf(Double.parseDouble(arr.get(i-2))/Double.parseDouble(arr.get(i+1))/100));
            RemoveRange(arr, i - 1, 3);
            //arr[i - 2] = ((Convert.ToDouble(arr[i - 2])) / (Convert.ToDouble(arr[i + 1])) / 100).ToString();
        }
        else if (arr.size() - i - 2 > 0 && arr.get(i+2).equals("%"))
        {
            //arr[i - 1] = ((Convert.ToDouble(arr[i - 1])) / (Convert.ToDouble(arr[i + 1])) * 100).ToString();
            arr.set(i-1, String.valueOf(Double.parseDouble(arr.get(i-1))/Double.parseDouble(arr.get(i+1))*100));
            RemoveRange(arr, i, 3);
        }
        else
        {
            arr.set(i-1, String.valueOf(Double.parseDouble(arr.get(i-1))/Double.parseDouble(arr.get(i+1))));
            // arr[i - 1] = ((Convert.ToDouble(arr[i - 1])) * (Convert.ToDouble(arr[i + 1]))).ToString();
            RemoveRange(arr, i, 2);
        }
    } // вычисление деления
    private void Multiply(ArrayList<String> arr, int i) {
        if ((arr.get(i - 1).equals("%")) && (arr.size() - i - 2 > 0 && arr.get(i + 2).equals("%")))
        {
            arr.set(i-2, String.valueOf(Double.parseDouble(arr.get(i-2))*Double.parseDouble(arr.get(i+1))/10000));
            //arr[i - 2] = ((Convert.ToDouble(arr[i - 2])) * (Convert.ToDouble(arr[i + 1])) / 10000).ToString();
            RemoveRange(arr, i - 1, 4);
        }
        else if (arr.get(i - 1).equals("%"))
        {
            arr.set(i-2, String.valueOf(Double.parseDouble(arr.get(i-2))*Double.parseDouble(arr.get(i+1))/100));
            RemoveRange(arr, i - 1, 3);
          //  arr[i - 2] = ((Convert.ToDouble(arr[i - 2])) * (Convert.ToDouble(arr[i + 1])) / 100).ToString();
        }
        else if (arr.size() - i - 2 > 0 && arr.get(i+2).equals("%"))
        {
            arr.set(i-1, String.valueOf(Double.parseDouble(arr.get(i-1))*Double.parseDouble(arr.get(i+1))/100));
            //arr[i - 1] = ((Convert.ToDouble(arr[i - 1])) * (Convert.ToDouble(arr[i + 1])) / 100).ToString();
            RemoveRange(arr, i, 3);
        }
        else
        {
            arr.set(i-1, String.valueOf(Double.parseDouble(arr.get(i-1))*Double.parseDouble(arr.get(i+1))));
           // arr[i - 1] = ((Convert.ToDouble(arr[i - 1])) * (Convert.ToDouble(arr[i + 1]))).ToString();
            RemoveRange(arr, i, 2);
        }
    } // вычисление умножения
    private void Percent(ArrayList<String> arr) {
        for (int i = 0; i < arr.size(); i++)
        {
            if (arr.get(i).equals("%"))
            {
                //   MessageBox.Show("%");
                if (i > 1 && "+-*/".contains(arr.get(i - 2))) return;
                else
                {
                    arr.set(i - 1, String.valueOf((Double.parseDouble(arr.get(i-1))) / 100));
                    RemoveRange(arr, i, 1);
                }
            }
        }
    } // вычисления процента
    private void PlusMinus(ArrayList<String> arr) {
        for (int i = 0; i < arr.size(); i++)
        {
            if (arr.get(i).equals("+"))
            {
                //MessageBox.Show("+");
                Addition(arr, i);
                i = 0;
            }
            if (arr.get(i).equals("-"))
            {
                // MessageBox.Show("-");
                Subtraction(arr, i);
                i = 0;
            }
        }
    }
    private void Addition(ArrayList<String> arr, int i) {
        if (i + 2 < arr.size() && arr.get(i + 2).equals("%"))
        {
            arr.set(i - 1, String.valueOf(Double.parseDouble(arr.get(i - 1)) * (1 + Double.parseDouble(arr.get(i + 1)) / 100)));
            RemoveRange(arr, i, 3);
        }
        else
        {
            arr.set(i - 1, String.valueOf(Double.parseDouble(arr.get(i - 1)) + Double.parseDouble(arr.get(i + 1))));
            RemoveRange(arr, i, 2);
        }
    } // вычисление суммы
    private void Subtraction(ArrayList<String> arr, int i) {
        if (i + 2 < arr.size() && arr.get(i + 2).equals("%"))
        {
            arr.set(i - 1, String.valueOf(Double.parseDouble(arr.get(i - 1)) * (1 - Double.parseDouble(arr.get(i + 1)) / 100)));
            RemoveRange(arr, i, 3);
        }
        else
        {
            arr.set(i - 1, String.valueOf(Double.parseDouble(arr.get(i - 1)) - Double.parseDouble(arr.get(i + 1))));
            RemoveRange(arr, i, 2);
        }
    } // вычисление разницы

//    private void textBox1_KeyPress(object sender, KeyPressEventArgs e)
//    {
//        if (!("1234567890()E%,/*+- ".Contains(e.KeyChar)))
//        {
//            e.Handled = true;
//        }
//    } // не дает писать в поле для текста плохие символы
//
//    private void textBox1_KeyDown(object sender, KeyEventArgs e)
//    {
//        if (e.KeyCode == Keys.V && e.Control)
//        {
//            // Получаем текст из буфера обмена
//            string clipboardText = Clipboard.GetText();
//
//            // Проверяем вставляемый текст на наличие запрещенных символов
//            if (!ContainsForbiddenCharacters(clipboardText))
//            {
//                // Запрещаем вставку текста с запрещенными символами
//                e.Handled = true;
//                e.SuppressKeyPress = true;
//                MessageBox.Show("Вставка содержит запрещенные символы.");
//            }
//            else textBox1.Text += clipboardText;
//        }
//        else if ( e.KeyCode == Keys.Back)
//        {
//            int cursorPos = textBox1.SelectionStart;
//
//            // Проверяем, не является ли курсор началом текста
//            if (cursorPos > 0)
//            {
//                // Удаляем символ слева от позиции курсора
//                textBox1.Text = textBox1.Text.Remove(cursorPos - 1, 1);
//
//                // Перемещаем курсор на один символ влево
//                textBox1.SelectionStart = cursorPos - 1;
//            }
//
//            // Предотвращаем дальнейшую обработку нажатия клавиши Backspace
//            e.Handled = true;
//        }
//    } // событие на CTRL + V и backspace
//
    private boolean ContainsForbiddenCharacters(String text) {
        String validChars = "1234567890/*+-E%,() ";
        for (char c : text.toCharArray()) {
            if (validChars.indexOf(c) == -1) {
                return false;
            }
        }
        return true;
    } // выполняется проверка буфера обмена, проверяется, можно ли вставить текст
    private boolean CanPutBracket() {
        String text = textView.getText().toString();
        int countOpen = 0;
        int countClose = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '(') {
                countOpen++;
            } else if (c == ')') {
                countClose++;
            }
        }
        return countOpen > countClose;
    } // проверка на возможность ставить )
    private boolean Found(String signs, int i) {
        char[] str = textView.getText().toString().toCharArray();
        return signs.indexOf(str[str.length - i]) > -1;
    } // метод для поиска в строке

    private void RemoveRange(ArrayList<String> arr, int index, int count) {
        for (int i = 0; i<count; i++) {
            arr.remove(index);
        }
    }
}