package com.law


import android.app.Activity
import android.content.Intent
import android.graphics.*
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.monsterily.cutphoto.crop.Crop.*
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File




class MainActivity : AppCompatActivity() {
    val REQUEST_CODE_PICK_IMAGE: Int = 0 //选择图片
    val REQUEST_CODE_CAPTURE_CAMEIA: Int = 1 //拍照
    var imageUri: Uri? = null
    val IMAGE_FILE_NAME: String = "1_user_head_icon.jpg" //拍照之后的图片名称
    var pictureFile: File? = null
    var camerapictureFile: File? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        pictureFile = File(
            this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                .toString() + File.separator + "myCapture",
            IMAGE_FILE_NAME
        )
        camerapictureFile = File(
            this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                .toString()
        )
        if (!pictureFile!!.parentFile.exists()) {
            pictureFile!!.parentFile.mkdirs()
        }
        ablum_txt.setOnClickListener {
            album(this)
        }
        camera_txt.setOnClickListener {
            camera(this,pictureFile)
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==200){
            album(this)
        }else if (requestCode==300){
            camera(this,pictureFile)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //判断请求码是否是请求打开相机的那个请求码
        //resultCode参数为返回的信息，为 RESULT_CANCELED表示失败(简单理解)
        if (resultCode == Activity.RESULT_CANCELED) {//失败
            Toast.makeText(this, "获取失败", Toast.LENGTH_SHORT)
        } else if (resultCode == Activity.RESULT_OK) {//成功
            if (requestCode == REQUEST_CODE_PICK_IMAGE) {//相册
                //返回选择图片的uri，参数data.getData()方法获得
                imageUri = data?.data
                //通过uri的方式返回，部分手机uri可能为空
                if (imageUri == null) {
                    //部分手机可能直接存放在bundle中
                    var bundleExtras = data?.getExtras()
                    if (bundleExtras != null) {
                         imageUri = bundleExtras.getParcelable("data")
                    }
                }
                //将获得的图片uri进行压缩再裁剪，不然图片像素过大，可能会出现错误
                /**
                 * imageUri 相册图片的路径
                 * pictureFile 复制一份图片进行操作，避免对原图片造成影响
                 * pictureFile 图片压缩生成的路径
                 * 2           裁剪框的形状，1为正方形，2为圆形
                 * REQUEST_CODE_PICK_IMAGE 当前为相册裁剪    相机传REQUEST_CODE_CAPTURE_CAMEIA
                 */
                Smallphoto(this,imageUri,pictureFile,pictureFile,1,REQUEST_CODE_PICK_IMAGE)
            } else if (requestCode == REQUEST_CODE_CAPTURE_CAMEIA) {//相机
                Smallphoto(this,userImageUri,pictureFile,pictureFile,1,REQUEST_CODE_CAPTURE_CAMEIA)
            } else if (requestCode == REQUEST_CROP) {
                //使用glide进行图片的加载
                Glide.with(this)
                    .load(getCircleBitmap(BitmapFactory.decodeFile(pictureFile.toString())))
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(icon)
            }
        }
    }
}
