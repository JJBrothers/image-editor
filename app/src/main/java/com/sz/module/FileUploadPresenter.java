package com.sz.module;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Multipart;
import retrofit2.http.PUT;
import retrofit2.http.PartMap;

/**
 * Created by jhpark on 2016. 4. 23..
 */
public class FileUploadPresenter {
  private ApiService mApiService;
  private IFileUploadPresenter mCommunicator;

  public FileUploadPresenter() {
    mApiService = ApiService.getInstance();
  }

  public void uploadFile(ArrayList<String> paths, IFileUploadPresenter communicator) {
    mCommunicator = communicator;
    Service service = mApiService.createService(Service.class);
    for (int i = 0; i < paths.size(); i++) {
      System.out.println(paths.get(i) + " idx = " + i);
      File file = new File(paths.get(i));
      RequestBody requestFile =
              RequestBody.create(MediaType.parse("multipart/form-data"), file);

      Map<String, RequestBody> map = new HashMap<>();
      map.put("image\"; filename=\"" + file.getName(), requestFile);

      Call<ResponseBody> call = service.upload(map);
      call.enqueue(new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call,
                               Response<ResponseBody> response) {
          Log.v("Upload", "success");
          Log.d("############", response.message());
          Log.d("############", String.valueOf(response.code()));
          Log.d("############", String.valueOf(response.headers()));
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
          Log.e("Upload error:", t.getMessage());
        }
      });
    }
  }

  //@part 는 서버에서 받을 일반변수 .
  //@partMap 는 서버에서 multipart 파일 정보를 매핑하여 보냄 .
  interface Service {
    @Multipart
    @PUT("/multipartUploadAjax.do")
    Call<ResponseBody> upload(@PartMap Map<String, RequestBody> file);
  }
}