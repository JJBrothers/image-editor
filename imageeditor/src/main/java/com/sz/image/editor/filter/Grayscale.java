package com.sz.image.editor.filter;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.request.target.SquaringDrawable;
import com.sz.image.editor.dialog.CustomProgressDialog;

import jp.wasabeef.glide.transformations.CropSquareTransformation;
import jp.wasabeef.glide.transformations.GrayscaleTransformation;


/**
 * Created by jhpark on 2016. 1. 14..
 */
public class Grayscale extends Filter{

  private final String GRAYSCALE = "graysacle";
  private Context mContext;
  private String mTargetPath;
  private int mTargetWidth;
  private int mTargetHeight;
  private boolean isFragmentShowingOnScreen = false;

  public Grayscale(Context context,String targetPath, int width, int height){
    mContext = context;
    mTargetPath = targetPath;
    mTargetWidth = width;
    mTargetHeight = height;
  }

  @Override
  public void assignFilterImage(TextView textView, ImageView imageView) {
    Glide.clear(imageView);

    Glide.with(mContext)
            .load(mTargetPath)
            .override(mTargetWidth, mTargetHeight)
            .skipMemoryCache(FILTER_IMAGE_CACHE)
            .diskCacheStrategy(FILTER_IMAGE_DISK_CACHE)
            .bitmapTransform(new GrayscaleTransformation(mContext), new CropSquareTransformation(mContext))
            .dontAnimate()
            .thumbnail(FILTER_IMAGE_LOADING_ALPHA)
            .into(imageView);
    String filterName = Type.GRAYSCALE.getName();
    textView.setText(filterName);

    Glide.get(mContext).clearMemory();
  }

  @Override
  public void getBitmap(final Callback callback) {


    new AsyncTask<Void, Void, Drawable>() {
      Bitmap bitmap;
      Drawable drawable;
      ProgressDialog progressDialog;

      @Override
      protected void onPreExecute() {
        super.onPreExecute();
        if(isFragmentShowingOnScreen){
          progressDialog = CustomProgressDialog.createPlainProgressDialog(mContext);
        }
      }

      @Override
      protected Drawable doInBackground(Void... params) {
        BitmapPool pool = Glide.get(mContext).getBitmapPool();
        try{

          drawable = Glide.with(mContext)
                  .load(mTargetPath)
                  .skipMemoryCache(MAIN_IMAGE_CACHE)
                  .diskCacheStrategy(MAIN_IMAGE_DISK_CACHE)
                  .bitmapTransform(new GrayscaleTransformation(mContext))
                  .into(mTargetWidth, mTargetHeight)
                  .get();
        }catch (Exception e){
          e.printStackTrace();
          return null;
        }
        return drawable;
      }
      @Override
      protected void onPostExecute(Drawable drawable) {

        if(drawable == null){
          callback.callbackBitmap(null);
          return;
        }

        if (drawable instanceof TransitionDrawable)
        {
          drawable = ((TransitionDrawable)drawable).getDrawable(1);
        }

        if (drawable instanceof BitmapDrawable)
        {
          bitmap = ((BitmapDrawable)drawable).getBitmap();
        }
        else if (drawable instanceof GlideBitmapDrawable)
        {
          bitmap = ((GlideBitmapDrawable)drawable).getBitmap();
        }
        else if (drawable instanceof SquaringDrawable)
        {
          SquaringDrawable sq = (SquaringDrawable)drawable;
          bitmap = ((GlideBitmapDrawable)sq.getCurrent()).getBitmap();
        }

        callback.callbackBitmap(bitmap);
        Log.i(GRAYSCALE, "비동기 콜백완료");
        if(isFragmentShowingOnScreen){
          progressDialog.dismiss();
        }
      }

    }.execute();
    Glide.get(mContext).clearMemory();
  }

  public void isFragmentShowingOnScreen(boolean bool){
    isFragmentShowingOnScreen = bool;
  }
}
