package com.sz.image.editor;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.sz.image.editor.adapter.FilterAdapter;
import com.sz.image.editor.adapter.ImagePagerAdapter;
import com.sz.image.editor.dialog.CustomProgressDialog;
import com.sz.image.editor.filter.Filter;
import com.sz.image.editor.util.file.FileUtils;
import com.sz.image.editor.util.transform.TransformUtil;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jhpark on 2016. 4. 9..
 */
public class ImageEffectActivity extends FragmentActivity implements ImageEffectFragment.Callback
        , FilterAdapter.Callback {

  /**
   * 이전 액티비티에서 결과 값을 받아 현재 액티비티에서 데이터 활용하고자하는 상수
   */
  public static final String EXTRA_RESULT = "select_result";
  public static final int REQUEST_CROP_ACTIVITY = 1;
  public static final String EXTRA_CHANGE_TARGET_PATH = "change_target_path";

  private final String TAG = "##ImageEffectActivity";
  ArrayList<String> mOriginalPathList;
  private ArrayList<ImageEffectFragment.ImageEffectState> mSaveList = new ArrayList<ImageEffectFragment.ImageEffectState>();
  private TextView mListState;

  private ViewPager mImagePager;
  private RecyclerView mFilterListView;
  private FilterAdapter mFilterAdapter;
  private ArrayList<Filter.Type> mFilterList;
  private ImagePagerAdapter mImagePagerAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_image_effect);

    Intent intent = getIntent();
    mOriginalPathList = intent.getStringArrayListExtra(EXTRA_RESULT);

    mListState = (TextView) findViewById(R.id.listState);
    mListState.setText(1 + " / " + mOriginalPathList.size());
    mFilterListView = (RecyclerView) findViewById(R.id.filterList);
    mFilterListView.setHasFixedSize(true);

    //recycler view의 보여질 아이템들을 linear layout horizontal로 세팅
    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
    mFilterListView.setLayoutManager(layoutManager);
    int filteredImageWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, getResources().getDimension(R.dimen.filter_image_width), this.getResources().getDisplayMetrics());
    int filteredImageHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, getResources().getDimension(R.dimen.filter_image_height), this.getResources().getDisplayMetrics());

    Log.d(TAG, "썸네일 가로 : " + filteredImageWidth);
    Log.d(TAG, "썸네일 세로 : " + filteredImageHeight);

    mFilterList = getAddedFilterList();
    mFilterAdapter = new FilterAdapter(this
            , filteredImageWidth
            , filteredImageHeight
            , mFilterList);
    mImagePager = (ViewPager) findViewById(R.id.fragmentPager);
    mImagePager.setOffscreenPageLimit(2); // +- loading count
    mImagePager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

      }

      @Override
      public void onPageSelected(int position) {
        mListState.setText(position + 1 + " / " + mOriginalPathList.size());
        if (mFilterListView.isShown()) {
          loadFilterList(position);
        }
      }

      @Override
      public void onPageScrollStateChanged(int state) {

      }
    });

    mImagePager.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override
      public void onGlobalLayout() {
        int targetImageWidth = mImagePager.getWidth();
        int targetImageHeight = mImagePager.getHeight();

        Log.i(TAG, "ViewPager 가로 : " + String.valueOf(targetImageWidth));
        Log.i(TAG, "ViewPager 세로 : " + String.valueOf(targetImageHeight));

        saveTempImageEffectState(targetImageWidth, targetImageHeight);

        mImagePagerAdapter = new ImagePagerAdapter(
                ImageEffectActivity.this
                , getSupportFragmentManager()
                , mOriginalPathList
                , targetImageWidth
                , targetImageHeight);
        mImagePager.setAdapter(mImagePagerAdapter);
        if (Build.VERSION.SDK_INT >= 16) {
          mImagePager.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
      }
    });
  }

  private void saveTempImageEffectState(int targetWidth, int targetHeight) {
    for (int i = 0; i < mOriginalPathList.size(); i++) {
      //System.out.println("원본 : "+mOriginalPathList.get(i));
      mSaveList.add(
              new ImageEffectFragment.ImageEffectState(
                      mOriginalPathList.get(i)
                      , targetWidth
                      , targetHeight
                      , Filter.Type.ORIGINAL
                      , 0
                      , 0
                      , 0
                      , 0
                      , 0
                      , 0
                      , 0
              ));
    }
  }

  private ArrayList<Filter.Type> getAddedFilterList() {
    ArrayList<Filter.Type> filterList = new ArrayList<Filter.Type>();
    filterList.add(Filter.Type.ORIGINAL);
    filterList.add(Filter.Type.GRAYSCALE);
    filterList.add(Filter.Type.WINTER);
    filterList.add(Filter.Type.WARM);
    filterList.add(Filter.Type.GRACE);
    filterList.add(Filter.Type.BRIGHTNESS);
    filterList.add(Filter.Type.FALL);
    filterList.add(Filter.Type.REMEMBERANCE);
    filterList.add(Filter.Type.ELEGANCE);
    filterList.add(Filter.Type.SEPIA);
    filterList.add(Filter.Type.VINETTE);
    return filterList;
  }

  public void clickedButtonEvent(View v) {
    int id = v.getId();
    if (id == R.id.filterBtn) {
      Log.i(TAG, "필터버튼 클릭");
      if (mFilterListView.isShown()) {
        mFilterListView.setVisibility(View.GONE);
      } else {
        mFilterListView.setVisibility(View.VISIBLE);
        int fragmentIndex = mImagePager.getCurrentItem();
        loadFilterList(fragmentIndex);
      }
    } else if (id == R.id.cropBtn) {
      Log.i(TAG, "자르기버튼 클릭");
      int fragmentIdx = mImagePager.getCurrentItem();
      ImageEffectFragment currentFragment = (ImageEffectFragment) mImagePagerAdapter.getFragment(fragmentIdx);
      Intent intent = new Intent(ImageEffectActivity.this, CropImageActivity.class);
      intent.putExtra(CropImageActivity.EXTRA_TARGETL_PATH, currentFragment.getTargetPath());
      intent.putExtra(CropImageActivity.EXTRA_FILTER_TYPE, currentFragment.getCurrentFilterType());
      intent.putExtra(CropImageActivity.EXTRA_TARGET_WIDTH, currentFragment.getTargetWidth());
      intent.putExtra(CropImageActivity.EXTRA_TARGET_HEIGHT, currentFragment.getTargetHeight());
      intent.putExtra(CropImageActivity.EXTRA_ROTATE_ANGLE, currentFragment.getRotateAngle());
      startActivityForResult(intent, REQUEST_CROP_ACTIVITY);
    } else if (id == R.id.rotateBtn) {
      Log.i(TAG, "회전버튼 클릭");
      int fragmentIdx = mImagePager.getCurrentItem();
      ImageEffectFragment currentFragment = (ImageEffectFragment) mImagePagerAdapter.getFragment(fragmentIdx);
      currentFragment.applyRotate();
      //필터리스트 여기에 로직 추가할것

    } else if (id == R.id.submitBtn) {
      Log.i(TAG, "전송버튼 클릭");
      final ProgressDialog progressDialog = CustomProgressDialog.createPlainProgressDialog(this);
      final ArrayList<String> filePathList = new ArrayList<String>();
      final ArrayList<Integer> willProcessIdxs = new ArrayList<Integer>();
      final HashMap<Integer,Boolean> willProcessState = new HashMap<Integer,Boolean>();
      for (int i = 0; i < mSaveList.size(); i++) {
        final int idx = i;
        System.out.println("파일 변환 시작 ! ");
        if (mSaveList.get(idx).filterType == Filter.Type.ORIGINAL
                && mSaveList.get(idx).beforeCropAngle == 0
                && mSaveList.get(idx).rotateAngle == 0
                && mSaveList.get(idx).afterCropAngle == 0
                && mSaveList.get(idx).cropX == 0
                && mSaveList.get(idx).cropY == 0
                && mSaveList.get(idx).cropWidth == 0
                && mSaveList.get(idx).cropHeight == 0) {
          System.out.println("탈출한다 ! ");
          filePathList.add(mSaveList.get(idx).targetPath);
        } else {
          filePathList.add(mSaveList.get(idx).targetPath);
          willProcessIdxs.add(idx);
          willProcessState.put(idx,false);
        }
      }

      if (willProcessIdxs.size() == 0) {
        progressDialog.dismiss();
        Intent intent = new Intent();
        intent.putStringArrayListExtra(MultiImageSelectorActivity.EXTRA_IMAGE_EFFECT_FILE_PATHS, filePathList);
        setResult(RESULT_OK, intent);
        finish();
        return;
      }

      for (int i = 0; i < willProcessIdxs.size(); i++) {
        final int idx = willProcessIdxs.get(i);
        String targetPath = mSaveList.get(idx).targetPath;
        Filter.Type filterType = mSaveList.get(idx).filterType;
        final int targetWidth = mSaveList.get(idx).targetWidth;
        final int targetHeight = mSaveList.get(idx).targetHeight;
        final int beforeCropAngle = mSaveList.get(idx).beforeCropAngle;
        final int rotateAngle = mSaveList.get(idx).rotateAngle;
        final int afterCropAngle = mSaveList.get(idx).afterCropAngle;
        final int cropX = mSaveList.get(idx).cropX;
        final int cropY = mSaveList.get(idx).cropY;
        final int cropWidth = mSaveList.get(idx).cropWidth;
        final int cropHeight = mSaveList.get(idx).cropHeight;


        //final long start = System.currentTimeMillis();
        Filter.getFilteredBitmap(
                this
                , targetPath
                , filterType
                , targetWidth
                , targetHeight
                , new Filter.Callback() {
                  @Override
                  public void callbackBitmap(Bitmap bitmap) {

                    System.out.println("#####이미지 필터 적용완료 인덱스번호는 : " + idx + " 번 ");

                    TransformUtil.getTransformedlBitmap(
                            ImageEffectActivity.this
                            , bitmap
                            , beforeCropAngle
                            , rotateAngle
                            , afterCropAngle
                            , cropX
                            , cropY
                            , cropWidth
                            , cropHeight
                            , targetWidth
                            , targetHeight
                            , new TransformUtil.BitmapCallback() {
                              @Override
                              public void onProcessDone(Bitmap bitmap) {
                                System.out.println("#####이미지 transform 적용완료 인덱스번호는 : " + idx + " 번 ");
                                FileUtils.createImageFile(
                                        ImageEffectActivity.this
                                        , bitmap
                                        , new FileUtils.Callback() {
                                          @Override
                                          public void callbackCreateFileSuccess(String path) {
                                            System.out.println("#####이미지 파일변환 완료 인덱스번호는 : " + idx + " 번 ");
                                            System.out.println("#####이미지 파일변환 주소는 : " + path + " 인덱스 번호는 : " + idx + " 번 ");
                                            filePathList.set(idx, path);
                                            willProcessState.put(idx,true);

                                            boolean processDone = true;
                                            for(int i=0;i<willProcessIdxs.size();i++){
                                              if(!willProcessState.get(willProcessIdxs.get(i))){
                                                processDone = false;
                                                break;
                                              }
                                            }

                                            if(processDone){
                                              progressDialog.dismiss();
                                              Intent intent = new Intent();
                                              intent.putStringArrayListExtra(MultiImageSelectorActivity.EXTRA_IMAGE_EFFECT_FILE_PATHS, filePathList);
                                              setResult(RESULT_OK, intent);
                                              finish();
                                            }
                                          }
                                        }, false);
                              }
                            }

                    );

                  }
                }

                , false);
      }


//      for(int i=0;i<mSaveList.size();i++){
//        System.out.println(mSaveList.get(i).toString()+" 파일 idx 번호는 : "+i);
//      }


    } else if (id == R.id.backBtn) {

      Log.i(TAG, "이전버튼 클릭");
      finish();
    }
  }

  private void loadFilterList(int position) {
    System.out.println("필터생성 ! ");
    //ImageEffectFragment currentFragment = (ImageEffectFragment) mImagePagerAdapter.getFragment(position);
    //mFilterAdapter.setTargetPath(currentFragment.getTargetPath());
    //mFilterAdapter.setSelectedFilterType(currentFragment.getCurrentFilterType());
    mFilterAdapter.setImageEffectState(mSaveList.get(position));
    mFilterListView.setAdapter(mFilterAdapter);
  }

  @Override
  public void onFilterSelected(Filter.Type filterType) {
    int fragmentIdx = mImagePager.getCurrentItem();
    ImageEffectFragment currentFragment = (ImageEffectFragment) mImagePagerAdapter.getFragment(fragmentIdx);
    currentFragment.applyFilter(filterType, true);
    onFilterChange(fragmentIdx, filterType);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_CROP_ACTIVITY && resultCode == RESULT_OK) {
      int fragmentIdx = mImagePager.getCurrentItem();
      ImageEffectFragment currentFragment = (ImageEffectFragment) mImagePagerAdapter.getFragment(fragmentIdx);
      int beforeCropAngle = data.getIntExtra(ImageEffectFragment.EXTRA_BEFORE_CROP_ANGLE, 0);
      int cropX = data.getIntExtra(ImageEffectFragment.EXTRA_CROP_X, 0);
      int cropY = data.getIntExtra(ImageEffectFragment.EXTRA_CROP_Y, 0);
      int cropWidth = data.getIntExtra(ImageEffectFragment.EXTRA_CROP_WIDTH, 0);
      int cropHeight = data.getIntExtra(ImageEffectFragment.EXTRA_CROP_HEIGHT, 0);
      currentFragment.applyCrop(beforeCropAngle, cropX, cropY, cropWidth, cropHeight);
    }
  }

  public void onFilterChange(
          int fragmentIdx
          , Filter.Type filterType) {
    ImageEffectFragment.ImageEffectState imageEffectState = mSaveList.get(fragmentIdx);
    imageEffectState.filterType = filterType;
    mSaveList.set(fragmentIdx, imageEffectState);
  }

  @Override
  public void onSaveCropState(
          int fragmentIdx
          , int beforeCropAngle
          , int afterCropAngle
          , int cropX
          , int cropY
          , int cropWidth
          , int cropHeight) {
    ImageEffectFragment.ImageEffectState imageEffectState = mSaveList.get(fragmentIdx);
    imageEffectState.beforeCropAngle = beforeCropAngle;
    imageEffectState.afterCropAngle = afterCropAngle;
    imageEffectState.cropX = cropX;
    imageEffectState.cropY = cropY;
    imageEffectState.cropWidth = cropWidth;
    imageEffectState.cropHeight = cropHeight;
    mSaveList.set(fragmentIdx, imageEffectState);
  }

  @Override
  public void onSaveRotateState(
          int fragmentIdx
          , int rotateAngle
          , int afterCropAngle) {
    ImageEffectFragment.ImageEffectState imageEffectState = mSaveList.get(fragmentIdx);
    imageEffectState.rotateAngle = rotateAngle;
    imageEffectState.afterCropAngle = afterCropAngle;
    mSaveList.set(fragmentIdx, imageEffectState);
  }

  //  @Override
//  public void saveFile(int fragmentIdx, String savePath) {
//    mSavePathList.set(fragmentIdx, savePath);
//  }
}
