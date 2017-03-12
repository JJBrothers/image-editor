package com.sz.image.editor;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.Glide;
import com.sz.image.editor.filter.Filter;
import com.sz.image.editor.util.transform.TransformUtil;
import com.sz.image.editor.R;
/**
 * Created by jhpark on 2016. 4. 10..
 */
public class CropImageActivity extends Activity {

  public static final String EXTRA_TARGETL_PATH = "target_path";
  public static final String EXTRA_FILTER_TYPE = "filter_type";
  public static final String EXTRA_TARGET_WIDTH = "target_width";
  public static final String EXTRA_TARGET_HEIGHT = "target_height";
  public static final String EXTRA_ROTATE_ANGLE = "rotate_angle";
  private String mTargetPath;
  private Filter.Type mFilterType;
  private int mTargetWidth;
  private int mTargetHeight;
  private int mRotateAngle;
  private CropImageView mCropImageView;
  private Bitmap mWillCropBitmap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_crop_image);
    Intent intent = getIntent();
    mTargetPath = intent.getStringExtra(EXTRA_TARGETL_PATH);
    mFilterType = (Filter.Type) intent.getSerializableExtra(EXTRA_FILTER_TYPE);
    mTargetWidth = intent.getIntExtra(EXTRA_TARGET_WIDTH, 0);
    mTargetHeight = intent.getIntExtra(EXTRA_TARGET_HEIGHT, 0);
    mRotateAngle = intent.getIntExtra(EXTRA_ROTATE_ANGLE, 0);
    mCropImageView = (CropImageView) findViewById(R.id.cropImage);

    mCropImageView.setFixedAspectRatio(false);
    Filter.getFilteredBitmap(this, mTargetPath, mFilterType, mTargetWidth, mTargetHeight, new Filter.Callback() {
      @Override
      public void callbackBitmap(Bitmap bitmap) {
        bitmap = TransformUtil.rotateImage(bitmap, mRotateAngle);
        mWillCropBitmap= TransformUtil.fitCenter(
                bitmap
                , Glide.get(CropImageActivity.this).getBitmapPool()
                , mTargetWidth
                , mTargetHeight);
        mCropImageView.setImageBitmap(mWillCropBitmap);
      }
    }, true);
  }

  public void clickedButtonEvent(View v) {
    int id = v.getId();
    if (id == R.id.free) {
      mCropImageView.setFixedAspectRatio(false);
    } else if (id == R.id.ratioOnoOne) {
      mCropImageView.setFixedAspectRatio(true);
      mCropImageView.setAspectRatio(1, 1);
    } else if (id == R.id.ratioFourThree) {
      mCropImageView.setFixedAspectRatio(true);
      mCropImageView.setAspectRatio(4, 3);
    } else if (id == R.id.ratioThreeFour) {
      mCropImageView.setFixedAspectRatio(true);
      mCropImageView.setAspectRatio(3, 4);
    } else if (id == R.id.submit) {

      int cropX = (int) mCropImageView.getCropX();
      int cropY = (int) mCropImageView.getCropY();
      int cropWidth = (int) mCropImageView.getCropWidth(mWillCropBitmap);
      int cropHeight = (int) mCropImageView.getCropHeight(mWillCropBitmap);

      Intent intent = new Intent();
      intent.putExtra(ImageEffectFragment.EXTRA_BEFORE_CROP_ANGLE, mRotateAngle);
      intent.putExtra(ImageEffectFragment.EXTRA_CROP_X, cropX);
      intent.putExtra(ImageEffectFragment.EXTRA_CROP_Y, cropY);
      intent.putExtra(ImageEffectFragment.EXTRA_CROP_WIDTH, cropWidth);
      intent.putExtra(ImageEffectFragment.EXTRA_CROP_HEIGHT, cropHeight);
      setResult(RESULT_OK, intent);
      finish();
    }
  }
}
