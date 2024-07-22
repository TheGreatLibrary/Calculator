package com.sinya.example.calculator;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    interface OnStateClickListener {
        void onStateClick(int position, String example);
    } // интерфейс для выполнения клика по ячейке истории

    private ArrayList<String> data; // массив примеров, сохраненных в файле
    private final OnStateClickListener onStateClickListener; // поле-метод для каждой ячейки

    public MyAdapter(ArrayList<String> data, OnStateClickListener onStateClickListener) {
        this.data = data;
        this.onStateClickListener = onStateClickListener;
    } // конструктор
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new ViewHolder(view);
    } // создает и возвращает viewHolder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String[] element = data.get(position).split("="); // данные из файла получаются и деляется знаком "="
        holder.textExample.setText(element[0]);
        holder.textAnswer.setText(element[1]);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onStateClickListener.onStateClick(position, holder.textExample.getText().toString());
            }
        }); // при клике на ячейку подается ее позиция и содержимое
    }
    @Override
    public int getItemCount() {
        return data.size();
    } // выдает количество элементов в списке

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textExample; // пример
        public TextView textAnswer; // ответ
        public ViewHolder(View view) {
            super(view);
            textExample = view.findViewById(R.id.item_example);
            textAnswer = view.findViewById(R.id.item_answer);
        } // конструктор

    } // класс-ячейка истории, там хранятся 2 textView с ответом и самим примером
}
