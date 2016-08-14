package com.example.rajan.stockhawk.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.TaskParams;

/**
 * Created by sam_chordas on 10/1/15.
 */
public class QuoteHistoryIntentService extends IntentService {

  public QuoteHistoryIntentService(){
    super(QuoteHistoryIntentService.class.getName());
  }

  public QuoteHistoryIntentService(String name) {
    super(name);
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    Log.d(QuoteHistoryIntentService.class.getSimpleName(), "QuoteHistory Intent Service");
    QuoteHistoryTaskService stockTaskService = new QuoteHistoryTaskService(this);
    Bundle args = new Bundle();
    args.putString("symbol", intent.getStringExtra("symbol"));
    args.putString("startDate", intent.getStringExtra("startDate"));
    args.putString("endDate", intent.getStringExtra("endDate"));
    // We can call OnRunTask from the intent service to force it to run immediately instead of
    // scheduling a task.
    stockTaskService.onRunTask(new TaskParams(intent.getStringExtra("tag"), args));
  }
}
