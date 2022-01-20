# cutphotos
感谢博主  
https://www.jb51.net/article/144717.htm  
https://github.com/shengge/android-crop  
打开相册选择图片  
Crop.album(Activity activity)  
在onActivityResult生命周期里面进行裁剪  
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
![Image text](https://github.com/Monsterily/cutphotos/blob/master/image/ab.jpg)  
![Image text](https://github.com/Monsterily/cutphotos/blob/master/image/ab2.jpg)  
打开相机选择图片  
Crop.camera(Activity activity)  
在onActivityResult生命周期里面进行裁剪  
 else if (requestCode == REQUEST_CODE_CAPTURE_CAMEIA) {//相机  
       /**  
                 * imageUri 相册图片的路径  
                 * pictureFile 复制一份图片进行操作，避免对原图片造成影响  
                 * pictureFile 图片压缩生成的路径  
                 * 2           裁剪框的形状，1为正方形，2为圆形  
                 * REQUEST_CODE_CAPTURE_CAMEIAE 当前为相机裁剪    相册传REQUEST_CODE_PICK_IMAGE  
                 */  
                Smallphoto(this,userImageUri,pictureFile,pictureFile,1,REQUEST_CODE_CAPTURE_CAMEIA)  
                }  
![Image text](https://github.com/Monsterily/cutphotos/blob/master/image/ca.jpg)  
![Image text](https://github.com/Monsterily/cutphotos/blob/master/image/ca2.jpg)  
