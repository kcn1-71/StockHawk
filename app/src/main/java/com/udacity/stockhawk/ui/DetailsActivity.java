package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.udacity.stockhawk.R;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;


public class DetailsActivity extends AppCompatActivity {

    @BindView(R.id.detail_symbol)
    TextView detailSymbol;
    @BindView(R.id.detail_price)
    TextView detailPrice;
    @BindView(R.id.detail_graph)
    GraphView graphView;

    BigDecimal stockPrice;
    List<HistoricalQuote> stockHistoryList;
    List<HistoricalQuote> reverseList;
    double maxPrice = 0;
    double minPrice = 0;
    String selectedStock;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent detailIntent = getIntent();
        selectedStock = detailIntent.getStringExtra("selectedStock");

        ButterKnife.bind(this);

        //get stock history
        new getHistory().execute();
    }

    private class getHistory extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Calendar from = Calendar.getInstance();
            Calendar to = Calendar.getInstance();
            from.add(Calendar.YEAR, -5); // from 5 years ago

            try {
                Stock stock = YahooFinance.get(selectedStock, from, to, Interval.MONTHLY);
                stockPrice = stock.getQuote().getPrice();
                stockHistoryList = stock.getHistory();

                Collections.reverse(stockHistoryList);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            detailSymbol.setText(selectedStock);
            detailPrice.setText(String.valueOf(stockPrice));

            LineGraphSeries<DataPoint> series = new LineGraphSeries<>();

            for (HistoricalQuote quotes : stockHistoryList) {

                Calendar calendar = quotes.getDate();
                BigDecimal price = quotes.getHigh();

                Date date = calendar.getTime();

                DataPoint dataPoint = new DataPoint(date, price.doubleValue());

                if (price.doubleValue() > maxPrice) {
                    maxPrice = price.doubleValue();
                }

                if (price.doubleValue() < minPrice) {
                    minPrice = price.doubleValue();
                }

                series.appendData(dataPoint, true, 62);
            }

            graphView.addSeries(series);

            graphView.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getApplicationContext()));
            graphView.getGridLabelRenderer().setNumHorizontalLabels(4);

            Calendar minDate = stockHistoryList.get(0).getDate();
            Date lowDate = minDate.getTime();
            Calendar maxDate = stockHistoryList.get(stockHistoryList.size() - 1).getDate();
            Date highDate = maxDate.getTime();

            graphView.getViewport().setMinX(lowDate.getTime());
            graphView.getViewport().setMaxX(highDate.getTime());

            // activate horizontal zooming and scrolling
            graphView.getViewport().setScalable(true);

            // activate horizontal scrolling
            graphView.getViewport().setScrollable(true);

            GridLabelRenderer renderer = graphView.getGridLabelRenderer();
            renderer.setNumHorizontalLabels(4);
            renderer.setNumVerticalLabels(5);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
