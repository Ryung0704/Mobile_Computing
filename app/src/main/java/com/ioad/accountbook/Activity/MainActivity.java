package com.ioad.accountbook.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.annotation.SuppressLint;
import android.widget.Button;
import android.widget.EditText;


import com.ioad.accountbook.Adapter.MainRecyclerAdapter;
import com.ioad.accountbook.DAO.Content;
import com.ioad.accountbook.R;
import com.ioad.accountbook.SQL.AccountBookDB;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    String TAG = getClass().getSimpleName();

    CalendarView calendarView;
    TextView tv_now_month, tv_income_tot, tv_export_tot, tv_month_income_tot, tv_month_expend_tot, tv_difference_tot;
    Button btn_newData, button2;
    LinearLayout ll_tot;

    //    MainListAdapter incomeAdapter;
//    MainListAdapter exportAdapter;
    RecyclerView.LayoutManager incomeLayoutManager;
    RecyclerView.LayoutManager exportLayoutManager;
    RecyclerView.Adapter incomeAdapter;
    RecyclerView.Adapter exportAdapter;

//    MainRecyclerAdapter incomeAdapter;
//    MainRecyclerAdapter exportAdapter;

    AccountBookDB dataBase;
    ArrayList<Content> incomeData = new ArrayList<>();
    ArrayList<Content> exportData = new ArrayList<>();

    Calendar cal = Calendar.getInstance();
    SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
    SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
    String selectDay = format.format(Calendar.getInstance().getTime());
    String nowMonth = monthFormat.format(Calendar.getInstance().getTime());
    String year = "";
    String month = "";
    String day = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataBase = new AccountBookDB(getApplicationContext());

        calendarView = findViewById(R.id.calendarView);
//
        btn_newData = findViewById(R.id.btn_newData);
        tv_now_month = findViewById(R.id.tv_now_month);
        tv_income_tot = findViewById(R.id.tv_income_tot);
        tv_export_tot = findViewById(R.id.tv_export_tot);
        ll_tot = findViewById(R.id.ll_tot);
        tv_month_income_tot = findViewById(R.id.tv_month_income_tot);
        tv_month_expend_tot = findViewById(R.id.tv_month_expend_tot);
        tv_difference_tot = findViewById(R.id.tv_difference_tot);

        incomeLayoutManager = new LinearLayoutManager(this);
        exportLayoutManager = new LinearLayoutManager(this);
        setNowMonth(nowMonth);
        getDetailDate(selectDay);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int day) {
                month += 1;
                if (month >= 10) {
                    if (day >= 10) {
                        selectDay = Integer.toString(year) + Integer.toString(month) + Integer.toString(day);
                    } else {
                        selectDay = Integer.toString(year) + Integer.toString(month) + "0" + Integer.toString(day);
                    }
                } else {
                    if (day >= 10) {
                        selectDay = Integer.toString(year) + "0" + Integer.toString(month) + Integer.toString(day);
                    } else {
                        selectDay = Integer.toString(year) + "0" + Integer.toString(month) + "0" + Integer.toString(day);
                    }
                }
                getDetailDate(selectDay);

                Toast.makeText(getApplicationContext(), selectDay, Toast.LENGTH_SHORT).show();
                incomeData.clear();
                exportData.clear();
                getListData();
                getTotalData();

            }
        });


        btn_newData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NewDataActivity.class);
                intent.putExtra("DATE", selectDay);
                startActivity(intent);
            }
        });

        Button Button2 = (Button)findViewById(R.id.button2);
        Button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),calculator.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        incomeData.clear();
        exportData.clear();
        getListData();
        getTotalData();
    }

    public void getListData() {

        String incomeTotStr = null;
        String exportTotStr = null;
        int incomeTot = 0;
        int exportTot = 0;

        SQLiteDatabase DB;
        String query;

        try {
            DB = dataBase.getReadableDatabase();
            query = "SELECT seq_num, m_type, m_amount, m_detail FROM moneybook " +
                    "WHERE m_year = '" + year + "'" +
                    "AND m_month = '" + month + "' " +
                    "AND m_day = '" + day + "';";

            Cursor cursor = DB.rawQuery(query, null);
            Log.e(TAG, query);
            while (cursor.moveToNext()) {
                String seq = cursor.getString(0);
                String type = cursor.getString(1);
                String amount = cursor.getString(2);
                String kind = cursor.getString(3);

                Log.e(TAG, seq);
                Content content = new Content(seq, type, amount, kind, selectDay);

                if (type.equals("income")) {

                    incomeData.add(content);
                    incomeTot += Integer.parseInt(amount);

                    DecimalFormat myFormatter = new DecimalFormat("###,###");
                    incomeTotStr = myFormatter.format(incomeTot);
                } else {

                    exportData.add(content);

                    exportTot += Integer.parseInt(amount);
                    DecimalFormat myFormatter = new DecimalFormat("###,###");
                    exportTotStr = myFormatter.format(exportTot);
                }

                if (incomeData.isEmpty()) {
                    incomeTotStr = "0";
                }

                if (exportData.isEmpty()) {
                    exportTotStr = "0";
                }
            }

            cursor.close();
            dataBase.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {

            if (incomeData.isEmpty() && exportData.isEmpty()) {

                ll_tot.setVisibility(View.INVISIBLE);

            } else if (incomeData.isEmpty() && !exportData.isEmpty()) {

                ll_tot.setVisibility(View.VISIBLE);
                tv_income_tot.setText(incomeTotStr + " 원");
                tv_export_tot.setText(exportTotStr + " 원");

//                exportAdapter = new MainListAdapter(MainActivity.this, R.layout.main_list_layout, exportData);
//                lv_mainList_export.setAdapter(exportAdapter);
                exportAdapter = new MainRecyclerAdapter(MainActivity.this, exportData);

            } else if (!incomeData.isEmpty() && exportData.isEmpty()) {

                ll_tot.setVisibility(View.VISIBLE);
                tv_income_tot.setText(incomeTotStr + " 원");
                tv_export_tot.setText(exportTotStr + " 원");

                incomeAdapter = new MainRecyclerAdapter(MainActivity.this, incomeData);

            } else {
                ll_tot.setVisibility(View.VISIBLE);
                tv_income_tot.setText(incomeTotStr + " 원");
                tv_export_tot.setText(exportTotStr + " 원");


                exportAdapter = new MainRecyclerAdapter(MainActivity.this, exportData);

                incomeAdapter = new MainRecyclerAdapter(MainActivity.this, incomeData);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void getTotalData() {
        SQLiteDatabase DB;
        String query;
        int incomeTot = 0;
        int expendTot = 0;
        String incomeTotStr = "";
        String exportTotStr = "";
        int differenceTot = 0;
        String differenceStr = "";

        try {
            DB = dataBase.getReadableDatabase();
            query = "SELECT m_type, m_amount FROM moneybook " +
                    "WHERE m_month = '" + month + "';";

            Cursor cursor = DB.rawQuery(query, null);

            while (cursor.moveToNext()) {
                String type = cursor.getString(0);
                String amount = cursor.getString(1);

                if (type.equals("income")) {
                    incomeTot += Integer.parseInt(amount);
                    DecimalFormat myFormatter = new DecimalFormat("###,###");
                    incomeTotStr = myFormatter.format(incomeTot);
                } else {
                    expendTot += Integer.parseInt(amount);
                    DecimalFormat myFormatter = new DecimalFormat("###,###");
                    exportTotStr = myFormatter.format(expendTot);
                }

            }


            cursor.close();
            dataBase.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            differenceTot = incomeTot - expendTot;
            DecimalFormat myFormatter = new DecimalFormat("###,###");
            differenceStr = myFormatter.format(differenceTot);

            if (incomeTotStr == null || incomeTotStr.length() == 0) {
                incomeTotStr = "0";
            }

            if (exportTotStr == null || exportTotStr.length() == 0) {
                exportTotStr = "0";
            }
            tv_month_income_tot.setText(incomeTotStr + " 원");
            tv_month_expend_tot.setText(exportTotStr + " 원");
            tv_difference_tot.setText(differenceStr + " 원");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setNowMonth(String currentMonth) {
        int month = Integer.parseInt(currentMonth);
        tv_now_month.setText(Integer.toString(month) + "월");
    }

    private void getDetailDate(String date) {
        year = date.substring(0, 4);
        month = date.substring(4, 6);
        day = date.substring(6, date.length());
    }

}