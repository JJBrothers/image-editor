package com.sz.image.editor.util.transform;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;

/**
 * Created by jhpark on 2016. 4. 15..
 */
public class TransformUtil {
  private static final String TAG = "##TransformUtil";
  public static final int PAINT_FLAGS = Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG;

  public static Resource<Bitmap> rotateImage(Resource<Bitmap> resource, BitmapPool bitmapPool, float degree) {
    Bitmap source = resource.get();
    Matrix matrix = new Matrix();
    matrix.postRotate(degree);
    Bitmap bm = Bitmap.createBitmap(source
            , 0
            , 0
            , source.getWidth(),
            source.getHeight()
            , matrix
            , true);

    return BitmapResource.obtain(bm, bitmapPool);
  }

  public static Bitmap rotateImage(Bitmap bitmap, float degree) {
    Matrix matrix = new Matrix();
    matrix.postRotate(degree);
    return Bitmap.createBitmap(bitmap
            , 0
            , 0
            , bitmap.getWidth(),
            bitmap.getHeight()
            , matrix
            , true);
  }

  public static Resource<Bitmap> cropImage(Resource<Bitmap> resource, BitmapPool bitmapPool, int cropX, int cropY, int cropWidth, int cropHeight) {
    Bitmap source = resource.get();
    Bitmap bm = Bitmap.createBitmap(source,
            cropX,
            cropY,
            cropWidth,
            cropHeight);

    return BitmapResource.obtain(bm, bitmapPool);
  }

  public static Bitmap cropImage(Bitmap bitmap, int cropX, int cropY, int cropWidth, int cropHeight) {
    return Bitmap.createBitmap(bitmap,
            cropX,
            cropY,
            cropWidth,
            cropHeight);
  }

  public static void getTransformedlBitmap(
          final Context context
          , final Bitmap bitmap
          , final int beforeCropAngle
          , final int rotateAngle
          , final int afterCropAngle
          , final int cropX
          , final int cropY
          , final int cropWidth
          , final int cropHeight
          , final int targetWidth
          , final int targetHeight
          , final BitmapCallback callback) {

    //.get(targetWidth,targetHeight, Bitmap.Config.RGB_565);

    new AsyncTask<Bitmap, Void, Bitmap>() {
      @Override
      protected Bitmap doInBackground(Bitmap... bitmaps) {

        BitmapPool bitmapPool = Glide.get(context).getBitmapPool();
        Resource<Bitmap> bitmapResource = BitmapResource.obtain(bitmaps[0], bitmapPool);

        if (beforeCropAngle != 0) {
          bitmaps[0] = rotateImage(bitmaps[0], beforeCropAngle);
          if (cropWidth != 0
                  && cropHeight != 0) {
            bitmaps[0] = fitCenter(bitmaps[0], Glide.get(context).getBitmapPool(), targetWidth, targetHeight);
            bitmaps[0] = cropImage(bitmaps[0], cropX, cropY, cropWidth, cropHeight);
            if (afterCropAngle != 0) {
              bitmaps[0] = rotateImage(bitmaps[0], afterCropAngle);
            }
          }
        } else {
          if (cropWidth != 0
                  && cropHeight != 0) {
            bitmaps[0] = fitCenter(bitmaps[0], Glide.get(context).getBitmapPool(), targetWidth, targetHeight);
            bitmaps[0] = cropImage(bitmaps[0], cropX, cropY, cropWidth, cropHeight);
            if (afterCropAngle != 0) {
              bitmaps[0] = rotateImage(bitmaps[0], afterCropAngle);
            }
          } else {
            if (rotateAngle != 0) {
              bitmaps[0] = rotateImage(bitmaps[0], rotateAngle);
            }
          }
        }
        return bitmaps[0];
      }

      @Override
      protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        callback.onProcessDone(bitmap);
      }
    }.execute(bitmap);
  }

//  public static void getTransformedlBitmap(
//          final Context context
//          , final Bitmap bitmap
//          , final int beforeCropAngle
//          , final int rotateAngle
//          , final int afterCropAngle
//          , final int cropX
//          , final int cropY
//          , final int cropWidth
//          , final int cropHeight
//          , final int targetWidth
//          , final int targetHeight
//          , final BitmapCallback callback) {
//
//    //.get(targetWidth,targetHeight, Bitmap.Config.RGB_565);
//
//    new AsyncTask<Bitmap, Void, Resource<Bitmap>>() {
//      @Override
//      protected Resource<Bitmap> doInBackground(Bitmap... bitmaps) {
//
//        BitmapPool bitmapPool = Glide.get(context).getBitmapPool();
//        Resource<Bitmap> bitmapResource = BitmapResource.obtain(bitmaps[0], bitmapPool);
//
//        if (beforeCropAngle != 0) {
//          bitmapResource = rotateImage(bitmapResource, bitmapPool, beforeCropAngle);
//          if (cropWidth != 0
//                  && cropHeight != 0) {
//            bitmapResource = fitCenter(bitmapResource, bitmapPool, targetWidth, targetHeight);
//            bitmapResource = cropImage(bitmapResource, bitmapPool, cropX, cropY, cropWidth, cropHeight);
//            if (afterCropAngle != 0) {
//              bitmapResource = rotateImage(bitmapResource, bitmapPool, afterCropAngle);
//            }
//          }
//        } else {
//          if (cropWidth != 0
//                  && cropHeight != 0) {
//            bitmapResource = fitCenter(bitmapResource, bitmapPool, targetWidth, targetHeight);
//            bitmapResource = cropImage(bitmapResource, bitmapPool, cropX, cropY, cropWidth, cropHeight);
//            if (afterCropAngle != 0) {
//              bitmapResource = rotateImage(bitmapResource, bitmapPool, afterCropAngle);
//            }
//          } else {
//            if (rotateAngle != 0) {
//              bitmapResource = rotateImage(bitmapResource, bitmapPool, rotateAngle);
//            }
//          }
//        }
//        return bitmapResource;
//      }
//
//      @Override
//      protected void onPostExecute(Resource<Bitmap> resource) {
//        super.onPostExecute(resource);
//        Bitmap bitmap = resource.get();
//        callback.onProcessDone(bitmap);
//      }
//    }.execute(bitmap);
//  }

  public static Resource<Bitmap> fitCenter(Resource<Bitmap> resouce, BitmapPool bitmapPool, int width, int height) {
    Bitmap toFit = resouce.get();
    if (toFit.getWidth() == width && toFit.getHeight() == height) {
      if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "requested target size matches input, returning input");
      }
      return BitmapResource.obtain(toFit, bitmapPool);
    }
    final float widthPercentage = width / (float) toFit.getWidth();
    final float heightPercentage = height / (float) toFit.getHeight();
    final float minPercentage = Math.min(widthPercentage, heightPercentage);

    // take the floor of the target width/height, not round. If the matrix
    // passed into drawBitmap rounds differently, we want to slightly
    // overdraw, not underdraw, to avoid artifacts from bitmap reuse.
    final int targetWidth = (int) (minPercentage * toFit.getWidth());
    final int targetHeight = (int) (minPercentage * toFit.getHeight());

    if (toFit.getWidth() == targetWidth && toFit.getHeight() == targetHeight) {
      if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "adjusted target size matches input, returning input");
      }
      return BitmapResource.obtain(toFit, bitmapPool);
    }

    Bitmap.Config config = getSafeConfig(toFit);
    Bitmap toReuse = bitmapPool.get(targetWidth, targetHeight, config);
    if (toReuse == null) {
      toReuse = Bitmap.createBitmap(targetWidth, targetHeight, config);
    }
    // We don't add or remove alpha, so keep the alpha setting of the Bitmap we were given.
    setAlpha(toFit, toReuse);

    if (Log.isLoggable(TAG, Log.VERBOSE)) {
      Log.v(TAG, "request: " + width + "x" + height);
      Log.v(TAG, "toFit:   " + toFit.getWidth() + "x" + toFit.getHeight());
      Log.v(TAG, "toReuse: " + toReuse.getWidth() + "x" + toReuse.getHeight());
      Log.v(TAG, "minPct:   " + minPercentage);
    }

    Canvas canvas = new Canvas(toReuse);
    Matrix matrix = new Matrix();
    matrix.setScale(minPercentage, minPercentage);
    Paint paint = new Paint(PAINT_FLAGS);
    canvas.drawBitmap(toFit, matrix, paint);

    return BitmapResource.obtain(toFit, bitmapPool);
  }

  /* 인자값으로 width, height 는 목표 target size*/
  public static Bitmap fitCenter(Bitmap toFit, BitmapPool bitmapPool, int width, int height) {
    if (toFit.getWidth() == width && toFit.getHeight() == height) {
      if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "requested target size matches input, returning input");
      }
      return toFit;
    }
    final float widthPercentage = width / (float) toFit.getWidth();
    final float heightPercentage = height / (float) toFit.getHeight();
    final float minPercentage = Math.min(widthPercentage, heightPercentage);

    // take the floor of the target width/height, not round. If the matrix
    // passed into drawBitmap rounds differently, we want to slightly
    // overdraw, not underdraw, to avoid artifacts from bitmap reuse.
    final int targetWidth = (int) (minPercentage * toFit.getWidth());
    final int targetHeight = (int) (minPercentage * toFit.getHeight());

    if (toFit.getWidth() == targetWidth && toFit.getHeight() == targetHeight) {
      if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "adjusted target size matches input, returning input");
      }
      return toFit;
    }

    Bitmap.Config config = getSafeConfig(toFit);
    Bitmap toReuse = bitmapPool.get(targetWidth, targetHeight, config);
    if (toReuse == null) {
      toReuse = Bitmap.createBitmap(targetWidth, targetHeight, config);
    }
    // We don't add or remove alpha, so keep the alpha setting of the Bitmap we were given.
    setAlpha(toFit, toReuse);

    if (Log.isLoggable(TAG, Log.VERBOSE)) {
      Log.v(TAG, "request: " + width + "x" + height);
      Log.v(TAG, "toFit:   " + toFit.getWidth() + "x" + toFit.getHeight());
      Log.v(TAG, "toReuse: " + toReuse.getWidth() + "x" + toReuse.getHeight());
      Log.v(TAG, "minPct:   " + minPercentage);
    }

    Canvas canvas = new Canvas(toReuse);
    Matrix matrix = new Matrix();
    matrix.setScale(minPercentage, minPercentage);
    Paint paint = new Paint(PAINT_FLAGS);
    canvas.drawBitmap(toFit, matrix, paint);

    return toReuse;
  }

  private static Bitmap.Config getSafeConfig(Bitmap bitmap) {
    return bitmap.getConfig() != null ? bitmap.getConfig() : Bitmap.Config.ARGB_8888;
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
  public static void setAlpha(Bitmap toTransform, Bitmap outBitmap) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1 && outBitmap != null) {
      outBitmap.setHasAlpha(toTransform.hasAlpha());
    }
  }

  public interface BitmapCallback {
    public void onProcessDone(Bitmap bitmap);
  }
}
