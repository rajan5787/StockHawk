package com.example.rajan.stockhawk.data;

import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

/**
 * Created by sam_chordas on 10/5/15.
 */
@ContentProvider(authority = QuoteProvider.AUTHORITY, database = QuoteDatabase.class)
public class QuoteProvider {
  public static final String AUTHORITY = "com.example.rajan.stockhawk.data.QuoteProvider";

  static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

  interface Path{
    String QUOTES = "quotes";
    String QUOTE_HISTORY = "quoteList";
  }

  private static Uri buildUri(String... paths){
    Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
    for (String path:paths){
      builder.appendPath(path);
    }
    return builder.build();
  }

  @TableEndpoint(table = QuoteDatabase.QUOTES)
  public static class Quotes{
    @ContentUri(
        path = Path.QUOTES,
        type = "vnd.android.cursor.dir/quote"
    )
    public static final Uri CONTENT_URI = buildUri(Path.QUOTES);

    @InexactContentUri(
        name = "QUOTE_ID",
        path = Path.QUOTES + "/*",
        type = "vnd.android.cursor.item/quote",
        whereColumn = QuoteColumns.SYMBOL,
        pathSegment = 1
    )
    public static Uri withSymbol(String symbol){
      return buildUri(Path.QUOTES, symbol);
    }
  }

  @TableEndpoint(table = QuoteDatabase.QUOTE_HISTORY)
  public static class QuoteHistory{
    @ContentUri(
            path = Path.QUOTE_HISTORY,
            type = "vnd.android.cursor.dir/quoteHistory"
    )
    public static final Uri CONTENT_URI = buildUri(Path.QUOTE_HISTORY);

    @InexactContentUri(
            name = "QUOTE_ID",
            path = Path.QUOTE_HISTORY + "/*",
            type = "vnd.android.cursor.item/quoteHistory",
            whereColumn = QuoteHistoryColumns.SYMBOL,
            pathSegment = 1
    )
    public static Uri withSymbol(String symbol){
      return buildUri(Path.QUOTE_HISTORY, symbol);
    }
  }
}
