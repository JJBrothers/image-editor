package com.sz.image.editor.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.sz.image.editor.R;
import com.sz.image.editor.bean.Folder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment의 popupListWindow의 폴더리스트에 대한 데이터를 관리한다 .
 */
public class FolderAdapter extends BaseAdapter {

  /**
   * 상위 context
   */
  private Context mContext;
  private LayoutInflater mInflater;

  /**
   * folder의 리스트를 담을 list
   */
  private List<Folder> mFolders = new ArrayList<>();

  int mImageSize;

  int lastSelected = 0;

  /**
   * 생성자.데이터 초기화
   */
  public FolderAdapter(Context context) {
    mContext = context;
    mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    mImageSize = mContext.getResources().getDimensionPixelOffset(R.dimen.folder_cover_size);
  }

  /**
   * folder data set
   * @param folders
   */
  public void setData(List<Folder> folders) {
    if (folders != null && folders.size() > 0) {
      mFolders = folders;
    } else {
      mFolders.clear();
    }

    //데이터 재구성.
    notifyDataSetChanged();
  }

  /**
   * 전체보기를 포함한 폴더의 전체갯수.
   * @return size + 1
   */
  @Override
  public int getCount() {
    return mFolders.size() + 1;
  }

  /**
   * 폴더객체 반환
   * @return folder
   */
  @Override
  public Folder getItem(int i) {
    //idx 0번째 폴더면 null을 반환
    if (i == 0) return null;

    return mFolders.get(i - 1);
  }

  /**
   * item id를 반환. default 구성
   * @return idx
   */
  @Override
  public long getItemId(int i) {
    return i;
  }

  /**
   * 폴더에 대한 데이터 할당과 재활용
   * @param i,view,viewGroup
   */
  @Override
  public View getView(int i, View view, ViewGroup viewGroup) {
    ViewHolder holder;

    //view가 null 이면 전체보기, 처음엔 무조건 전체보기가 보여진다 .
    if (view == null) {
      view = mInflater.inflate(R.layout.list_item_folder, viewGroup, false);

      //viewholder class 생성
      holder = new ViewHolder(view);
    } else {
      holder = (ViewHolder) view.getTag();
    }
    if (holder != null) {
      if (i == 0) {
        holder.name.setText("전체보기");
        holder.size.setText(getTotalImageSize() + "개");
        if (mFolders.size() > 0) {
          Folder f = mFolders.get(0);

          /**
           * 메모리 절약을 위해 picasso 대신 glide를 사용한다. 속도도 훨씬 빠르다.
           */
          Glide.with(mContext)
                  .load(new File(f.cover.path))
                  .error(R.drawable.default_error)
                  .override(mImageSize, mImageSize)
                  .centerCrop()
                  .into(holder.cover);
        }
      } else {
        holder.bindData(getItem(i));
      }
      if (lastSelected == i) {
        //체크표시
        holder.indicator.setVisibility(View.VISIBLE);
      } else {
        holder.indicator.setVisibility(View.INVISIBLE);
      }
    }
    return view;
  }

  /**
   * default로 설정된 전체보기시 모든 폴더의 사진갯수를 반환
   * @return result
   */
  private int getTotalImageSize() {
    int result = 0;
    if (mFolders != null && mFolders.size() > 0) {
      for (Folder f : mFolders) {
        result += f.images.size();
      }
    }
    return result;
  }

  /**
   * popupListWindow의 폴더리스트에서 폴더 클릭시. 같은폴더 클릭시 리턴.
   * @param i
   */
  public void setSelectIndex(int i) {
    if (lastSelected == i) return;

    //선택된 idx를 저장
    lastSelected = i;

    //데이터 재구성
    notifyDataSetChanged();
  }

  public int getSelectIndex() {
    return lastSelected;
  }

  class ViewHolder {
    ImageView cover;
    TextView name;
    TextView size;
    ImageView indicator;

    ViewHolder(View view) {
      cover = (ImageView) view.findViewById(R.id.cover);
      name = (TextView) view.findViewById(R.id.name);
      size = (TextView) view.findViewById(R.id.size);
      indicator = (ImageView) view.findViewById(R.id.indicator);
      view.setTag(this);
    }

    /**
     * 데이터 bind
     */
    void bindData(Folder data) {
      name.setText(data.name);
      size.setText(data.images.size() + "개");
      Glide.with(mContext)
              .load(new File(data.cover.path))
              .placeholder(R.drawable.default_error)
              .override(mImageSize, mImageSize)
              .centerCrop()
              .into(cover);

    }
  }

}
