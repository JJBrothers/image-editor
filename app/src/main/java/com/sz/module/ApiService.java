package com.sz.module;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by jhpark on 2016. 4. 23..
 */
public class ApiService {
  public static final String API_BASE_URL = "http://192.168.219.101:8080/";
  private OkHttpClient.Builder mHttpClient;
  private Retrofit.Builder mBuilder;

  private static ApiService mApiService = new ApiService();

  private ApiService() {
    mHttpClient = new OkHttpClient.Builder();
    mBuilder = new Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create());
  }

  public static ApiService getInstance(){
    return mApiService;
  }

  public <S> S createService(Class<S> serviceClass) {
    Retrofit retrofit = mBuilder.client(mHttpClient.build()).build();
    return retrofit.create(serviceClass);
  }
}
