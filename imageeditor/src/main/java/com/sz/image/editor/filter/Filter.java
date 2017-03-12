package com.sz.image.editor.filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;

/**
 * Created by jhpark on 2016. 4. 9..
 */
public abstract class Filter {

  public static final boolean FILTER_IMAGE_CACHE = false;
  public static final DiskCacheStrategy FILTER_IMAGE_DISK_CACHE = DiskCacheStrategy.NONE;
  public static final float FILTER_IMAGE_LOADING_ALPHA = 0.1f;
  public static final boolean MAIN_IMAGE_CACHE = false;
  public static final DiskCacheStrategy MAIN_IMAGE_DISK_CACHE = DiskCacheStrategy.NONE;

  /**
   * 필터 효과 종류 상수 설정 .
   */
  public enum Type {
    ORIGINAL("없음"),
    GRAYSCALE("흑백"),
    SEPIA("세피아"),
    VINETTE("비네트"),
    BRIGHTNESS("밝음"),
    WINTER("그겨울"),
    WARM("따스한"),
    FALL("가을날"),
    REMEMBERANCE("회상"),
    ELEGANCE("우아한"),
    GRACE("단아한");

    private String name;

    private Type(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

  private static Filter getFilterObject(Context context, String targetPath, Type filterType, int width, int heigth) {
    Filter filter = null;

    switch (filterType) {
      case ORIGINAL:
        filter = new Original(context, targetPath, width, heigth);
        break;

      case GRAYSCALE:
        filter = new Grayscale(context, targetPath, width, heigth);
        break;

      case SEPIA:
        filter = new Sepia(context, targetPath, width, heigth);
        break;

      case VINETTE:
        filter = new Vinette(context, targetPath, width, heigth);
        break;

      case BRIGHTNESS:
        filter = new Brightness(context, targetPath, width, heigth);
        break;

      case WINTER:
        filter = new Winter(context, targetPath, width, heigth);
        break;

      case WARM:
        filter = new Warm(context, targetPath, width, heigth);
        break;

      case FALL:
        filter = new Fall(context, targetPath, width, heigth);
        break;

      case REMEMBERANCE:
        filter = new Rememberance(context, targetPath, width, heigth);
        break;

      case ELEGANCE:
        filter = new Elegance(context, targetPath, width, heigth);
        break;

      case GRACE:
        filter = new Grace(context, targetPath, width, heigth);
        break;

    }
    return filter;
  }

  public static void assignFilterImage(Context context
          , String targetPath
          , Filter.Type filterType
          , int width
          , int height
          , TextView textView
          , ImageView imageView) {
    Filter filter = getFilterObject(context, targetPath, filterType, width, height);
    filter.assignFilterImage(textView, imageView);
  }

  public static void getFilteredBitmap(Context context
          , String targetPath
          , Filter.Type filterType
          , int width
          , int height
          , Callback callback
          , boolean bool) {
    Filter filter = getFilterObject(context, targetPath, filterType, width, height);
    filter.isFragmentShowingOnScreen(bool);
    filter.getBitmap(callback);
  }

  public abstract void assignFilterImage(TextView textView, ImageView imageView);

  public abstract void getBitmap(Callback callback);

  public abstract void isFragmentShowingOnScreen(boolean bool);

//  public class RotateTransformation extends BitmapTransformation {
//
//    private float rotateRotationAngle = 0f;
//
//    public RotateTransformation(Context context, float rotateRotationAngle) {
//      super( context );
//
//      this.rotateRotationAngle = rotateRotationAngle;
//    }
//
//    @Override
//    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
//      Matrix matrix = new Matrix();
//      matrix.postRotate(rotateRotationAngle);
//      return Bitmap.createBitmap(toTransform, 0, 0, toTransform.getWidth(), toTransform.getHeight(), matrix, true);
//    }
//
//    @Override
//    public String getId() {
//      return "rotate" + rotateRotationAngle;
//    }
//  }

//
//  public class CropTransformation implements Transformation<Bitmap> {
//
//    private BitmapPool mBitmapPool;
//    private int mWidth;
//    private int mHeight;
//
//    private int mCropX;
//    private int mCropY;
//    private int mCropWidth;
//    private int mCropHeight;
//
//    public CropTransformation(Context context, int cropX, int cropY, int cropWidth, int cropHeight) {
//      this(Glide.get(context).getBitmapPool());
//      this.mCropX = cropX;
//      this.mCropY = cropY;
//      this.mCropWidth = cropWidth;
//      this.mCropHeight = cropHeight;
//    }
//
//    public CropTransformation(BitmapPool pool) {
//      this.mBitmapPool = pool;
//    }
//
//    @Override
//    public Resource<Bitmap> transform(Resource<Bitmap> resource, int outWidth, int outHeight) {
//      //Bitmap source = resource.get();
//      Bitmap source = Bitmap.createBitmap(resource.get(), mWidth, mHeight, size, size);
//      System.out.println(source.getWidth()+" / "+source.getHeight());
//      int size = Math.min(source.getWidth(), source.getHeight());
//
//      mWidth = (source.getWidth() - size) / 2;
//      mHeight = (source.getHeight() - size) / 2;
//
//      Bitmap.Config config =
//              source.getConfig() != null ? source.getConfig() : Bitmap.Config.ARGB_8888;
//      Bitmap bitmap = mBitmapPool.get(mWidth, mHeight, config);
//      if (bitmap == null) {
//        bitmap = Bitmap.createBitmap(source, mWidth, mHeight, size, size);
//      }
//
//      return BitmapResource.obtain(bitmap, mBitmapPool);
//    }
//
//    @Override public String getId() {
//      return "CropSquareTransformation(width=" + mWidth + ", height=" + mHeight + ")";
//    }
//  }


  public interface Callback {
    void callbackBitmap(Bitmap bitmap);
  }
}
