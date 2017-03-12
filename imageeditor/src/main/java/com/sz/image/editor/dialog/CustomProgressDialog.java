package com.sz.image.editor.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.TypedValue;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.sz.image.editor.R;


/**
 * Created by jhpark on 2016. 1. 18..
 */
public class CustomProgressDialog extends Dialog {

  //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ현재 사용중
  public static ProgressDialog createPlainProgressDialog(Context mContext) {
    ProgressDialog dialog = new ProgressDialog(mContext);
    try {
      dialog.show();
    } catch (WindowManager.BadTokenException e) {

    }
     dialog.setCancelable(false);

    //progress dialog wheel 을 감싸고있는 네모 박스를 투명하게
    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

    //progress dialog 백그라운드 검은색 blur제거
    dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    //dialog.setIndeterminate(true);

    dialog.setContentView(R.layout.progress_dialog);
    // dialog.setMessage(Message);
    return dialog;
  }
  //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ현재 사용중

  public CustomProgressDialog(Context context) {
    super(context, R.style.CustomProgress);
  }

  public static CustomProgressDialog show(Context context, CharSequence title, CharSequence message) {
    return show(context, title, message, false);
  }
  public static CustomProgressDialog show(Context context, CharSequence title, CharSequence message, boolean indeterminate) {
    return show(context, title, message, indeterminate, false, null);
  }

  public static CustomProgressDialog show(Context context, CharSequence title, CharSequence message, boolean indeterminate, boolean cancelable) {
    return show(context, title, message, indeterminate, cancelable, null);
  }

  public static CustomProgressDialog show(Context context, CharSequence title, CharSequence message, boolean indeterminate, boolean cancelable, OnCancelListener cancelListener) {
    CustomProgressDialog dialog = new CustomProgressDialog(context);
    dialog.setTitle(title);

    dialog.setCancelable(cancelable);
    dialog.setOnCancelListener(cancelListener);
/* The next line will add the ProgressBar to the dialog. */

    final int width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, context.getResources().getDisplayMetrics());
    final int height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, context.getResources().getDisplayMetrics());

    dialog.addContentView(new ProgressBar(context), new LinearLayout.LayoutParams(width,height));

    dialog.show();

    return dialog;

  }
}
