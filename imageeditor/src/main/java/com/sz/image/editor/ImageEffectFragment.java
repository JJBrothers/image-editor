package com.sz.image.editor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import com.sz.image.editor.filter.Filter;
import com.sz.image.editor.util.transform.TransformUtil;

/**
 * Created by jhpark on 2016. 4. 9..
 */
public class ImageEffectFragment extends Fragment {

  private final String TAG = "ImageEffectFragment";

  /**
   * Fragment 종료시점과 복원할때 쓰일 상수 .
   */
  private final String EXTRA_SAVE_DATA = "save_data";

  /**
   * 현재 Fragment에서 활용할 수정되었거나, 또는 기본이미지 경로
   */
  public static final String EXTRA_TARGET_PATH = "target_path";

  /**
   * 이미지 크기
   */
  public static final String EXTRA_TARGET_WIDTH = "target_width";
  public static final String EXTRA_TARGET_HEIGHT = "target_height";
  public static final String EXTRA_FRAGMENT_INDEX = "fragment_index";
  private final String EXTRA_CURRENT_FILTER_TYPE = "current_apply_filter_type";
  //private final String EXTRA_FINAL_PATH = "final_path";
  private final String EXTRA_ROTATE_ANGLE = "rotate_angle";
  public static final String EXTRA_BEFORE_CROP_ANGLE = "before_crop_angle";
  private final String EXTRA_AFTER_CROP_ANGLE = "after_crop_angle";
  public static final String EXTRA_CROP_X = "crop_x";
  public static final String EXTRA_CROP_Y = "crop_y";
  public static final String EXTRA_CROP_WIDTH = "crop_width";
  public static final String EXTRA_CROP_HEIGHT = "crop_height";
  private Context mContext;
  private Callback mCallback;
  private ImageView mFinalImage;
  private Bitmap mFinalBitmap;
  private String mTargetPath;
  public int mTargetWidth;
  private int mTargetHeight;
  private int mFragmentIndex;
  private Filter.Type mCurrentFilterType;
  private int mRotateAngle;
  private int mBeforeCropAngle;
  private int mAfterCropAngle;
  private int mCropX;
  private int mCropY;
  private int mCropWidth;
  private int mCropHeight;

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    mContext = context;
    mCallback = (Callback) context;
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_image_effect, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mFinalImage = (ImageView) view.findViewById(R.id.finalImage);
    mTargetPath = getArguments().getString(EXTRA_TARGET_PATH); //원본이미지에 필터를뺀 모든효과 적용한 path
    mTargetWidth = getArguments().getInt(EXTRA_TARGET_WIDTH);
    mTargetHeight = getArguments().getInt(EXTRA_TARGET_HEIGHT);
    mFragmentIndex = getArguments().getInt(EXTRA_FRAGMENT_INDEX);
    mCurrentFilterType = Filter.Type.ORIGINAL;
  }

  @Override
  public void onViewStateRestored(Bundle savedInstanceState) {
    super.onViewStateRestored(savedInstanceState);
    Log.d(TAG, "###onViewStateRestored");
    if (savedInstanceState != null) {
      System.out.println("###저장된 값없음 복구할값 없음. 탈출 ! ");
      Bundle bundle = savedInstanceState.getBundle(EXTRA_SAVE_DATA);
      mCurrentFilterType = (Filter.Type) bundle.getSerializable(EXTRA_CURRENT_FILTER_TYPE);
      mRotateAngle = bundle.getInt(EXTRA_ROTATE_ANGLE);
      mBeforeCropAngle = bundle.getInt(EXTRA_BEFORE_CROP_ANGLE);
      mAfterCropAngle = bundle.getInt(EXTRA_AFTER_CROP_ANGLE);
      mCropX = bundle.getInt(EXTRA_CROP_X);
      mCropY = bundle.getInt(EXTRA_CROP_Y);
      mCropWidth = bundle.getInt(EXTRA_CROP_WIDTH);
      mCropHeight = bundle.getInt(EXTRA_CROP_HEIGHT);
    }

    applyFilter(mCurrentFilterType, false);
  }

  //프래그먼트가 종료되기전 상태저장한다.
  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    Log.i(TAG, "##onSaveInstanceState");
    Bundle bundle = new Bundle();
    bundle.putSerializable(EXTRA_CURRENT_FILTER_TYPE, mCurrentFilterType);//Fragement가 종료되기전 가장 마지막에 적용한 효과정보를 저장한다.
    bundle.putInt(EXTRA_ROTATE_ANGLE, mRotateAngle);
    bundle.putInt(EXTRA_BEFORE_CROP_ANGLE, mBeforeCropAngle);
    bundle.putInt(EXTRA_AFTER_CROP_ANGLE, mAfterCropAngle);
    bundle.putInt(EXTRA_CROP_X, mCropX);
    bundle.putInt(EXTRA_CROP_Y, mCropY);
    bundle.putInt(EXTRA_CROP_WIDTH, mCropWidth);
    bundle.putInt(EXTRA_CROP_HEIGHT, mCropHeight);
    outState.putBundle(EXTRA_SAVE_DATA, bundle);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    System.out.println("프래그먼트 종료");
  }

  public String getTargetPath() {
    return mTargetPath;
  }

  public Filter.Type getCurrentFilterType() {
    return mCurrentFilterType;
  }

  public int getRotateAngle() {
    return mRotateAngle;
  }

  public int getTargetWidth() {
    return mTargetWidth;
  }

  public int getTargetHeight() {
    return mTargetHeight;
  }

  private Bitmap mFilteredOnlyBitmap;

  public void applyFilter(Filter.Type filterType, boolean setProgress) {
    System.out.println("applyFilter 진입 !" + filterType.getName());
    mCurrentFilterType = filterType;
    Filter.getFilteredBitmap(mContext, mTargetPath, mCurrentFilterType, mTargetWidth, mTargetHeight, new Filter.Callback() {
      @Override
      public void callbackBitmap(Bitmap bitmap) {
        mFilteredOnlyBitmap = bitmap; //crop시 속도향상을 위한 비트맵
        TransformUtil.getTransformedlBitmap(
                mContext
                , bitmap
                , mBeforeCropAngle
                , mRotateAngle
                , mAfterCropAngle
                , mCropX
                , mCropY
                , mCropWidth
                , mCropHeight
                , mTargetWidth
                , mTargetHeight
                , new TransformUtil.BitmapCallback() {
                  @Override
                  public void onProcessDone(Bitmap bitmap) {
                    mFinalBitmap = bitmap;
                    mFinalImage.setImageBitmap(mFinalBitmap);
                  }
                });
      }
    }, setProgress);
  }

  public void applyCrop(int beforeCropAngle, int cropX, int cropY, int cropWidth, int cropHeight) {
    mFinalImage.setImageBitmap(null);
    mBeforeCropAngle = beforeCropAngle;
    mAfterCropAngle = 0; //초기화
    mCropX = cropX;
    mCropY = cropY;
    mCropWidth = cropWidth;
    mCropHeight = cropHeight;
    TransformUtil.getTransformedlBitmap(
            mContext
            , mFilteredOnlyBitmap
            , mBeforeCropAngle
            , mRotateAngle
            , mAfterCropAngle
            , mCropX
            , mCropY
            , mCropWidth
            , mCropHeight
            , mTargetWidth
            , mTargetHeight
            , new TransformUtil.BitmapCallback() {
              @Override
              public void onProcessDone(Bitmap bitmap) {
                mFinalBitmap = bitmap;
                mFinalImage.setImageBitmap(mFinalBitmap);
                //이미지 효과 상태저장후 자르기 변경 콜백을 보내 필터리스트를 업데이트
                //callbackImageEffectState();
                mCallback.onSaveCropState(mFragmentIndex, mBeforeCropAngle, mAfterCropAngle, mCropX, mCropY, mCropWidth, mCropHeight);
              }
            });
  }

  public void applyRotate() {
    if (isRotating) return;
    //  int shapeVal = mFinalBitmap.getHeight() > mFinalBitmap.getWidth() ? 0 : 1;
    //System.out.println(shapeVal + "shapeVal");
    float[] scaleArr = getFromAndToValue();
    ScaleAnimation scaleAnim = new ScaleAnimation(
            scaleArr[0]
            , scaleArr[1]
            , scaleArr[2]
            , scaleArr[3]
            , ScaleAnimation.RELATIVE_TO_SELF
            , 0.5f
            , ScaleAnimation.RELATIVE_TO_SELF
            , 0.5f);

    RotateAnimation rotateAnim = new RotateAnimation(
            0
            , 90
            , RotateAnimation.RELATIVE_TO_SELF
            , 0.5f
            , RotateAnimation.RELATIVE_TO_SELF
            , 0.5f);
    AnimationSet animSet = new AnimationSet(false);
    animSet.addAnimation(rotateAnim);
    animSet.addAnimation(scaleAnim);
    animSet.setDuration(300);
    animSet.setFillAfter(true);

    animSet.setAnimationListener(new Animation.AnimationListener() {
      @Override
      public void onAnimationStart(Animation animation) {
        isRotating = true;
        System.out.println("애니메이션 시작");
        mRotateAngle += 90;
        if (mRotateAngle == 360) mRotateAngle = 0;
        mAfterCropAngle += 90;
        if (mAfterCropAngle == 360) mAfterCropAngle = 0;
      }

      @Override
      public void onAnimationEnd(Animation animation) {
        new Handler().postDelayed(new Runnable() {
          @Override
          public void run() {
            System.out.println("깜빡임 방지 ");
            mFinalBitmap = TransformUtil.rotateImage(mFinalBitmap, 90);
            mFinalImage.setAnimation(null);
            mFinalImage.setImageBitmap(null);
            mFinalImage.setImageBitmap(mFinalBitmap);
            isRotating = false;
            //이미지 효과 상태저장후 회전 변경 콜백을 보내 필터리스트를 업데이트
            //callbackImageEffectState();
            mCallback.onSaveRotateState(mFragmentIndex, mRotateAngle, mAfterCropAngle);
          }
        }, 10);
        System.out.println("애니메이션 끝");
      }

      @Override
      public void onAnimationRepeat(Animation animation) {
        System.out.println("onAnimationRepeat");
      }
    });
    mFinalImage.startAnimation(animSet);
  }

  private float[] getFromAndToValue() {
    float scaleArr[] = new float[4];
    System.out.println("비트맵 가로 = " + mFinalBitmap.getWidth());
    System.out.println("비트맵 세로 = " + mFinalBitmap.getHeight());

    if (getActualDisplayWidth() == getActualDisplayHeight()) {
      System.out.println("비트맵 세로와 가로 길이 같다 ! ");
      scaleArr[0] = 1.0f;
      scaleArr[1] = 1.0f;
      scaleArr[2] = 1.0f;
      scaleArr[3] = 1.0f;
    } else {
      if (getActualDisplayWidth() > getActualDisplayHeight()) {
        System.out.println("회전하기전 비트맵 가로가 더크다");
        float xToY = (float) mTargetHeight / getActualDisplayWidth();//현재 보여지는 viewpager 세로길이 / 이미지 가로길이
        float yTox = (float) mTargetWidth / getActualDisplayHeight();//현재 보여지는 vuewpager 가로길이 / 이미지 세로길이

        //현재 보여지고있는 이미지를 viewpager에 맞는 비율을 구한다.
        float toScale = Math.floor(xToY * getActualDisplayWidth()) <= (float) mTargetHeight
                && Math.floor(xToY * getActualDisplayHeight()) <= (float) mTargetWidth
                ? xToY : yTox;
//        System.out.println("xToY > "+xToY);
//        System.out.println("yTox > "+yTox);
//        System.out.println(Math.floor(xToY*  getActualDisplayWidth())+" <- xToY*  getActualDisplayWidth()");
//        System.out.println(Math.floor(xToY * getActualDisplayHeight())+" <- xToY * getActualDisplayHeight()");
        scaleArr[0] = 1.0f;
        scaleArr[1] = toScale;
        scaleArr[2] = 1.0f;
        scaleArr[3] = toScale;
      } else {
        System.out.println("회전하기전 비트맵 세로가 더크다");
        scaleArr[0] = 1.0f;
        scaleArr[1] = (float) mTargetWidth / getActualDisplayHeight();
        scaleArr[2] = 1.0f;
        scaleArr[3] = (float) mTargetWidth / getActualDisplayHeight();
      }
//
//      System.out.println("scale 0 = " + scaleArr[0]);
//      System.out.println("scale 1 = " + scaleArr[1]);
//      System.out.println("scale 2 = " + scaleArr[2]);
//      System.out.println("scale 3 = " + scaleArr[3]);

    }
    ;
    return scaleArr;
  }


  private float getActualDisplayWidth() {
    float[] f = new float[9];
    mFinalImage.getImageMatrix().getValues(f);
    final float scaleX = f[Matrix.MSCALE_X];
    final Drawable d = mFinalImage.getDrawable();
    final float origW = d.getIntrinsicWidth();
    final float actW = origW * scaleX;
    return actW;
  }

  private float getActualDisplayHeight() {
    float[] f = new float[9];
    mFinalImage.getImageMatrix().getValues(f);
    final float scaleY = f[Matrix.MSCALE_Y];
    final Drawable d = mFinalImage.getDrawable();
    final float origH = d.getIntrinsicHeight();
    final float actH = origH * scaleY;
    return actH;
  }

  private boolean isRotating = false;
//  private Animation.AnimationListener animationListener = new Animation.AnimationListener() {
//
//    @Override
//    public void onAnimationStart(Animation animation) {
//      isRotating = true;
//      System.out.println("애니메이션 시작");
//      mRotateAngle += 90;
//      if (mRotateAngle == 360) mRotateAngle = 0;
//      mAfterCropAngle += 90;
//      if (mAfterCropAngle == 360) mAfterCropAngle = 0;
//    }
//
//    @Override
//    public void onAnimationEnd(Animation animation) {
//      new Handler().postDelayed(new Runnable() {
//        @Override
//        public void run() {
//          System.out.println("깜빡임 방지 ");
//          mFinalBitmap = TransformUtil.rotateImage(mFinalBitmap, 90);
//          mFinalImage.setAnimation(null);
//          mFinalImage.setImageBitmap(mFinalBitmap);
//          isRotating = false;
//
//          //이미지 효과 상태저장후 회전 변경 콜백을 보내 필터리스트를 업데이트
//          //callbackImageEffectState();
//          mCallback.onSaveRotateState(mFragmentIndex, mRotateAngle, mAfterCropAngle);
//        }
//      }, 100);
//      System.out.println("애니메이션 끝");
//    }
//
//    @Override
//    public void onAnimationRepeat(Animation animation) {
//      System.out.println("onAnimationRepeat");
//    }
//  };

  public static class ImageEffectState {
    public String targetPath;
    public int targetWidth;
    public int targetHeight;
    public Filter.Type filterType;
    public int beforeCropAngle;
    public int rotateAngle;
    public int afterCropAngle;
    public int cropX;
    public int cropY;
    public int cropWidth;
    public int cropHeight;

    public ImageEffectState(
            String targetPath
            , int targetWidth
            , int targetHeight
            , Filter.Type filterType
            , int beforeCropAngle
            , int rotateAngle
            , int afterCropAngle
            , int cropX
            , int cropY
            , int cropWidth
            , int cropHeight
    ) {
      this.targetPath = targetPath;
      this.targetWidth = targetWidth;
      this.targetHeight = targetHeight;
      this.filterType = filterType;
      this.beforeCropAngle = beforeCropAngle;
      this.rotateAngle = rotateAngle;
      this.afterCropAngle = afterCropAngle;
      this.cropX = cropX;
      this.cropY = cropY;
      this.cropWidth = cropWidth;
      this.cropHeight = cropHeight;
    }

  }

  interface Callback {
    //public void saveFile(int fragmentIdx, String savePath);
    //public void onSaveImageEffectState(int fragmentIdx, ImageEffectState effectState);
    //public void onFiterChange(int fragmentIdx, Filter.Type filterType);
    public void onSaveCropState(int fragmentIdx, int beforeCropAngle, int afterCropAngle, int cropX, int cropY, int cropWidth, int cropHeight);

    public void onSaveRotateState(int fragmentIdx, int rotateAngle, int afterCropAngle);
  }
}
