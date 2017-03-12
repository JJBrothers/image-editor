package com.sz.image.editor.util.file;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;

import com.sz.image.editor.dialog.CustomProgressDialog;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * File Util.
 * 파일처리에 대한 task를 담당.
 */
public class FileUtils {

  public static File createTmpFile(Context context) {

    String state = Environment.getExternalStorageState();
    if (state.equals(Environment.MEDIA_MOUNTED)) {

      File pic = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
      String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss.SSS", Locale.KOREA).format(new Date());
      String fileName = "multi_image_" + timeStamp + "";
      File tmpFile = new File(pic, fileName + ".jpg");
      return tmpFile;
    } else {
      File cacheDir = context.getCacheDir();
      String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss.SSS", Locale.KOREA).format(new Date());
      String fileName = "multi_image_" + timeStamp + "";
      File tmpFile = new File(cacheDir, fileName + ".jpg");
      return tmpFile;
    }

  }

  /**
   * 필터 효과를 적용하거나, 이미지 자르기, 이미지 회전 등
   * 전송을 하기위한 임시 파일을 저장한다.
   *
   * @param context
   * @param bitmap
   * @return savedPath
   */
  public static String createImageTempFile(Context context, Bitmap bitmap) {

    //임시저장경로생성
    String dirPath = context.getCacheDir().getPath() + "/temp/";

    System.out.println(dirPath + "##dirPath");

    String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss.SSS", Locale.KOREA).format(new Date());

    //임시저장경로
    File file = new File(dirPath);

    //임시저장 경로 폴더가 존재하지않으면 만든다
    if (!file.exists()) {
      file.mkdir();
    }

    String path = dirPath + fileName + ".png";
    //현재 이 이미지를 crop 한 파일이 있으면 삭제하고 새로저장
    File fileCacheItem = new File(path);

    OutputStream out = null;

    try {
      fileCacheItem.createNewFile();
      out = new FileOutputStream(fileCacheItem);

      //비트맵을 설정된 경로에 생
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
      System.out.println("##//파일생성");
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        out.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return path;
  }

  public static void createImageFile(final Context context, Bitmap bitmap, final Callback callback, final boolean dialogBool) {


    new AsyncTask<Bitmap, Void, String>() {

      private ProgressDialog progressDialog;

      @Override
      protected void onPreExecute() {
        super.onPreExecute();
        if (dialogBool) {
          progressDialog = CustomProgressDialog.createPlainProgressDialog(context);
        }
      }

      @Override
      protected String doInBackground(Bitmap... bitmaps) {

        String dirPath = context.getCacheDir().getPath() + "/temp/";
        System.out.println(dirPath + "##dirPath");
        String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss.SSS", Locale.KOREA).format(new Date());
        //임시저장경로
        File file = new File(dirPath);
        //임시저장 경로 폴더가 존재하지않으면 만든다
        if (!file.exists()) {
          file.mkdir();
        }
        String path = dirPath + fileName + ".jpg";
        File fileCacheItem = new File(path);
        OutputStream out = null;
        try {
          fileCacheItem.createNewFile();
          out = new FileOutputStream(fileCacheItem);
          //BufferedWriter out1 = new BufferedWriter(new FileWriter(fileCacheItem), 32768);
          ByteArrayOutputStream bytes = new ByteArrayOutputStream();
          bitmaps[0].compress(Bitmap.CompressFormat.JPEG, 100, bytes);
          //bitmaps[0].compress(Bitmap.CompressFormat.PNG, 100, bytes);
          out.write(bytes.toByteArray());
          out.flush();
          //bitmaps[0].compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
          e.printStackTrace();
        } finally {
          try {
            out.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }

        return path;
      }

      @Override
      protected void onPostExecute(String path) {
        super.onPostExecute(path);
        if (dialogBool) {
          progressDialog.dismiss();
        }
        callback.callbackCreateFileSuccess(path);
      }
    }.execute(bitmap);
  }


  public static void test(final Context context, Bitmap bitmap) {
    ///storage/emulated/0/DCIM/Camera/20160418_220932.jpg


    new AsyncTask<Bitmap, Void, String>() {

      private ProgressDialog progressDialog;

      @Override
      protected String doInBackground(Bitmap... bitmaps) {
        String dirPath = context.getCacheDir().getPath() + "/temp/";
        System.out.println(dirPath + "##dirPath");

        //임시저장경로
        File file = new File(dirPath);
        //임시저장 경로 폴더가 존재하지않으면 만든다
        if (!file.exists()) {
          file.mkdir();
        }
        OutputStream out = null;

        try {
          for (int i = 0; i < 20; i++) {
            String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss.SSS", Locale.KOREA).format(new Date());
            String path = dirPath + fileName + ".png";
            File fileCacheItem = new File(path);

            fileCacheItem.createNewFile();
            out = new FileOutputStream(fileCacheItem);
            //BufferedWriter out1 = new BufferedWriter(new FileWriter(fileCacheItem), 32768);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmaps[0].compress(Bitmap.CompressFormat.JPEG, 100, bytes);

            out.write(bytes.toByteArray());
            System.out.println("파일 생성 !! ");
          }
          out.flush();

          //bitmaps[0].compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
          e.printStackTrace();
        } finally {
          try {
            out.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }

        return null;
      }

    }.execute(bitmap);


  }

  public interface Callback {
    public void callbackCreateFileSuccess(String path);
  }

}
