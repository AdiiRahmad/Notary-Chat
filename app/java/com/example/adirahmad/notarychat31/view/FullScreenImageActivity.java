package com.example.adirahmad.notarychat31.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.example.adirahmad.notarychat31.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class FullScreenImageActivity extends AppCompatActivity {

    private ImageView mImageView;

    private ProgressDialog progressDialog;

    String urlImage;
    private String imageURL;
    CountDownTimer countdowntimer = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);


        Bundle extras = getIntent().getExtras();

        if (extras != null){
            urlImage    =   extras.getString("imgkey");
//            idGambar = "-";
//            idGambar.concat(extras.getString("gamb"));
//            Log.d("Test", idGambar);
        }else{
            urlImage    =   "";
        }

        bindViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        setValues();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        System.gc();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            onBackPressed();
        }else if(id == R.id.SimpanGambar ){
            galleryAddPic(imageURL);
        }
        return super.onOptionsItemSelected(item);
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_fullscreen, menu);
        return true;
    }


    private void bindViews(){
        progressDialog = new ProgressDialog(this);
        mImageView = (ImageView) findViewById(R.id.imageView);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //tvUser = (TextView)toolbar.findViewById(R.id.title);



        Glide.with(FullScreenImageActivity.this)
                .load(urlImage)
                .asBitmap()
                .override(640,640)
                .fitCenter()
                .into(new SimpleTarget<Bitmap>(100,100) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        imageURL = saveImage(resource);
                        mImageView.setImageBitmap(resource);

                    }
                });

    }

    private void galleryAddPic(String imagePath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imagePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
        Toast.makeText(this, "Gambar disimpan", Toast.LENGTH_SHORT).show();
    }

//    private void deleteImage(String imageURL){
//        File carigambar = new File(imageURL);
//        if(carigambar.exists()){
//
//            carigambar.delete();
//            Toast.makeText(this, "Gambar Dihapus", Toast.LENGTH_SHORT).show();
//
//        }else{
//            Toast.makeText(this, "Gambar Tidak Ditemukan", Toast.LENGTH_SHORT).show();
//
//        }
//
//    }

    private String saveImage(Bitmap image) {
        String savedImagePath = null;
        String namaFile = "IMG_" + new SimpleDateFormat("yyyyMMddhhmm", Locale.getDefault()).format(new Date());
        String imageFileName = namaFile + ".jpg";
        File storageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        + "/ImagesNotary");
        boolean success = true;
        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        }
        if (success) {
            File imageFile = new File(storageDir, imageFileName);
            savedImagePath = imageFile.getAbsolutePath();
            try {
                OutputStream fOut = new FileOutputStream(imageFile);
                image.compress(Bitmap.CompressFormat.JPEG, 100  , fOut);
                fOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Add the image to the system gallery
            //galleryAddPic(savedImagePath);
        }
        return savedImagePath;
    }


//    private void setValues(){
//        String nameUser,urlPhotoUser,urlPhotoClick;
//        nameUser = getIntent().getStringExtra("nameUser");
//        urlPhotoUser = getIntent().getStringExtra("urlPhotoUser");
//        urlPhotoClick = getIntent().getStringExtra("urlPhotoClick");
//        Log.i("TAG","imagem recebida "+urlPhotoClick);
//        //tvUser.setText(nameUser); // Name
//        Glide.with(this).load(urlPhotoUser).centerCrop().transform(new CircleTransform(this)).override(40,40).into(ivUser);
//
//        Glide.with(this).load( urlPhotoClick).asBitmap().override(640,640).fitCenter().into(new SimpleTarget<Bitmap>() {
//
//            @Override
//            public void onLoadStarted(Drawable placeholder) {
//                progressDialog.setMessage("Carregando Imagem...");
//                progressDialog.show();
//            }
//
//            @Override
//            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
//                progressDialog.dismiss();
//                mImageView.setImageBitmap(resource);
//            }
//
//            @Override
//            public void onLoadFailed(Exception e, Drawable errorDrawable) {
//                Toast.makeText(FullScreenImageActivity.this,"Erro, tente novamente",Toast.LENGTH_LONG).show();
//                progressDialog.dismiss();
//            }
//        });
//    }

}
