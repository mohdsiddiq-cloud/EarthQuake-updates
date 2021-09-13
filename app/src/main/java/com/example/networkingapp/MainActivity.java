package com.example.networkingapp;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    wordAdapter adapter;

    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String USGS_REQUEST_URL ="https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&orderby=time&minmag=5&limit=10";

    private TextView mEmptyStateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView earthquakeListView=(ListView) findViewById(R.id.list);
        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        earthquakeListView.setEmptyView(mEmptyStateTextView);


      quakeReport quake=new quakeReport();
       quake.execute(USGS_REQUEST_URL);

    }

   public class quakeReport extends AsyncTask<String,Void,ArrayList<words>> {

       private final String LOG_TAG = quakeReport.class.getName();

       @Override
       protected ArrayList<words> doInBackground(String... strings) {
           ArrayList<words> result= null;
           try {
               result = QueryUtils.fetchEarthQuakeData(USGS_REQUEST_URL);
           } catch (IOException e) {
               e.printStackTrace();
           }
           return result;
        }

        @Override
        protected void onPostExecute(ArrayList<words> result) {

            View loadingIndicator = findViewById(R.id.loading_indicator);
           ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected =  activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            // If network active start fetching data
            if (isConnected) {
                mEmptyStateTextView.setText(R.string.no_earthquakes);
                Log.i("Information","Calling initLoader()");
                loadingIndicator.setVisibility(View.VISIBLE);

            } else if(! isConnected){
                mEmptyStateTextView.setText("No internet connection");
           }
            loadingIndicator.setVisibility(View.GONE);
            updateUI(result);
       }
    }

   public void updateUI(ArrayList<words> result){
       ListView earthquakeListView = (ListView) findViewById(R.id.list);

       // Create a new {@link ArrayAdapter} of earthquakes
        adapter = new wordAdapter(this, result);

        // Set the adapter on the {@link ListView}
       // so the list can be populated in the user interface
       earthquakeListView.setAdapter(adapter);

       earthquakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                words current= adapter.getItem(position);
                Uri earthquakeUri = Uri.parse(current.getMurl());

               // Create a new intent to view the earthquake URI
               Intent websiteIntent = new Intent(Intent.ACTION_VIEW, earthquakeUri);

                // Send the intent to launch a new activity
                startActivity(websiteIntent);
            }
        });
   }
    }
