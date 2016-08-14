package com.example.rajan.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.rajan.stockhawk.R;
import com.example.rajan.stockhawk.data.QuoteColumns;
import com.example.rajan.stockhawk.data.QuoteProvider;


/**
 * Created by rajan on 10/06/16.
 */
public class QuoteWidgetRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new QuoteWidgetRemoteViewsServiceFactory(this.getApplicationContext(), intent);
    }

}

/**
 * This is the factory that will provide data to the collection widget.
 */
class QuoteWidgetRemoteViewsServiceFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext;
    private Cursor mCursor;

    public QuoteWidgetRemoteViewsServiceFactory(Context context, Intent intent) {
        mContext = context;
    }

    public void onCreate() {
        // Since we reload the cursor in onDataSetChanged() which gets called immediately after
        // onCreate(), we do nothing here.
    }

    public void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
        }
    }

    public int getCount() {
        return mCursor.getCount();
    }

    public RemoteViews getViewAt(int position) {
        // Get the data for this position from the content provider
        String stockSymbol = "";

        if (mCursor.moveToPosition(position)) {
            stockSymbol = mCursor.getString(mCursor.getColumnIndex(QuoteColumns.SYMBOL));
        }

        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_collection_item);
        rv.setTextViewText(R.id.stock_symbol, stockSymbol);

        if (mCursor.getInt(mCursor.getColumnIndex("is_up")) == 1){
            rv.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
        } else{
            rv.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
        }

        rv.setTextViewText(R.id.change,mCursor.getString(mCursor.getColumnIndex("percent_change")));

        return rv;
    }
    public RemoteViews getLoadingView() {
        // We aren't going to return a default loading view in this sample
        return null;
    }

    public int getViewTypeCount() {
        // Technically, we have two types of views (the dark and light background views)
        return 1;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }

    public void onDataSetChanged() {
        // Refresh the cursor
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{ QuoteColumns.SYMBOL, QuoteColumns.PERCENT_CHANGE, QuoteColumns.ISUP},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
    }
}