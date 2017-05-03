package com.udacity.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.support.v4.content.ContextCompat;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Quotes widget remote views service.
 */

public class QuotesWidgetRemoteViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new QuotesWidgetRemoteViewsFactory(this, intent);
    }

    private static class QuotesWidgetRemoteViewsFactory implements RemoteViewsFactory {

        private Context context;
        private int appWidgetId;
        private Cursor data;

        QuotesWidgetRemoteViewsFactory(Context context, Intent intent) {
            this.context = context;

            appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

        }

        @Override
        public void onCreate() {
            // Do nothing
        }

        private void initData() {

            closeGracefully();

            ContentResolver resolver = context.getContentResolver();

            long identityToken = Binder.clearCallingIdentity();
            data = resolver.query(Contract.Quote.URI, null, null, null,Contract.Quote.COLUMN_SYMBOL);
            Binder.restoreCallingIdentity(identityToken);
        }

        private void closeGracefully() {
            if (data != null) {
                data.close();
                data = null;
            }
        }

        @Override
        public void onDataSetChanged() {
             initData();
        }

        @Override
        public void onDestroy() {
            closeGracefully();
        }

        @Override
        public int getCount() {
            return data == null ? 0 : data.getCount();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews view = new RemoteViews(context.getPackageName(),
                    R.layout.list_item_widget_quote);

            if (data.moveToPosition(position)) {
                String symbol = data.getString(Contract.Quote.POSITION_SYMBOL);
                float price = data.getFloat(Contract.Quote.POSITION_PRICE);
                float rawAbsoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);

                DecimalFormat dollarFormat =
                        (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                DecimalFormat dollarFormatWithPlus =
                        (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                dollarFormatWithPlus.setPositivePrefix("+$");

                view.setTextViewText(R.id.list_item_widget_stock_symbol, symbol);
                view.setTextViewText(R.id.list_item_widget_stock_price, dollarFormat.format(price));
                view.setTextViewText(R.id.list_item_widget_stock_change, dollarFormatWithPlus.format(rawAbsoluteChange));
                view.setTextColor(R.id.list_item_widget_stock_change,
                        rawAbsoluteChange < 0 ?
                                ContextCompat.getColor(context, R.color.material_red_700) :
                                ContextCompat.getColor(context, R.color.material_green_700)
                );
            }

            return view;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
