package com.sz.module;

import android.Manifest;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.sz.image.editor.MultiImageSelectorActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity{

  private final String TAG = "####MainActivity";

  //이미지 선택시 카메라 사용여부.
  private boolean showCamera = true;

  //이미지 최대 선택갯수.
  private int maxNum = 20;

  //이미지 다중선택모드 . MODE_SINGLE = 한장만 선택 .
  private int selectedMode = MultiImageSelectorActivity.MODE_MULTI;

  private final int REQUEST_IMAGE = 2;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Button btn = (Button) findViewById(R.id.editorBtn);
    btn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        new TedPermission(MainActivity.this)
                .setPermissionListener(new PermissionListener() {
                  @Override
                  public void onPermissionGranted() {

                    Intent intent = new Intent(MainActivity.this, MultiImageSelectorActivity.class);
                    intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, showCamera);
                    intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, maxNum);
                    intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, selectedMode);
                    startActivityForResult(intent, REQUEST_IMAGE);
                  }

                  @Override
                  public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                    //Toast.makeText(MainActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
                    Toast.makeText(MainActivity.this, "권한을 거부하여 실행할수없습니다.", Toast.LENGTH_SHORT).show();
                  }
                })
                .setDeniedMessage("권한을 허용하지않으면 이미지에디터를 실행할수없습니다.\n\n[설정] > [권한]에서 해당권한을 활성화해주세요 ")
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();
      }
    });
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == REQUEST_IMAGE && resultCode == RESULT_OK) {
      Log.d(TAG, "이미지도착 ");
      ArrayList<String> paths = data.getStringArrayListExtra("filePaths");

      FileUploadPresenter presenter = new FileUploadPresenter();
      presenter.uploadFile(paths, new IFileUploadPresenter() {
        @Override
        public void onFileUploadSuccess() {

        }
      });
    }
  }
}
