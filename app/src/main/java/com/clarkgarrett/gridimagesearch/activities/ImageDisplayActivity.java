package com.clarkgarrett.gridimagesearch.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.clarkgarrett.gridimagesearch.CustomViews.TouchImageView;
import com.clarkgarrett.gridimagesearch.R;
import com.clarkgarrett.gridimagesearch.models.ImageResult;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class ImageDisplayActivity extends ActionBarActivity implements Callback{

    private ShareActionProvider shareActionProvider;
    TouchImageView tivImageResult;
    MenuItem item;
    boolean safeToSetupActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        safeToSetupActionProvider= false;
        Log.i("DEBUG","ImageDisplayActivity onCreate called");
        setContentView(R.layout.activity_image_display_2);
        //getSupportActionBar().hide();
        ImageResult result= (ImageResult)getIntent().getSerializableExtra("result");
        tivImageResult = (TouchImageView)findViewById(R.id.tivImageResult);
        tivImageResult.setMaxZoom(6);
        Picasso.with(this).load(result.fullUrl).into(tivImageResult, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Log.i("DEBUG", "ImageDisplayActivity onCreateOptionsMenu called");
        getMenuInflater().inflate(R.menu.menu_advanced_search, menu);
        item = menu.findItem(R.id.menu_item_share);
        if (safeToSetupActionProvider) {
            setupActionProvider();
        }else{
            safeToSetupActionProvider=true;
        }


        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("DEBUG","ImageDisplayActivity onResume called");
    }

    /*
    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(shareIntent);
        }
    }*/

    private Uri getPictureUri() {

        Drawable drawable = tivImageResult.getDrawable();
        Log.i("DEBUG", "mDrawable = "+ drawable);
        Bitmap mBitmap = ((BitmapDrawable) drawable).getBitmap();
        Log.i("DEBUG", "Bitmap = " + mBitmap);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(),
                mBitmap, "Image Description", null);

        Uri uri = Uri.parse(path);
        return uri;
    }

    public void onError() {
        Log.i("DEBUG", "Picasso onError called");
    }

    public void onSuccess() {
        Log.i("DEBUG", "Picasso onSuccess called");
        if (safeToSetupActionProvider) {
            setupActionProvider();
        }else{
            safeToSetupActionProvider=true;
        }

    }

    private void setupActionProvider(){
        Uri bmpUri = getPictureUri();
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("image/*");
        i.putExtra(Intent.EXTRA_STREAM, bmpUri);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        shareActionProvider.setShareIntent(i);

    }

    @Override
    public boolean onSupportNavigateUp () {
        Log.i("DEBUG","ImageDisplayActivity onNavigateUp called");
        onBackPressed();
        return true;
    }
}
