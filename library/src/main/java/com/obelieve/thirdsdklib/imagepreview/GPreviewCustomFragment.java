package com.obelieve.thirdsdklib.imagepreview;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.Target;
import com.previewlibrary.enitity.IThumbViewInfo;
import com.previewlibrary.view.BasePhotoFragment;
import com.obelieve.thirdsdklib.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class GPreviewCustomFragment extends BasePhotoFragment {

    private IThumbViewInfo b;
    private Dialog mBottomDialog;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        b = getBeanViewInfo();
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showBottomDialog(getActivity());
                return false;
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1024) {
            if (PermissionUtil.hasAllPermissionsGranted(grantResults)) {
                savePicture(getContext());
            } else {
                ToastUtil.show(getContext(),"???????????????????????????????????????????????????");
            }

        }
    }

    @SuppressLint("CheckResult")
    private void savePicture(final Context context) {
        if (b != null && !TextUtils.isEmpty(b.getUrl()) && isSdExsit()) {
            final String imgUrl = b.getUrl();
            final String dir = Environment.getExternalStorageDirectory().getAbsolutePath() +
                    File.separator + "2048Sport";
            String filename;
            Drawable drawable = imageView.getDrawable();
            if (!new File(dir).exists()) {
                new File(dir).mkdir();
            }
            if (drawable instanceof GifDrawable) {
                filename = System.currentTimeMillis() + ".gif";
            } else {
                filename = System.currentTimeMillis() + ".jpg";
            }
            final String path = dir + File.separator + filename;
            Observable.create(new ObservableOnSubscribe<Boolean>() {
                @Override
                public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                    try {
                        String cachePath = getGlideCacheImagePath(imgUrl);
                        boolean bool = copyFile(context, cachePath, path);
                        emitter.onNext(bool);
                        emitter.onComplete();
                    } catch (Exception e) {
                        emitter.onError(e);
                    }
                }
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>() {
                @Override
                public void accept(Boolean bool) throws Exception {
                    if (bool) {
                        ToastUtil.show(getContext(),"??????????????????" + dir + "????????????");
                    } else {
                        ToastUtil.show(getContext(),"????????????");
                    }
                }
            }, new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) throws Exception {
                    ToastUtil.show(getContext(),"????????????");
                }
            });
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(com.previewlibrary.R.layout.fragment_image_photo_layout, container, false);
    }

    public void showBottomDialog(final Context context) {
        if (mBottomDialog == null) {
            mBottomDialog = new Dialog(context, R.style.BottomDialog);
            mBottomDialog.setCanceledOnTouchOutside(true);
            SelectPictureActionView view = new SelectPictureActionView(context);
            view.setCallback(new SelectPictureActionView.Callback() {
                @Override
                public void onSavePicture() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if ((context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                            savePicture(context);
                        } else {
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1024);
                        }
                    }
                }

                @Override
                public void onCancel() {
                    mBottomDialog.dismiss();
                }
            });
            mBottomDialog.setContentView(view);
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.width = context.getResources().getDisplayMetrics().widthPixels;
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            view.setLayoutParams(layoutParams);
            if (mBottomDialog.getWindow() != null)
                mBottomDialog.getWindow().setGravity(Gravity.BOTTOM);
        }
        mBottomDialog.show();
    }

    public boolean copyFile(Context context, String oldPath, final String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //???????????????
                InputStream inStream = new FileInputStream(oldPath); //???????????????
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //????????? ????????????
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri uri = Uri.fromFile(new File(newPath));
                intent.setData(uri);
                context.sendBroadcast(intent);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return false;
    }

    private String getGlideCacheImagePath(String imgUrl) {
        String path = null;
        FutureTarget<File> future = Glide.with(this)
                .load(imgUrl)
                .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
        try {
            File cacheFile = future.get();
            path = cacheFile.getAbsolutePath();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return path;
    }


    public static class PermissionUtil {

        /**
         * ????????????????????????
         */
        public static boolean lacksPermission(Context mContexts, String permission) {
            return ContextCompat.checkSelfPermission(mContexts, permission) == PackageManager.PERMISSION_DENIED;
        }

        public static boolean hasAllPermissionsGranted(int[] grantResults) {
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * ??????SD???????????????
     *
     * @return true:??????
     */
    public static boolean isSdExsit() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else
            return false;
    }
    private static class ToastUtil{

        public static void show(Context context,String msg){
            Toast.makeText(context,msg,Toast.LENGTH_SHORT).show();
        }
    }
}
