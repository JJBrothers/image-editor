package com.sz.image.editor.filter;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
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

import jp.wasabeef.glide.transformations.ColorFilterTransformation;
import jp.wasabeef.glide.transformations.CropSquareTransformation;

/**
 * Created by jhpark on 2016. 5. 7..
 */
public class Fall extends Filter{
  private final String FALL = "fall";
  private final int[] mArgb = {50, 205, 133, 63};
  private Context mContext;
  private String mTargetPath;
  private int mTargetWidth;
  private int mTargetHeight;
  private boolean isFragmentShowingOnScreen = false;

  public Fall(Context context, String targetPath, int width, int height) {
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
            .bitmapTransform(new ColorFilterTransformation(mContext, Color.argb(mArgb[0], mArgb[1], mArgb[2], mArgb[3])), new CropSquareTransformation(mContext))
            .dontAnimate()
            .thumbnail(FILTER_IMAGE_LOADING_ALPHA)
            .into(imageView);
    String fileName = Filter.Type.FALL.getName();
    textView.setText(fileName);

    Glide.get(mContext).clearMemory();
  }

  @Override
  public void getBitmap(final Filter.Callback callback) {


    new AsyncTask<Void, Void, Drawable>() {
      Bitmap bitmap;
      Drawable drawable;
      ProgressDialog progressDialog;

      @Override
      protected void onPreExecute() {
        super.onPreExecute();
        if (isFragmentShowingOnScreen) {
          progressDialog = CustomProgressDialog.createPlainProgressDialog(mContext);
        }
      }

      @Override
      protected Drawable doInBackground(Void... params) {
        BitmapPool pool = Glide.get(mContext).getBitmapPool();
        try {

          drawable = Glide.with(mContext)
                  .load(mTargetPath)
                  .skipMemoryCache(MAIN_IMAGE_CACHE)
                  .diskCacheStrategy(MAIN_IMAGE_DISK_CACHE)
                  .bitmapTransform(new ColorFilterTransformation(mContext, Color.argb(mArgb[0], mArgb[1], mArgb[2], mArgb[3])))
                  .into(mTargetWidth, mTargetHeight)
                  .get();
        } catch (Exception e) {
          e.printStackTrace();
          return null;
        }
        return drawable;
      }

      @Override
      protected void onPostExecute(Drawable drawable) {

        if (drawable == null) {
          callback.callbackBitmap(null);
          return;
        }

        if (drawable instanceof TransitionDrawable) {
          drawable = ((TransitionDrawable) drawable).getDrawable(1);
        }

        if (drawable instanceof BitmapDrawable) {
          bitmap = ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof GlideBitmapDrawable) {
          bitmap = ((GlideBitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof SquaringDrawable) {
          SquaringDrawable sq = (SquaringDrawable) drawable;
          bitmap = ((GlideBitmapDrawable) sq.getCurrent()).getBitmap();
        }

        callback.callbackBitmap(bitmap);
        Log.i(FALL, "비동기 콜백완료");
        if (isFragmentShowingOnScreen) {
          progressDialog.dismiss();
        }
      }

    }.execute();
    Glide.get(mContext).clearMemory();
  }

  public void isFragmentShowingOnScreen(boolean bool) {
    isFragmentShowingOnScreen = bool;
  }
}
