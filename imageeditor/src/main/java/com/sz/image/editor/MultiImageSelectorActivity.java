package com.sz.image.editor;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

/**
 * 액션바의 view 관리. Fragment를 inflate 하여 body에 Fragment 생성.
 * Fragment와의 통신을 근거로 데이터를 저장하거나 해제.
 */
public class MultiImageSelectorActivity extends FragmentActivity implements MultiImageSelectorFragment.Callback {

  private final String TAG = "MultiImageSelectorActivity";
  public static final String EXTRA_IMAGE_EFFECT_FILE_PATHS = "image_effect_save_file_paths";

  /**
   * ImageEffectActivity 요청코드
   */
  private final int REQUEST_IMAGE_EFFECT = 1;

  /**
   * 최대 이미지 선택갯수, 디폴트 9
   */
  public static final String EXTRA_SELECT_COUNT = "max_select_count";

  /**
   * 이미지선택모드. 다중선택, 싱글선택
   */
  public static final String EXTRA_SELECT_MODE = "select_count_mode";

  /**
   * 이미지 리스트내 카메라 촬영 버튼 활성화 유무
   */
  public static final String EXTRA_SHOW_CAMERA = "show_camera";

  /**
   * 이미지 선택 결과 리스트
   */
  public static final String EXTRA_RESULT = "select_result";

  /**
   * 현재 액티비티로 오기전 이미 선택된 사진이 있을시 default로 선택박스 체크
   */
  public static final String EXTRA_DEFAULT_SELECTED_LIST = "default_list";

  /**
   * 이미지 선택 싱글모드, 1장
   */
  public static final int MODE_SINGLE = 0;

  /**
   * 이미지 선택 다중선택모드, 다중선택
   */
  public static final int MODE_MULTI = 1;

  /**
   * 이미지 선택시 담을 결과 리스트
   */
  private ArrayList<String> resultList = new ArrayList<>();

  /**
   * 이미지 선택완료버튼.
   */
  private Button mSubmitButton;

  /**
   * 이미지 효과버튼.
   */
  private Button mEffectButton;

  /**
   * 이전 액티비티에서 사용자가 정의한 이미지 갯수를 담을 변수
   */
  private int mDefaultCount;

  private TextView mTitle;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_multi_image_selector);
    Intent intent = getIntent();

    //사용자가 입력한 이미지 선택 갯수를 받는다.
    mDefaultCount = intent.getIntExtra(EXTRA_SELECT_COUNT, 9);


    //사용자가 입력한 이미지 선택 모드를 변수에 담는다. 0은 싱글,1은 다중
    int mode = intent.getIntExtra(EXTRA_SELECT_MODE, MODE_MULTI);

    //사용자가 입력한 카메라 표시유무를 변수에 담는다 . default 는 true 보여준다.
    boolean isShow = intent.getBooleanExtra(EXTRA_SHOW_CAMERA, true);

    //이전액티비티로 갔다가, 다시 현재 액티비티로 올경우 선택한 사진이 있었다면 resultList 에 이전에 선택한 사진정보를 담는다 .
    if (mode == MODE_MULTI && intent.hasExtra(EXTRA_DEFAULT_SELECTED_LIST)) {
      resultList = intent.getStringArrayListExtra(EXTRA_DEFAULT_SELECTED_LIST);
    }

    //Fragment로 넘겨받은 값들을 다시 넘겨준다.
    Bundle bundle = new Bundle();
    bundle.putInt(MultiImageSelectorFragment.EXTRA_SELECT_COUNT, mDefaultCount);
    bundle.putInt(MultiImageSelectorFragment.EXTRA_SELECT_MODE, mode);
    bundle.putBoolean(MultiImageSelectorFragment.EXTRA_SHOW_CAMERA, isShow);
    bundle.putStringArrayList(MultiImageSelectorFragment.EXTRA_DEFAULT_SELECTED_LIST, resultList);

    /**
     * add 메소드의 첫번째 파라미터로는 해당 Fragment 가 포함될 ViewGroup 을 나타냅니다. 즉 framelayout id가 imagegrid다, fragment 를 이곳에 붙일꺼다add 메소드의 첫번째 파라미터로는 해당 Fragment 가 포함될 ViewGroup 을 나타냅니다. 즉 framelayout id가 imagegrid다, fragment 를 이곳에 붙일꺼다
     * add 메소드의 두번째 파라미터는 프래그먼트 객체와(인스턴스화)하고 이름을 붙인다 . 3번째 파라미터는 인스턴스화된 프래그먼트에 값을 넘길 객체이다add 메소드의 두번째 파라미터는 프래그먼트 객체와(인스턴스화)하고 이름을 붙인다 . 3번째 파라미터는 인스턴스화된 프래그먼트에 값을 넘길 객체이다
     * Fragment 의 변경이 있은 후에는 반드시 commit() 메소드를 호출하여 변경사항을 반영Fragment 의 변경이 있은 후에는 반드시 commit() 메소드를 호출하여 변경사항을 반영
     * commit후 바로 Fragment 객체로 이동
     */
    getSupportFragmentManager().beginTransaction()
            .add(R.id.image_grid, Fragment.instantiate(this, MultiImageSelectorFragment.class.getName(), bundle))
            .commit();

    //뒤로가기버튼.
    findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        setResult(RESULT_CANCELED);
        finish();
      }
    });

    //actionbar title
    mTitle = (TextView) findViewById(R.id.title);

    //초기에는 전체보기로 세팅
    mTitle.setText(R.string.folder_all);

    //전송버튼.
    mSubmitButton = (Button) findViewById(R.id.commit);

    mEffectButton = (Button) findViewById(R.id.effect);

    //선택되었던 사진이 한개도 없다면 버튼 disable
    if (resultList == null || resultList.size() <= 0) {

      //전송,효과버튼 disable
      mSubmitButton.setText("전송");
      mSubmitButton.setEnabled(false);

      mEffectButton.setEnabled(false);

      //전송,효과버튼 enable
    } else {
      mSubmitButton.setText("전송(" + resultList.size() + "/" + mDefaultCount + ")");
      mSubmitButton.setEnabled(true);

      mEffectButton.setEnabled(true);
    }

    //전송버튼 클릭시.
    mSubmitButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        //선택된 사진이 한개라도 있을시 이전 액티비에 값을 전달한다 .
        if (resultList != null && resultList.size() > 0) {
          Intent data = new Intent();
          data.putStringArrayListExtra("filePaths", resultList);
          setResult(RESULT_OK, data);
          finish();
        }
      }
    });

    //효과버튼 클릭시
    mEffectButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        if (resultList != null && resultList.size() > 0) {

          //ImageEffectActivity에 현재 선택된 이미지 경로를 보내 해당 액티비티에서 효과를 처리하도록 한다
          Intent data = new Intent(MultiImageSelectorActivity.this, ImageEffectActivity.class);
          data.putStringArrayListExtra(ImageEffectActivity.EXTRA_RESULT, resultList);
          startActivityForResult(data, REQUEST_IMAGE_EFFECT);
        }

      }
    });
  }

  /**
   * 효과 적용된 이미지를 리턴받는다. 현재 액티비티를 종료시키고 호출한 액티비티로 파일 저장 uri를 넘긴다.
   */
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_IMAGE_EFFECT && resultCode == RESULT_OK) {
      Log.d(TAG, "이미지 선택 액티비티 정상 콜백. 파일 uri를 받자 ! ");
      ArrayList<String> paths = data.getStringArrayListExtra(EXTRA_IMAGE_EFFECT_FILE_PATHS);
      Intent intent = new Intent();
      intent.putStringArrayListExtra("filePaths",paths);
      setResult(RESULT_OK,intent);
      finish();
    }
  }

  /**
   * 싱글 모드에서 이미지 선택 콜백.
   * 싱글 모드에서 이미지 하나를 선택하면 이전 액티비티로 선택된 이미지 경로를 보낸다.
   *
   * @param path 이미지 경로
   */
  @Override
  public void onSingleImageSelected(String path) {
    Intent data = new Intent();
    resultList.add(path);
    data.putStringArrayListExtra(EXTRA_RESULT, resultList);
    setResult(RESULT_OK, data);
    finish();
  }

  /**
   * 다중 선택모드 이미지 선택 콜백.
   * 다중 선택모드에서 이미지를 선택할때마다 list add. 이미지선택갯수 count를 +1
   *
   * @param path 이미지 경로
   */
  @Override
  public void onImageSelected(String path) {
    if (!resultList.contains(path)) {
      resultList.add(path);
    }

    if (resultList.size() > 0) {
      mSubmitButton.setText("전송(" + resultList.size() + "/" + mDefaultCount + ")");
      if (!mSubmitButton.isEnabled()) {
        mSubmitButton.setEnabled(true);

        mEffectButton.setEnabled(true);
      }
    }
  }

  /**
   * 이미지 선택 해제 콜백.
   * 이미지 선택 해제시 list에서 선택된 이미지를 제거, 이미지 선택갯수 count를 -1
   *
   * @param path 이미지 경로
   */
  @Override
  public void onImageUnselected(String path) {
    if (resultList.contains(path)) {
      resultList.remove(path);
      mSubmitButton.setText("전송(" + resultList.size() + "/" + mDefaultCount + ")");
    } else {
      mSubmitButton.setText("전송(" + resultList.size() + "/" + mDefaultCount + ")");
    }

    if (resultList.size() == 0) {
      mSubmitButton.setText("전송");
      mSubmitButton.setEnabled(false);
      mEffectButton.setEnabled(false);
    }
  }

  /**
   * 카메라 촬영 이미지 콜백.
   * 카메라로 촬영한 이미지를 이전액티비티로 전달.
   *
   * @param imageFile 이미지파일
   */
  @Override
  public void onCameraShot(File imageFile) {
    if (imageFile != null) {
      Intent data = new Intent();
      resultList.add(imageFile.getAbsolutePath());
      //data.putStringArrayListExtra(EXTRA_RESULT, resultList);
      data.putStringArrayListExtra("filePaths", resultList);
      setResult(RESULT_OK, data);
      finish();
    }
  }

  /**
   * 폴더이름 콜백.
   * 선택된 폴더의 이름을 액션바의 title로 정의한다.
   *
   * @param name 폴더이름
   */
  @Override
  public void onFolderChanged(String name) {
    if (name == null) {
      mTitle.setText(R.string.folder_all);
    } else {
      mTitle.setText(name);
    }
  }
}
