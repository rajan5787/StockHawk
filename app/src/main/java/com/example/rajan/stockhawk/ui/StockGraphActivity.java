package com.example.rajan.stockhawk.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;
import com.example.rajan.stockhawk.R;
import com.example.rajan.stockhawk.data.QuoteColumns;
import com.example.rajan.stockhawk.data.QuoteHistoryColumns;
import com.example.rajan.stockhawk.data.QuoteProvider;
import com.example.rajan.stockhawk.service.QuoteHistoryIntentService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class StockGraphActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private String quoteSymbol = "";

    private Intent mServiceIntent;
    private LineChartView mChartView;

    private static final int CURSOR_LOADER_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_graph);

        quoteSymbol = getIntent().getStringExtra(Intent.EXTRA_TEXT);

        Calendar cal = Calendar.getInstance();
        Date today = cal.getTime();
        cal.add(Calendar.YEAR, -1);

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");

        String startDate = format1.format(cal.getTime());
        String endDate = format1.format(today);

        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        mServiceIntent = new Intent(this, QuoteHistoryIntentService.class);
        if (savedInstanceState == null){

            // Run the initialize task service so that some stocks appear upon an empty database
            mServiceIntent.putExtra("symbol", quoteSymbol);

            mServiceIntent.putExtra("endDate", endDate);
            mServiceIntent.putExtra("startDate", startDate);


            if (isConnected){
                startService(mServiceIntent);
            } else{
                networkToast();
            }
        }

        mChartView = (LineChartView) findViewById(R.id.linechart);
        mChartView.setLabelsColor(Color.parseColor("#ffffff"));

        TextView textViewsymbol = (TextView) findViewById(R.id.quote_symbol);
        textViewsymbol.setText(quoteSymbol);

        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args){
        // This narrows the return to only the stocks that are most current.
        return new CursorLoader(this, QuoteProvider.QuoteHistory.CONTENT_URI,
                new String[]{ QuoteHistoryColumns._ID, QuoteHistoryColumns.SYMBOL,  QuoteHistoryColumns.DATE, QuoteHistoryColumns.CLOSE},
                QuoteColumns.SYMBOL + " = ?",
                new String[]{quoteSymbol},
                "Date ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data){
        if (data != null && data.getCount() > 0) {
            float minimum = 0;
            float maximum = 0;
            String lastMonth = "";

            data.moveToFirst();

            LineSet dataSet = new LineSet();
            dataSet.setColor(Color.parseColor("#758cbb"))
                    .setFill(Color.parseColor("#2d374c"))
                    .setDotsColor(Color.parseColor("#758cbb"))
                    .setThickness(4)
                    .setDashed(new float[]{10f,10f});

            while (data.moveToNext()) {
                String dateString = data.getString(data.getColumnIndex("date"));
                String labelDate = "";

                SimpleDateFormat parseFormat = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", getResources().getConfiguration().locale);

                try{
                    Date date = parseFormat.parse(dateString);

                    String month = monthFormat.format(date);
                    if(!month.equals(lastMonth)) {
                        labelDate = monthFormat.format(date);
                        lastMonth = labelDate;
                    }
                    else
                        labelDate = "";
                } catch(ParseException e) {
                    labelDate = "";
                }


                float close = (float) data.getFloat(data.getColumnIndex("close"));
                if(minimum == 0 || close < minimum)
                    minimum = close;
                if(maximum == 0 || close > maximum)
                    maximum = close;


                dataSet.addPoint(labelDate,  close);
            }

            mChartView.setAxisBorderValues((int) minimum, (int) maximum);

            int step = ((int) maximum - (int)minimum) / 10;

            mChartView.setStep(step);

            mChartView.addData(dataSet);
            mChartView.show();
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader){
    }

    public void networkToast(){
        Toast.makeText(this, getString(R.string.no_internet_data_not_up_to_date), Toast.LENGTH_SHORT).show();
    }

}
