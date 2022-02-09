package com.monsterily.cutphoto.crop;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;


import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;

import com.monsterily.cutphoto.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;


/**
 * Builder for crop Intents and utils for handling result
 */
public class Crop {

    public static final int REQUEST_CROP = 6709;
    public static final int REQUEST_PICK = 9162;
    public static final int RESULT_ERROR = 404;
    public static final int REQUEST_CODE_PICK_IMAGE = 0;//选择图片
    public static final int REQUEST_CODE_CAPTURE_CAMEIA = 1;//拍照
    public static final int REQUEST_CODE_CUT_CAMEIA = 2;//剪切;
    public static int SHAPE = 1;
    public static Uri userImageUri = null;

    static interface Extra {
        String ASPECT_X = "aspect_x";
        String ASPECT_Y = "aspect_y";
        String MAX_X = "max_x";
        String MAX_Y = "max_y";
        String ERROR = "error";
    }

    private static volatile Crop singleton;
    private Intent cropIntent;

    public static Crop getInstance() {
        if (singleton == null) {
            synchronized (Crop.class) {
                if (singleton == null) {
                    singleton = new Crop();
                }
            }
        }
        return singleton;
    }

    public Crop() {

    }

    /**
     * Create a crop Intent builder with source image
     *
     * @param source Source image URI
     */
    public Crop(Uri source) {
        cropIntent = new Intent();
        cropIntent.setData(source);
    }

    /*
     *输入路径
     */
    public Crop input(Uri source) {
        cropIntent = new Intent();
        cropIntent.setData(source);
        return this;
    }

    /**
     * Set output URI where the cropped image will be saved
     *
     * @param output Output image URI
     */
    public Crop output(Uri output) {
        cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, output);
        return this;
    }

    /**
     * Set fixed aspect ratio for crop area
     *
     * @param x Aspect X
     * @param y Aspect Y
     */
    public Crop withAspect(int x, int y) {
        cropIntent.putExtra(Extra.ASPECT_X, x);
        cropIntent.putExtra(Extra.ASPECT_Y, y);
        return this;
    }

    /**
     * Crop area with fixed 1:1 aspect ratio
     */
    public Crop asSquare() {
        cropIntent.putExtra(Extra.ASPECT_X, 1);
        cropIntent.putExtra(Extra.ASPECT_Y, 1);
        return this;
    }

    /**
     * 1为方形裁剪
     * 2为圆形裁剪
     */
    public Crop setShape(int shape) {
        SHAPE = shape;
        return this;
    }

    /**
     * Set maximum crop size
     *
     * @param width  Max width
     * @param height Max height
     */
    public Crop withMaxSize(int width, int height) {
        cropIntent.putExtra(Extra.MAX_X, width);
        cropIntent.putExtra(Extra.MAX_Y, height);
        return this;
    }

    /**
     * Send the crop Intent!
     *
     * @param activity Activity that will receive result
     */
    public void start(Activity activity) {
        activity.startActivityForResult(getIntent(activity), REQUEST_CROP);
    }

    /**
     * Send the crop Intent!
     *
     * @param context  Context
     * @param fragment Fragment that will receive result
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void start(Context context, Fragment fragment) {
        fragment.startActivityForResult(getIntent(context), REQUEST_CROP);
    }

    Intent getIntent(Context context) {
        if (SHAPE == 1) {
            cropIntent.setClass(context, CropImageActivity.class);
        } else {
            cropIntent.setClass(context, CircleCropImageActivity.class);
        }
        return cropIntent;
    }

    /**
     * Retrieve URI for cropped image, as set in the Intent builder
     *
     * @param result Output Image URI
     */
    public static Uri getOutput(Intent result) {
        return result.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
    }

    /**
     * Retrieve error that caused crop to fail
     *
     * @param result Result Intent
     * @return Throwable handled in CropImageActivity
     */
    public static Throwable getError(Intent result) {
        return (Throwable) result.getSerializableExtra(Extra.ERROR);
    }

    /**
     * Utility method that starts an image picker since that often precedes a crop
     *
     * @param activity Activity that will receive result
     */
    public static void pickImage(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT).setType("image/*");
        try {
            activity.startActivityForResult(intent, REQUEST_PICK);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, R.string.crop__pick_error, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 裁剪
     * file文件所在的路径
     * outfile文件输出的路径
     */
    public static void Cutphoto(Activity activity, File file, File outfile, int shape) {
        Crop.getInstance().input(Uri.fromFile(file)).output(Uri.fromFile(outfile)).asSquare().setShape(shape).start(activity);
    }

    /**
     * 裁剪
     * uri 文件所在的路径
     * outuri 文件输出的路径
     */
    public static void Cutphoto(Activity activity, Uri uri, Uri outuri, int shape) {
        Crop.getInstance().input(uri).output(outuri).asSquare().setShape(shape).start(activity);
    }


    //luban压缩图片再裁剪

    /**
     * imageUri 图片相册保存的路径
     * file 文件路径
     * outfile 输出文件路径
     * shape 裁剪的形状
     */
    public static void Smallphoto(Activity activity, Uri imageUri, File file, File outfile, int shape, int requestCode) {
        if (requestCode == REQUEST_CODE_PICK_IMAGE) {
            copyFolder(activity, imageUri, file, outfile, shape);
        } else {
            luban(activity, file, outfile, shape);
        }

    }

    /**
     * luban压缩
     * file 文件路径
     * outfile 输出文件路径
     * shape 裁剪的形状
     */
    public static void luban(Activity activity, File file, File outfile, int shape) {
        Luban.with(activity)
                .load(file)
                .ignoreBy(100)
                .setTargetDir(file.getParent())
                .setCompressListener(new OnCompressListener() {
                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onSuccess(File file) {
                        Crop.getInstance().input(Uri.fromFile(file)).output(Uri.fromFile(outfile)).asSquare().setShape(shape)
                                .start(activity);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("鲁班压缩出错", "onError: " + e);
                    }
                }).launch();
    }

    public static void copyFolder(Activity activity, Uri oldPath, File newPath, File outfile, int shape) {
        try {
            if (!newPath.exists()) {  //文件不存在时
                newPath.createNewFile();
            }
            InputStream iFile = activity.getContentResolver().openInputStream(oldPath);
            OutputStream oFile = new FileOutputStream(newPath);
            byte[] read = new byte[1024];
            int len = 0;
            while ((len = iFile.read(read)) != -1) {
                oFile.write(read, 0, len);
            }
            oFile.flush();
            oFile.close();
            iFile.close();
            luban(activity, newPath, outfile, shape);
        } catch (Exception e) {
            Log.d("测试", "copyFolder: " + e);
            e.printStackTrace();
        }
    }

    /**
     * @param bitmap src图片
     * @return 将裁剪的图片保存为圆形
     */
    public static Bitmap getCircleBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        //在画布上绘制一个圆
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    //打开相册
    public static void album(Activity activity) {
        //进行权限的判断,查看是否拥有权限
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED
        ) {
            //权限还没有授予，进行申请
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    200
            );//申请的requestcode为200
        } else {
            //如果权限已经申请过，直接进行图片选择
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            //判断系统中是否有处理该Intent的Activity
            if (intent.resolveActivity(activity.getPackageManager()) != null) {
                activity.startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
            } else {
                Toast.makeText(activity, "未找到图片查看器", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //打开相册
    public static void album(Fragment fragment, Activity activity) {
        //进行权限的判断,查看是否拥有权限
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED
        ) {
            //权限还没有授予，进行申请
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    200
            );//申请的requestcode为200
        } else {
            //如果权限已经申请过，直接进行图片选择
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            //判断系统中是否有处理该Intent的Activity
            if (intent.resolveActivity(activity.getPackageManager()) != null) {
                fragment.startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
            } else {
                Toast.makeText(activity, "未找到图片查看器", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //打开相机
    public static void camera(Activity activity, File file) {
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
        ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED
        ) {
            //权限还没有授予，进行申请
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    300
            );//申请的requstCode为300
        } else {
            //权限已经申请
            imageCapture(activity, file);
        }
    }

    //打开相机
    public static void camera(Fragment fragment,Activity activity, File file) {
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
        ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED
        ) {
            //权限还没有授予，进行申请
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    300
            );//申请的requstCode为300
        } else {
            //权限已经申请
            imageCapture(fragment,activity, file);
        }
    }
    //跳转到拍照

    /**
     * pictureFile 存放头像的目录
     *
     * @param activity
     * @param pictureFile
     */
    private static void imageCapture(Activity activity, File pictureFile) {
        Intent intent;
        if (!pictureFile.exists()) {
            pictureFile.getParentFile().mkdir();
        }
        // 判断当前系统，android7.0以上版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            /*FileProvider是ContentProvider的一个子类，用于应用程序之间私有
                文件的传递,需要在清单里配置，代码在下方。实际的getUriForFile就是
       FileProvider.getUriForFile("上下文"，"清单文件中authorities的值"，"共享的文件")；*/
            userImageUri = FileProvider.getUriForFile(
                    activity,
                    "com.monsterily.common.cutphoto.crop", pictureFile
            );
        } else {
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            userImageUri = Uri.fromFile(pictureFile);
        }
        // 去拍照,拍照的结果存到pictureUri对应的路径中
        intent.putExtra(MediaStore.EXTRA_OUTPUT, userImageUri);
        activity.startActivityForResult(intent, REQUEST_CODE_CAPTURE_CAMEIA);
    }


    //跳转到拍照

    /**
     * pictureFile 存放头像的目录
     *
     * @param activity
     * @param pictureFile
     */
    private static void imageCapture(Fragment fragment,Activity activity, File pictureFile) {
        Intent intent;
        if (!pictureFile.exists()) {
            pictureFile.getParentFile().mkdir();
        }
        // 判断当前系统，android7.0以上版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            /*FileProvider是ContentProvider的一个子类，用于应用程序之间私有
                文件的传递,需要在清单里配置，代码在下方。实际的getUriForFile就是
       FileProvider.getUriForFile("上下文"，"清单文件中authorities的值"，"共享的文件")；*/
            userImageUri = FileProvider.getUriForFile(
                    activity,
                    "com.monsterily.common.cutphoto.crop", pictureFile
            );
        } else {
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            userImageUri = Uri.fromFile(pictureFile);
        }
        // 去拍照,拍照的结果存到pictureUri对应的路径中
        intent.putExtra(MediaStore.EXTRA_OUTPUT, userImageUri);
        fragment.startActivityForResult(intent, REQUEST_CODE_CAPTURE_CAMEIA);
    }

}
