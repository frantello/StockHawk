package com.udacity.stockhawk.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.opencsv.CSVReader;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * History activity.
 */

public class HistoryActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String QUOTE_SYMBOL_EXTRA = "HistoryActivity.Symbol";

    private static final int QUOTE_LOADER_ID = 0;

    private String symbol;

    private int textColor = 0;

    @BindView(R.id.activity_quote_text)
    TextView text;

    @BindView(R.id.activity_quote_history)
    LineChart history;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        ButterKnife.bind(this);

        textColor = text.getCurrentTextColor();

        setUpChart();

        Intent intent = getIntent();

        if (intent != null) {
            Bundle bundle = intent.getExtras();

            if (bundle.containsKey(QUOTE_SYMBOL_EXTRA)) {

                symbol = bundle.getString(QUOTE_SYMBOL_EXTRA);

                getSupportLoaderManager().initLoader(QUOTE_LOADER_ID, bundle, this);
            }
        }
    }

    private void setUpChart() {

        history.setDescription(null);
        history.getLegend().setTextColor(textColor);

        XAxis xAxis = history.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(4);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return DateFormat.getDateFormat(HistoryActivity.this).format(new Date((long) value));
            }
        });

        xAxis.setTextColor(textColor);
        xAxis.setAxisLineColor(textColor);

        YAxis rightAxis = history.getAxisRight();
        rightAxis.setEnabled(false);

        YAxis leftAxis = history.getAxisLeft();
        leftAxis.setTextColor(textColor);
        leftAxis.setAxisLineColor(textColor);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Loader<Cursor> loader = new CursorLoader(this,
                Contract.Quote.makeUriForStock(symbol),
                null, null, null, null);

        loader.forceLoad();

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data != null && !data.moveToFirst()) {
            return;
        }

        addQuotesDataSet(data.getString(Contract.Quote.POSITION_HISTORY));
    }

    private void addQuotesDataSet(String raw) {

        try {
            List<String[]> values = new CSVReader(new StringReader(raw), ',').readAll();


            List<Entry> entries = new ArrayList<>();

            for (String[] value : values) {

                entries.add(new Entry(Float.valueOf(value[0]), Float.valueOf(value[1])));
            }

            Collections.sort(entries, new Comparator<Entry>() {
                @Override
                public int compare(Entry o1, Entry o2) {
                    return Float.compare(o1.getX(), o2.getX());
                }
            });

            LineDataSet dataSet = new LineDataSet(entries,
                    getString(R.string.activity_history_data_set_label));
            dataSet.setColor(ContextCompat.getColor(this, R.color.chart_line));
            dataSet.setFillColor(ContextCompat.getColor(this, R.color.chart_fill));
            dataSet.setDrawCircleHole(false);
            dataSet.setDrawCircles(false);
            dataSet.setDrawValues(false);
            dataSet.setDrawFilled(true);

            LineData lineData = new LineData(dataSet);

            history.setData(lineData);
            history.invalidate();

        } catch (IOException exception) {
            Timber.e(exception, "Error parsing history");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public static void open(Context context, String symbol) {
        Intent intent = new Intent(context, HistoryActivity.class);
        intent.putExtra(QUOTE_SYMBOL_EXTRA, symbol);

        context.startActivity(intent);
    }
}
