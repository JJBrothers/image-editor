package com.sz.image.editor.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sz.image.editor.ImageEffectFragment;
import com.sz.image.editor.R;
import com.sz.image.editor.filter.Filter;

import java.util.ArrayList;

/**
 * Created by jhpark on 2016. 4. 9..
 */
public class FilterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private final String TAG = "####FilterAdapter";
  private Context mContext;
  private Callback mCallback;
  private ImageEffectFragment.ImageEffectState mImageEffectState;
  private int mTargetWidth;
  private int mTargetHeight;
  private ArrayList<Filter.Type> mFilterList;
  private FilterItemHolder mTempHolder;

  public FilterAdapter(Context context, int width, int height, ArrayList<Filter.Type> list) {
    mContext = context;
    mCallback = (Callback) context;
    mTargetWidth = width;
    mTargetHeight = height;
    mFilterList = list;
  }

  @Override
  public int getItemCount() {
    return mFilterList.size();
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_filter, parent, false);
    RecyclerView.ViewHolder viewHolder = new FilterItemHolder(view);
    return viewHolder;
  }

  private class FilterItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private ImageView filteredImage;
    private TextView filterName;
    private Callback callback;

    public FilterItemHolder(View itemView) {
      super(itemView);
      filteredImage = (ImageView) itemView.findViewById(R.id.filteredImg);
      filterName = (TextView) itemView.findViewById(R.id.filterName);
      itemView.setOnClickListener(this);
    }

    public void setClickListener(Callback callback) {
      this.callback = callback;
    }

    @Override
    public void onClick(View view) {
      int filterIndex = getAdapterPosition();
      Filter.Type filterType = mFilterList.get(filterIndex);
      callback.onFilterSelected(filterType);
    }
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

    final FilterItemHolder itemHolder = (FilterItemHolder) holder;
    final ImageView imgView = itemHolder.filteredImage;
    final TextView textView = itemHolder.filterName;

    //선택된 position의 값과 저장된 FilterIndex의 값이 동일하면 이미지뷰 테두리에 선택된 효과를 준다 .
    if (mImageEffectState.filterType.getName().equals(mFilterList.get(position).getName())) {
      imgView.setBackgroundResource(R.drawable.filter_selected);
      mTempHolder = (FilterItemHolder) holder;
    }

    itemHolder.setClickListener(new Callback() {
      @Override
      public void onFilterSelected(Filter.Type filterType) {
        //같은 필터를 클릭시 return
        if (mTempHolder != null && (mTempHolder == itemHolder)) return;

        if (mTempHolder != null)
          mTempHolder.filteredImage.setBackgroundResource(0);//기존에 선택된 필터의 효과제거

        mTempHolder = itemHolder;
        itemHolder.filteredImage.setBackgroundResource(R.drawable.filter_selected);
        mCallback.onFilterSelected(filterType);
      }
    });

//    Filter.getFilteredBitmap(
//            mContext
//            , mImageEffectState.targetPath
//            , mFilterList.get(position)
//            , mTargetWidth
//            , mTargetHeight
//            , new Filter.Callback() {
//              @Override
//              public void callbackBitmap(Bitmap bitmap) {
//
//                float widthRatio = (float) mTargetWidth / (float) mImageEffectState.targetWidth;
//                float heightRatio = (float) mTargetHeight / (float) mImageEffectState.targetHeight;
//                System.out.println((int) ((float) mImageEffectState.cropX * widthRatio)+"@@");
//                System.out.println((int) ((float) mImageEffectState.cropY * heightRatio)+"@@");
//                System.out.println((int) ((float) mImageEffectState.cropWidth * widthRatio)+"@@");
//                System.out.println((int) ((float) mImageEffectState.cropHeight * heightRatio)+"@@");
//
//                TransformUtil.getTransformedlBitmap(
//                        mContext
//                        , bitmap
//                        , mImageEffectState.beforeCropAngle
//                        , mImageEffectState.rotateAngle
//                        , mImageEffectState.afterCropAngle
//                        , (int) ((float) mImageEffectState.cropX * widthRatio)
//                        , (int) ((float) mImageEffectState.cropY * heightRatio)
//                        , (int) ((float) mImageEffectState.cropWidth * widthRatio)
//                        , (int) ((float) mImageEffectState.cropHeight * heightRatio)
//                        , mTargetWidth
//                        , mTargetHeight
//                        , new TransformUtil.BitmapCallback() {
//                          @Override
//                          public void onProcessDone(Bitmap bitmap) {
//                            imgView.setImageBitmap(bitmap);
//                            textView.setText(mFilterList.get(position).getName());
//                          }
//                        });
//              }
//            }
//            , false);


    Filter.assignFilterImage(
            mContext
            , mImageEffectState.targetPath
            , mFilterList.get(position)
            , mTargetWidth, mTargetHeight
            , textView
            , imgView);
  }

//  private int[] getCropValue() {
//    int[] vals = new int[4];
//    float widthRatio = (float) mTargetWidth / (float) mImageEffectState.targetWidth;
//    float heightRatio = (float) mTargetHeight / (float) mImageEffectState.targetHeight;
//    switch (mImageEffectState.beforeCropAngle) {
//      case 0:
//        vals[0] = (int) ((float) mImageEffectState.cropX * widthRatio);
//        vals[1] = (int) ((float) mImageEffectState.cropY * heightRatio);
//        vals[2] = (int) ((float) mImageEffectState.cropWidth * widthRatio);
//        vals[3] = (int) ((float) mImageEffectState.cropHeight * heightRatio);
//        System.out.println("crop X = " + vals[0]);
//        System.out.println("crop Y = " + vals[1]);
//        System.out.println("crop Width = " + vals[2]);
//        System.out.println("crop Height = " + vals[3]);
//        break;
//      case 90:
//        break;
//      case 180:
//        break;
//      case 270:
//        break;
//    }
//
//    return null;
//  }

//  public void setTargetPath(String targetPath){
//    mTargetPath = targetPath;
//  }



  public void setImageEffectState(ImageEffectFragment.ImageEffectState imageEffectState) {
    mImageEffectState = imageEffectState;
  }

  public interface Callback {
    void onFilterSelected(Filter.Type filterType);
  }
}
