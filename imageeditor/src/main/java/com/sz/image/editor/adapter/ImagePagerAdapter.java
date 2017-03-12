package com.sz.image.editor.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.view.ViewGroup;

import com.sz.image.editor.ImageEffectFragment;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jhpark on 2016. 4. 9..
 */
public class ImagePagerAdapter extends FragmentPagerAdapter {

  private Context mContext;
  private ArrayList<String> mTargetPathList;
  private int mTargetWidth;
  private int mTargetHeight;
  private HashMap<Integer,Fragment.SavedState> mSavedFragments = new HashMap<Integer,Fragment.SavedState>();
  private HashMap<Integer, Fragment> mRegisteredFragments = new HashMap<Integer,Fragment>();

  public ImagePagerAdapter(Context context, FragmentManager fm, ArrayList<String> pathList, int width, int height) {
    super(fm);
    mContext = context;
    mTargetPathList = pathList;
    mTargetWidth = width;
    mTargetHeight = height;
  }

  @Override
  public Fragment getItem(int position) {
    ImageEffectFragment imageEffectFragment = new ImageEffectFragment();
    Bundle bundle = new Bundle();
    bundle.putString(ImageEffectFragment.EXTRA_TARGET_PATH,mTargetPathList.get(position));
    bundle.putInt(ImageEffectFragment.EXTRA_TARGET_WIDTH, mTargetWidth);
    bundle.putInt(ImageEffectFragment.EXTRA_TARGET_HEIGHT, mTargetHeight);
    bundle.putInt(ImageEffectFragment.EXTRA_FRAGMENT_INDEX, position);
    imageEffectFragment.setArguments(bundle);
    mRegisteredFragments.put(position,imageEffectFragment);

    if(mSavedFragments.get(position) != null){
      imageEffectFragment.setInitialSavedState(mSavedFragments.get(position));
    }

    return imageEffectFragment;
  }

  @Override
  public void destroyItem(ViewGroup container, int position, Object object) {
    super.destroyItem(container, position, object);
    ImageEffectFragment imageEffectFragment = (ImageEffectFragment)object;
    FragmentManager fragmentManager = imageEffectFragment.getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    Fragment.SavedState savedState = fragmentManager.saveFragmentInstanceState(imageEffectFragment);//Fragment 종료되기전 Fragment 상태정보 저장
    mRegisteredFragments.remove(position); //등록 Fragment삭제
    mSavedFragments.put(position, savedState);//상태정보 저장
    fragmentTransaction.remove(imageEffectFragment);//삭제
    fragmentTransaction.commit();//변경사항 반영
  }

  @Override
  public int getCount() {
    return mTargetPathList.size();
  }

  public Fragment getFragment(int index){
    return mRegisteredFragments.get(index);
  }
}
