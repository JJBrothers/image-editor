package com.sz.image.editor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListPopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.sz.image.editor.adapter.FolderAdapter;
import com.sz.image.editor.adapter.ImageGridAdapter;
import com.sz.image.editor.bean.Folder;
import com.sz.image.editor.bean.Image;
import com.sz.image.editor.util.file.FileUtils;
import com.sz.image.editor.util.file.TimeUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 상위 FragmentActivity로부터 생성된 Fragment.
 * GirdView와 PopupListWindow, 각각의 adapter를 통해 데이터를 보여준다
 * 상위 FragmentActivity와 이벤트발생에 대한 데이터를 보낸다 .
 */
public class MultiImageSelectorFragment extends Fragment {

  private static final String TAG = "MultiImageSelector";

  /**
   * 최대 이미지 선택갯수, 디폴트 9
   */
  public static final String EXTRA_SELECT_COUNT = "max_select_count";

  /**
   * 이미지 선택모드. 다중선택, 싱글선택
   */
  public static final String EXTRA_SELECT_MODE = "select_count_mode";

  /**
   * 이미지 리스트내 카메라 촬영 활성화 유무
   */
  public static final String EXTRA_SHOW_CAMERA = "show_camera";

  /**
   * 현재 액티비티로 오기전 이미 선택된 사진이 있을시 default로 선택박스 체크
   */
  public static final String EXTRA_DEFAULT_SELECTED_LIST = "default_result";

  /**
   * 이미지 선택 싱글모드, 1장
   */
  public static final int MODE_SINGLE = 0;

  /**
   * 이미지 선택 다중선택모드, 다중선택
   */
  public static final int MODE_MULTI = 1;

  /**
   * 전체사진 변수
   */
  private static final int LOADER_ALL = 0;

  /**
   * 전체사진외 폴더별 사진 변수
   */
  private static final int LOADER_CATEGORY = 1;

  /**
   * 카메라 요청 코드
   */
  private static final int REQUEST_CAMERA = 100;


  /**
   * 이미지 선택 결과.
   */
  private ArrayList<String> resultList = new ArrayList<>();

  /**
   * 폴더별 정보를 담고있는 list
   */
  private ArrayList<Folder> mResultFolder = new ArrayList<>();

  /**
   * 이미지를 표시할 gridview
   */
  private GridView mGridView;

  /**
   * 상위 FragmentActivity와 통신을 하기위한 callback
   */
  private Callback mCallback;

  /**
   * 이미지를 표시하기위한 gridview adapter
   */
  private ImageGridAdapter mImageAdapter;

  /**
   * folder 정보 표시하기위한 folder adapter
   */
  private FolderAdapter mFolderAdapter;

  /**
   * 폴더 리스트를 보여주기위한 popupWindow. 클릭시 폴더의 정보를 리스트로 나타냄
   */
  private ListPopupWindow mFolderPopupWindow;

  /**
   * gridview 에서 스크롤시 보여줄 이미지 타임라인.
   */
  private TextView mTimeLineText;

  /**
   * Fragment 하단에 현재 선택된 폴더 표시를 위한 textview
   */
  private TextView mCategoryText;

  /**
   * preview 현재 필요없을듯 ............
   */
  private Button mPreviewBtn;

  /**
   * popup listview 의 위치를 잡아줄 anchor view
   */
  private View mPopupAnchorView;

  /**
   * 사용자가 입력한 이미지 갯수를 변수에 담는다 .
   */
  private int mDesireImageCount;

  /**
   *
   */
  private boolean hasFolderGened = false;

  /**
   * 이미지 리스트내 카메라 촬영 버튼 활성화 유무이미지 리스트내 카메라 촬영 버튼 활성화 유무
   */
  private boolean mIsShowCamera = false;

  /**
   * 이미지를 표시할 그리드뷰 각 셀의 높이와 너비
   */
  private int mGridWidth, mGridHeight;

  /**
   * 임시파일
   */
  private File mTmpFile;

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    try {
      //상위 FragmentActivity와 통신을 위한 callback
      mCallback = (Callback) context;
    } catch (ClassCastException e) {
      throw new ClassCastException("The Activity must implement MultiImageSelectorFragment.Callback interface...");
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_multi_image_selector, container, false);
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    // 상위 액티비티로부터 사용자가 입력한 최대 이미지 선택 갯수의 값을 가져온다
    mDesireImageCount = getArguments().getInt(EXTRA_SELECT_COUNT);

    // 상위 액티비티로부터 사용자가 입력한 이미지 선택 모드를 가져온다.
    final int mode = getArguments().getInt(EXTRA_SELECT_MODE);

    // 상위 액티비티로부터 이미 선택되었던 이미지가 있는지에대한 값을 가져온다. 있으면 list에 add
    if (mode == MODE_MULTI) {
      ArrayList<String> tmp = getArguments().getStringArrayList(EXTRA_DEFAULT_SELECTED_LIST);
      if (tmp != null && tmp.size() > 0) {
        resultList = tmp;
      }
    }

    // 카메라 촬영 버튼 활성화 유무, default true
    mIsShowCamera = getArguments().getBoolean(EXTRA_SHOW_CAMERA, true);

    mImageAdapter = new ImageGridAdapter(getActivity(), mIsShowCamera);

    // 이미지 선택에 대한 선택 효과, single mode에서는 false
    mImageAdapter.showSelectIndicator(mode == MODE_MULTI);

    //popup list를 붙이기위한 anchor를 footer로 잡는다 .
    mPopupAnchorView = view.findViewById(R.id.footer);

    mTimeLineText = (TextView) view.findViewById(R.id.timeline_area);

    mTimeLineText.setVisibility(View.GONE);

    mCategoryText = (TextView) view.findViewById(R.id.category_btn);

    //초기에는 폴더 선택이 전체보기이므로 전체보기를 text에 set 한다 .
    mCategoryText.setText(R.string.folder_all);
    mCategoryText.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        if (mFolderPopupWindow == null) {
          createPopupFolderList(mGridWidth, mGridHeight);
        }

        //popupWindow가 활성화되었을시 text클릭시 닫는다
        if (mFolderPopupWindow.isShowing()) {
          mFolderPopupWindow.dismiss();
        } else {

          //popupWindow가 활성화 되지않았을시 클릭시 open
          mFolderPopupWindow.show();
          int index = mFolderAdapter.getSelectIndex();
          index = index == 0 ? index : index - 1;

          //현재 보고있는 폴더 체크
          mFolderPopupWindow.getListView().setSelection(index);
        }
      }
    });

 /*   mPreviewBtn = (Button) view.findViewById(R.id.preview);
    // 初始化，按钮状态初始化
    if (resultList == null || resultList.size() <= 0) {
      mPreviewBtn.setText(R.string.preview);
      mPreviewBtn.setEnabled(false);
    }
    mPreviewBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

      }
    });*/

    mGridView = (GridView) view.findViewById(R.id.grid);
    mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
      @Override
      public void onScrollStateChanged(AbsListView absListView, int state) {
/*
        final Picasso picasso = Picasso.with(getActivity());
        if (state == SCROLL_STATE_IDLE || state == SCROLL_STATE_TOUCH_SCROLL) {
          picasso.resumeTag(getActivity());
        } else {
          picasso.pauseTag(getActivity());
        }*/

        //gridview 에서 스크롤 정지시 타임라인이 사라진다
        if (state == SCROLL_STATE_IDLE) {
          mTimeLineText.setVisibility(View.GONE);

          //gridview 에서 스크롤시 타임라인 show.
        } else if (state == SCROLL_STATE_FLING) {
          mTimeLineText.setVisibility(View.VISIBLE);
        }
      }


      /**
       * 스크롤시에 타임라인에 보여줄 해당 사진의 날짜정보를 보여준다 .
       * @param view,firstVisibleItem,visibleItemCount,totalItemCount
       * firstVisibleItemfirstVisibleItem = 카메라 촬영버튼, visibleItemCount = 보여지고있는 아이템 갯수, totalItemCounttotalItemCount = 총 아이템 갯수
       */
      @Override
      public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (mTimeLineText.getVisibility() == View.VISIBLE) {
          int index = firstVisibleItem + 1 == view.getAdapter().getCount() ? view.getAdapter().getCount() - 1 : firstVisibleItem + 1;
          Image image = (Image) view.getAdapter().getItem(index);
          if (image != null) {
            mTimeLineText.setText(TimeUtils.formatPhotoDate(image.path));
          }
        }
      }
    });


    mGridView.setAdapter(mImageAdapter);

    //gridview 의 셀당 가로와,세로 크기를 구한다 . 이미지 할당을 위해서 gridview의 크기를 구해 할당하는 것이 메모리절약에 도움이 된다 .
    mGridView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override
      public void onGlobalLayout() {

        final int width = mGridView.getWidth();
        final int height = mGridView.getHeight();

        mGridWidth = width;
        mGridHeight = height;

        //
        final int desireSize = getResources().getDimensionPixelOffset(R.dimen.image_size);
        final int numCount = width / desireSize;
        final int columnSpace = getResources().getDimensionPixelOffset(R.dimen.space_size);
        int columnWidth = (width - columnSpace * (numCount - 1)) / numCount;
        mImageAdapter.setItemSize(columnWidth);
        mGridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
      }
    });


    mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        //이미지 선택에서 카메라 촬영 버튼 활성화시
        if (mImageAdapter.isShowCamera()) {

          //첫번째아이템은 카메라아이콘, 카메라를 호출한다 .
          if (i == 0) {
            showCameraAction();

            //선택한 이미지를 담는다.
          } else {
            Image image = (Image) adapterView.getAdapter().getItem(i);
            selectImageFromGrid(image, mode);
          }
        } else {
          Image image = (Image) adapterView.getAdapter().getItem(i);
          selectImageFromGrid(image, mode);
        }
      }
    });

    mFolderAdapter = new FolderAdapter(getActivity());
  }

  /**
   * 폴더 리스트를 만드는 popupListView
   * @param width, height 그리드뷰 가로세로 크기
   */
  private void createPopupFolderList(int width, int height) {
    mFolderPopupWindow = new ListPopupWindow(getActivity());
    mFolderPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
    mFolderPopupWindow.setAdapter(mFolderAdapter);
    mFolderPopupWindow.setContentWidth(width);
    mFolderPopupWindow.setWidth(width);

    //세로는 gridview에 8분의 5만큼 만든다
    mFolderPopupWindow.setHeight(height * 6 / 8);
    //mFolderPopupWindow.setHeight(height);

    //popuplistView 를 anchor뷰(footer) 상단에 위치시킨다 .
    mFolderPopupWindow.setAnchorView(mPopupAnchorView);

    //애니메이션 add -1 , 0은 없음
    mFolderPopupWindow.setAnimationStyle(-1);

    mFolderPopupWindow.setModal(true);
    mFolderPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        mFolderAdapter.setSelectIndex(i);

        final int index = i;
        final AdapterView v = adapterView;

        new Handler().postDelayed(new Runnable() {
          @Override
          public void run() {
            mFolderPopupWindow.dismiss();

            //전체보기 클릭시
            if (index == 0) {

              //loader 재시작
              getActivity().getSupportLoaderManager().restartLoader(LOADER_ALL, null, mLoaderCallback);

              //전체보기 text 변경
              mCategoryText.setText(R.string.folder_all);

              //변경시 기존의 카메라 활성여부를 set
              if (mIsShowCamera) {
                mImageAdapter.setShowCamera(true);
              } else {
                mImageAdapter.setShowCamera(false);
              }

              mCallback.onFolderChanged(null);

              //0번(전체보기)를 제외한 다른 폴더 클릭시
            } else {
              Folder folder = (Folder) v.getAdapter().getItem(index);
              if (null != folder) {
                mImageAdapter.setData(folder.images);
                mCategoryText.setText(folder.name);
                if (resultList != null && resultList.size() > 0) {
                  mImageAdapter.setDefaultSelected(resultList);
                }

                //폴더이름을 상위 액티비티에게 전달
                mCallback.onFolderChanged(folder.name);
              }
              if (mIsShowCamera) {
                mImageAdapter.setShowCamera(true);
              } else {
                mImageAdapter.setShowCamera(false);
              }
            }

            mGridView.smoothScrollToPosition(0);


          }
        }, 100);

      }
    });
  }

  /**
   * 모든 뷰가 만들어지고 전체 사진을 load 한다 .
   */
  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    getActivity().getSupportLoaderManager().initLoader(LOADER_ALL, null, mLoaderCallback);
  }

  /**
   * 카메라 사진 촬영후, 결과값을 받는다.
   * @param requestCode,resultCode,data. 요청코드,결과코드,데이터
   */
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    // 카메라로 사진 촬영후 상위 액티비티로 촬영 사진 경로 반환
    if (requestCode == REQUEST_CAMERA) {
      if (resultCode == Activity.RESULT_OK) {

        //카메라 촬영을 요청할때 같이보낸 tmpFile을 돌려받는다 .
        if (mTmpFile != null) {
          if (mCallback != null) {
            mCallback.onCameraShot(mTmpFile);
          }
        }
      } else {
        if (mTmpFile != null && mTmpFile.exists()) {
          mTmpFile.delete();
        }
      }
    }
  }

  /**
   * 화면 전환시 호출. gridview의 크기를 다시구해 열의 수와 이미지 사이즈를 할당한다
   * @param newConfig
   */
  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    Log.d(TAG, "on change");

    if (mFolderPopupWindow != null) {
      if (mFolderPopupWindow.isShowing()) {
        mFolderPopupWindow.dismiss();
      }
    }

    mGridView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override
      public void onGlobalLayout() {

        final int height = mGridView.getHeight();

        final int desireSize = getResources().getDimensionPixelOffset(R.dimen.image_size);
        Log.d(TAG, "Desire Size = " + desireSize);
        final int numCount = mGridView.getWidth() / desireSize;
        Log.d(TAG, "Grid Size = " + mGridView.getWidth());
        Log.d(TAG, "num count = " + numCount);
        final int columnSpace = getResources().getDimensionPixelOffset(R.dimen.space_size);
        int columnWidth = (mGridView.getWidth() - columnSpace * (numCount - 1)) / numCount;
        mImageAdapter.setItemSize(columnWidth);

        if (mFolderPopupWindow != null) {
          mFolderPopupWindow.setHeight(height * 5 / 8);
        }
        mGridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        //mGridView.getViewTreeObserver().removeGlobalOnLayoutListener(this);

      }
    });

    super.onConfigurationChanged(newConfig);

  }

  /**
   * 카메라를 호출하여 사진촬영. tmpFile을 만들어 카메라를 호출할때 같이 보낸다.
   */
  private void showCameraAction() {
    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {

      mTmpFile = FileUtils.createTmpFile(getActivity());

      //tmpFile을 intent에 put하여, 요청결과값으로 tmpFile의 촬영사진정보를 돌려받는다 .
      cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTmpFile));
      startActivityForResult(cameraIntent, REQUEST_CAMERA);
    } else {
      Toast.makeText(getActivity(), R.string.msg_no_camera, Toast.LENGTH_SHORT).show();
    }
  }

  /**
   * 선택한 이미지를 상위 액티비티로 값을 전달한다.
   * @param image
   */
  private void selectImageFromGrid(Image image, int mode) {
    if (image != null) {

      //현재 다중선택모드이면
      if (mode == MODE_MULTI) {
        if (resultList.contains(image.path)) {
          resultList.remove(image.path);
         /* if (resultList.size() != 0) {
            mPreviewBtn.setEnabled(true);
            mPreviewBtn.setText(getResources().getString(R.string.preview) + "(" + resultList.size() + ")");
          } else {
            mPreviewBtn.setEnabled(false);
            mPreviewBtn.setText(R.string.preview);
          }*/
          if (mCallback != null) {
            mCallback.onImageUnselected(image.path);
          }
        } else {
          // 사용자가 입력한 최대 사진 선택 갯수와 선택된 사진의 갯수를 비교 같으면 toast 출력
          if (mDesireImageCount == resultList.size()) {
            Toast.makeText(getActivity(), R.string.msg_amount_limit, Toast.LENGTH_SHORT).show();
            return;
          }

          resultList.add(image.path);
         /* mPreviewBtn.setEnabled(true);
          mPreviewBtn.setText(getResources().getString(R.string.preview) + "(" + resultList.size() + ")");*/
          if (mCallback != null) {
            mCallback.onImageSelected(image.path);
          }
        }
        mImageAdapter.select(image);
      } else if (mode == MODE_SINGLE) {
        if (mCallback != null) {
          mCallback.onSingleImageSelected(image.path);
        }
      }
    }
  }

  /**
   * LoaderManager 사용으로 비동기적으로 데이터를 처리한다 .
   * Cursor사용으로 안드로이드 내부 sql lite를 활용해 이미지를 로드한다 .
   */
  private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

    private final String[] IMAGE_PROJECTION = {
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media._ID};

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

      //전체사진 로드
      if (id == LOADER_ALL) {
        CursorLoader cursorLoader = new CursorLoader(getActivity(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                null, null, IMAGE_PROJECTION[2] + " DESC");
        return cursorLoader;
      } else if (id == LOADER_CATEGORY) {
        //선택된 폴더 사진 로드

        CursorLoader cursorLoader = new CursorLoader(getActivity(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                IMAGE_PROJECTION[0] + " like '%" + args.getString("path") + "%'", null, IMAGE_PROJECTION[2] + " DESC");
        return cursorLoader;
      }

      return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
      if (data != null) {
        List<Image> images = new ArrayList<>();
        int count = data.getCount();
        if (count > 0) {
          data.moveToFirst();
          do {
            String path = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
            String name = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
            long dateTime = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
            Image image = new Image(path, name, dateTime);
            images.add(image);
            if (!hasFolderGened) {

              File imageFile = new File(path);

              //폴더를구한다
              File folderFile = imageFile.getParentFile();
              Folder folder = new Folder();

              //폴더 세팅
              folder.name = folderFile.getName();
              folder.path = folderFile.getAbsolutePath();
              folder.cover = image;
              if (!mResultFolder.contains(folder)) {
                List<Image> imageList = new ArrayList<>();
                imageList.add(image);
                folder.images = imageList;
                mResultFolder.add(folder);
              } else {
                Folder f = mResultFolder.get(mResultFolder.indexOf(folder));
                f.images.add(image);
              }
            }

          } while (data.moveToNext());

          //gridview에 data 세팅. 데이터 재호출.(notifyDataSetChanged)
          mImageAdapter.setData(images);


          if (resultList != null && resultList.size() > 0) {
            mImageAdapter.setDefaultSelected(resultList);
          }

          mFolderAdapter.setData(mResultFolder);
          hasFolderGened = true;

        }
      }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
  };

  /**
   * callback interface
   */
  public interface Callback {
    void onSingleImageSelected(String path);

    void onImageSelected(String path);

    void onImageUnselected(String path);

    void onCameraShot(File imageFile);

    void onFolderChanged(String name);
  }
}
