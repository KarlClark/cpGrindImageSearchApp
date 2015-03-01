package com.clarkgarrett.gridimagesearch.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.clarkgarrett.gridimagesearch.R;
import com.clarkgarrett.gridimagesearch.Utility.EndlessScrollListener;
import com.clarkgarrett.gridimagesearch.adapters.ImageResultsAdapter;
import com.clarkgarrett.gridimagesearch.models.ImageResult;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class SearchActivity extends ActionBarActivity {

    private GridView gvResults;
    private SearchView searchView;
    private TextView tvErrMsg;
    private ArrayList<ImageResult> imageResults;
    private ImageResultsAdapter aImageResults;
    private String query="";
    private String queryExtension= "";
    private boolean newQuery;
    private CharSequence searchText ="";
    int start;
    private boolean needDownload= false;
    private static final String SAVED_QUERY = "saved_query";
    private static final String SAVED_QUERY_EXTENSION = "saved_Ext";
    private static final String SAVED_SEARCH_FIELD = "saved_srch";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Log.i("DEBUG", "Search Activity onCreate called");
        setupViews();
        resetAdvancedSettings();
        imageResults = new ArrayList<ImageResult>();
        aImageResults = new ImageResultsAdapter(this, imageResults);
        gvResults.setAdapter(aImageResults);
        gvResults.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                downLoadImages(false);
            }
        });


        if (savedInstanceState != null){
            Log.i("DEBUG", "savedInstanceState not null");
            query = savedInstanceState.getString(SAVED_QUERY);
            queryExtension = savedInstanceState.getString(SAVED_QUERY_EXTENSION);
            searchText = savedInstanceState.getCharSequence(SAVED_SEARCH_FIELD);
            needDownload = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("DEBUG", "SearchActivity onResume called");
        if (! isOnline()){
            tvErrMsg.setText(getResources().getString(R.string.no_internet_errmsg));
            tvErrMsg.setVisibility(View.VISIBLE);
            return;
        }else{
            tvErrMsg.setVisibility(View.GONE);
            if (needDownload){
                downLoadImages(true);
                needDownload = false;
            }
        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        Log.i("DEBUG","onSavedInstanceState called");
        savedInstanceState.putString(SAVED_QUERY , query);
        savedInstanceState.putString(SAVED_QUERY_EXTENSION, queryExtension);
        savedInstanceState.putCharSequence(SAVED_SEARCH_FIELD, searchView.getQuery());
    }

    private void setupViews(){
        tvErrMsg = (TextView)findViewById(R.id.tvErrMsg);
        gvResults =(GridView)findViewById(R.id.gvResults);
        gvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(SearchActivity.this, ImageDisplayActivity.class);
                ImageResult result = imageResults.get(position);
                i.putExtra("result", result);
                startActivity(i);
            }
        });
    }

    private void resetAdvancedSettings(){
        SharedPreferences prefs = getSharedPreferences(AdvancedSearchActivity.PREFS_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putInt(AdvancedSearchActivity.IMAGE_SIZE, 0);
        prefsEditor.putInt(AdvancedSearchActivity.COLOR_FILTER, 0);
        prefsEditor.putInt(AdvancedSearchActivity.IMAGE_TYPE , 0);
        prefsEditor.putString(AdvancedSearchActivity.SITE_FILTER, "");
        prefsEditor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Log.i("DEBUG" ,"onCreateOptionsMenu called");
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setIconifiedByDefault(true);
        searchView.setIconified(false);
        searchView.setQueryHint(getResources().getString(R.string.query_hint));
        if ( ! searchText.equals("")){
            searchView.setQuery(searchText, false);
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                query = s;
                downLoadImages(true);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        return true;
    }


    private void downLoadImages(boolean setNewQuery){
        newQuery = setNewQuery;
        if (newQuery) {
            start=0;
        }
        AsyncHttpClient client = new AsyncHttpClient();
        String searchUrl = "https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=" + query + "&rsz=8&start=" + start + queryExtension;
        Log.i("DEBUG","query= " + query);
        Log.i("DEBUG", "searchUrl= " + searchUrl);
        start += 8;
        client.get(searchUrl, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //Log.d("DEBUG",response.toString());
                JSONArray imageResultsJson = null;
                try {
                    imageResultsJson = response.getJSONObject("responseData").getJSONArray("results");
                    if (newQuery) {
                        aImageResults.clear();
                    }
                    aImageResults.addAll(ImageResult.fromJSONArray(imageResultsJson));
                }catch (JSONException e){
                    e.printStackTrace();
                }
                //Log.i("DEBUG", imageResults.toString());
            }
        });

    }

    // from clicking settings in acton bar
    public void onSettingsAction(MenuItem item) {
        Intent i = new Intent(this, AdvancedSearchActivity.class);
        startActivityForResult(i, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int ressultCode, Intent data){
        if (data == null) return;
        queryExtension="";
        String s = getResources().getStringArray(R.array.image_size)[0];  //zero element of array
        String extra= data.getStringExtra(AdvancedSearchActivity.IMAGE_SIZE); //what the user chose
        if( ! s.equals(extra)){  // not the "all sizes" case so add to queryExtensionString
            queryExtension= queryExtension + "&imgsz=" + extra;
        }

        s=  getResources().getStringArray(R.array.image_color)[0];
        extra = data.getStringExtra(AdvancedSearchActivity.COLOR_FILTER);
        if( ! s.equals(extra)){
            queryExtension= queryExtension + "&imgcolor=" + extra;
        }

        s=  getResources().getStringArray(R.array.image_type)[0];
        extra=data.getStringExtra(AdvancedSearchActivity.IMAGE_TYPE);
        if( ! s.equals(extra)){
            queryExtension= queryExtension + "&imgtype=" + extra;
        }

        extra=data.getStringExtra(AdvancedSearchActivity.SITE_FILTER);
        if( ! extra.equals("")){
            queryExtension= queryExtension + "&as_sitesearch=" + extra;
        }

        Log.i("DEBUG", "Query = " + query);
        Log.i("DEBUG", "query extension= " + queryExtension);

        // if the user has already done a search, then repeat the search
        // with the new conditions.
        if ( ! query.equals("")) {
            downLoadImages(true);
        }
    }

    public Boolean isOnline() {
        try {
            Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.com");
            int returnVal = p1.waitFor();
            boolean reachable = (returnVal==0);
            Log.i("DEBUG","reachable = " + reachable +  "  return value= " + returnVal);
            return reachable;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e("DEBUG", "e =" + e.toString());
        }
        return false;
    }
}
