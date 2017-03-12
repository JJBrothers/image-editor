package com.sz.image.editor.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.sz.image.editor.R;
import com.sz.image.editor.bean.Image;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment의 GridView 의 출력 이미지의 데이터를 관리한다 .
 */
public class ImageGridAdapter extends BaseAdapter {

  /**
   * 카메라 상수 0
   */
  private static final int TYPE_CAMERA = 0;

  /**
   * 카메라를 제외한 일반 이미지 상수 1
   */
  private static final int TYPE_NORMAL = 1;

  /**
   * 상위 context
   */
  private Context mContext;

  private LayoutInflater mInflater;
  private boolean showCamera = true;

  /**
   * single mode 일때는 false, multi mode 일때는 true, default true
   */
  private boolean showSelectIndicator = true;

  /**
   * 이미지 정보 list
   */
  private List<Image> mImages = new ArrayList<>();
  private List<Image> mSelectedImages = new ArrayList<>();

  /**
   * 이미지 크기.
   */
  private int mItemSize;

  /**
   * gridview 레이아웃 정의.
   */
  private GridView.LayoutParams mItemLayoutParams;

  public ImageGridAdapter(Context context, boolean showCamera) {
    mContext = context;
    mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    this.showCamera = showCamera;
    mItemLayoutParams = new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, GridView.LayoutParams.MATCH_PARENT);
  }

  /**
   * 이미지 선택에 대한 indicator 표시여부
   * @param b
   */
  public void showSelectIndicator(boolean b) {
    showSelectIndicator = b;
  }

  /**
   * gridview에 카메라 이미지 활성여부
   * @param b
   */
  public void setShowCamera(boolean b) {
    if (showCamera == b) return;

    showCamera = b;
    notifyDataSetChanged();
  }

  /**
   * 카메라 활성화 여부 반환
   * @return boolean
   */
  public boolean isShowCamera() {
    return showCamera;
  }

  /**
   * 이미지 선택 add or remove, dataset change
   * @param image
   */
  public void select(Image image) {
    if (mSelectedImages.contains(image)) {
      mSelectedImages.remove(image);
    } else {
      mSelectedImages.add(image);
    }
    notifyDataSetChanged();
  }

  /**
   * 선택된 이미지가 있다면 add
   * @param resultList
   */
  public void setDefaultSelected(ArrayList<String> resultList) {
    for (String path : resultList) {
      Image image = getImageByPath(path);
      if (image != null) {
        mSelectedImages.add(image);
      }
    }
    if (mSelectedImages.size() > 0) {
      notifyDataSetChanged();
    }
  }

  /**
   * 이미지 경로 반환
   * @param path
   */
  private Image getImageByPath(String path) {
    if (mImages != null && mImages.size() > 0) {
      for (Image image : mImages) {
        if (image.path.equalsIgnoreCase(path)) {
          return image;
        }
      }
    }
    return null;
  }

  /**
   * image data set
   * @param images
   */
  public void setData(List<Image> images) {
    mSelectedImages.clear();

    if (images != null && images.size() > 0) {
      mImages = images;
    } else {
      mImages.clear();
    }

    //gridview의 데이터를 재구성한다 .
    notifyDataSetChanged();
  }

  /**
   * gridview width, height set
   * @param columnWidth
   */
  public void setItemSize(int columnWidth) {

    if (mItemSize == columnWidth) {
      return;
    }

    mItemSize = columnWidth;

    mItemLayoutParams = new GridView.LayoutParams(mItemSize, mItemSize);

    notifyDataSetChanged();
  }

  /**
   * viewType의 종류는 카메라 촬영에 들어가는 이미지데이터와 일반 이미지데이터 2가지
   * @return 2
   */
  @Override
  public int getViewTypeCount() {
    return 2;
  }

  /**
   * gridview에 할당된 item 별 상수 반환 0이면 카메라이미지 , 1이면 일반이미지
   * @return TYPE_CAMERA, TYPE_NORMAL
   */
  @Override
  public int getItemViewType(int position) {
    if (showCamera) {
      return position == 0 ? TYPE_CAMERA : TYPE_NORMAL;
    }
    return TYPE_NORMAL;
  }

  /**
   * 이미지 전체 사이즈를 반환. 카메라 활성화시 size +1, 비활성시 size
   * @return images.size()
   */
  @Override
  public int getCount() {
    return showCamera ? mImages.size() + 1 : mImages.size();
  }

  /**
   * 이미지 정보 반환
   * @return Image
   */
  @Override
  public Image getItem(int i) {
    if (showCamera) {
      if (i == 0) {
        return null;
      }
      return mImages.get(i - 1);
    } else {
      return mImages.get(i);
    }
  }

  /**
   * 데이터의 id 반환,현재 default
   * @return idx
   */
  @Override
  public long getItemId(int i) {
    return i;
  }

  @Override
  public View getView(int i, View view, ViewGroup viewGroup) {

    int type = getItemViewType(i);
    if (type == TYPE_CAMERA) {
      view = mInflater.inflate(R.layout.list_item_camera, viewGroup, false);
      view.setTag(null);
    } else if (type == TYPE_NORMAL) {
      ViewHolde holde;
      if (view == null) {
        view = mInflater.inflate(R.layout.list_item_image, viewGroup, false);
        holde = new ViewHolde(view);
      } else {
        holde = (ViewHolde) view.getTag();
        if (holde == null) {
          view = mInflater.inflate(R.layout.list_item_image, viewGroup, false);
          holde = new ViewHolde(view);
        }
      }
      if (holde != null) {
        holde.bindData(getItem(i));
      }
    }

    /** Fixed View Size */
    GridView.LayoutParams lp = (GridView.LayoutParams) view.getLayoutParams();
    if (lp.height != mItemSize) {
      view.setLayoutParams(mItemLayoutParams);
    }

    return view;
  }

  class ViewHolde {
    ImageView image;
    ImageView indicator;
    View mask;

    ViewHolde(View view) {
      image = (ImageView) view.findViewById(R.id.image);
      indicator = (ImageView) view.findViewById(R.id.checkmark);
      mask = view.findViewById(R.id.mask);
      view.setTag(this);
    }

    void bindData(final Image data) {
      if (data == null) return;

      //single mode 에서는 false. multi mode 일때만 true
      if (showSelectIndicator) {
        indicator.setVisibility(View.VISIBLE);
        if (mSelectedImages.contains(data)) {

          indicator.setImageResource(R.drawable.btn_selected);
          mask.setVisibility(View.VISIBLE);
        } else {

          indicator.setImageResource(R.drawable.btn_unselected);
          mask.setVisibility(View.GONE);
        }
      } else {
        indicator.setVisibility(View.GONE);
      }
      File imageFile = new File(data.path);

      if (mItemSize > 0) {
        Glide.with(mContext)
                .load(imageFile)
                .placeholder(R.drawable.default_error)
                        //.error(R.drawable.default_error)
                .override(mItemSize, mItemSize)
                .centerCrop()
                .into(image);
      }
    }
  }

}
