package com.app.camerademo;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class GalleryActivity extends AppCompatActivity {

    private File photoFile;
    private TipoAdjunto tipoAdjunto;
    private AttachImageAdapter attachImageAdapter;
    private ArrayList<String> arrayFiles;
    private RecyclerView attach_RecyclerViewItems;
    private ImageView main_ivGallery;
    private ImageView main_ivCamera;
    private LinearLayout attach_ContenedorAdjuntos;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        attach_RecyclerViewItems = (RecyclerView) findViewById(R.id.attach_rvItems);
        main_ivGallery = (ImageView) findViewById(R.id.main_ivGallery);
        main_ivCamera = (ImageView) findViewById(R.id.main_ivCamera);
        attach_ContenedorAdjuntos = (LinearLayout) findViewById(R.id.attach_ContenedorAdjuntos);
        this.attach_RecyclerViewItems.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        this.attach_RecyclerViewItems.setLayoutManager(layoutManager);

        this.arrayFiles = new ArrayList<>();
        setListener();
        callAdapter();


        observerLayoutGetSize();



    }

    private void setListener() {

        main_ivGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showGallery();
            }
        });

        main_ivCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCamera();
            }
        });
    }

    private void showCamera() {
        this.tipoAdjunto = TipoAdjunto.Camera;
        if (Permissions.isGrantedPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                dispatchTakePictureIntent();

        } else {
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
            Permissions.verifyPermissions(this, permissions);
        }
    }

    private void setArrayConFilesNames(String path) {
        arrayFiles.add(path);
        this.attachImageAdapter.setFiles(arrayFiles);
        this.attachImageAdapter.notifyDataSetChanged();
    }

    private void callAdapter() {
        this.attachImageAdapter = new AttachImageAdapter(GalleryActivity.this);
        this.attachImageAdapter.setFiles(arrayFiles);
        attach_RecyclerViewItems.setAdapter(attachImageAdapter);
    }

    private void observerLayoutGetSize() {
        ViewTreeObserver observer = this.attach_ContenedorAdjuntos.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                attach_ContenedorAdjuntos.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                attachImageAdapter.setSize(attach_ContenedorAdjuntos.getWidth(),
                        attach_ContenedorAdjuntos.getHeight());
              //  setCount();
                attachImageAdapter.setFiles(arrayFiles);
                attachImageAdapter.notifyDataSetChanged();
            }
        });
    }

    private void resultGalleryKitkatAndHigher(Intent data, Uri uri) {
        // Cuando se escogen más de una imagen.
        ClipData clipData = data.getClipData();
        if (uri == null) {
            for (int i = 0; i < clipData.getItemCount(); i++) {
                    grantUriPermission(getPackageName(), clipData.getItemAt(i).getUri(), Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    setArrayConFilesNames(clipData.getItemAt(i).getUri().toString());
                    //return;

            }
        } else {
            //Cuando se escoge una imagen.
            grantUriPermission(getPackageName(), uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            setArrayConFilesNames(uri.toString());
        }
    }

    private void resultGalleryJellyBeanAndLess(Uri uri) {
        grantUriPermission(getPackageName(), uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        setArrayConFilesNames(uri.toString());
    }

    private void resultCameraCapture() {
        if (photoFile != null) {
           setArrayConFilesNames(photoFile.getPath());
        }
    }

    private void showGalleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        if (Build.VERSION.SDK_INT < 19) {
            //openCalls(true);
            startActivityForResult(intent, Constants.GALLERY_JELLY_BEAN_AND_LESS);
            //openCalls(false);
        } else {
            //openCalls(true);
            String[] mimetypes = {"image/*"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(intent, Constants.GALLERY_KITKAT_AND_HIGHER);
            //openCalls(false);
        }
        //openCalls(false);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == Constants.REQUEST_CODE_PERMISSION && this.tipoAdjunto == TipoAdjunto.Camera) {
            if (Permissions.isGrantedPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                dispatchTakePictureIntent();
            }
        }

        if (requestCode == Constants.REQUEST_CODE_PERMISSION && this.tipoAdjunto == TipoAdjunto.Gallery) {
            if (Permissions.isGrantedPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showGalleryIntent();
            }
        }

        this.tipoAdjunto = TipoAdjunto.Ninguno;
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private File createImageFile() throws IOException {
        String imageFileName = Constants.PREFIX_FILE_IMAGE + new SimpleDateFormat(Constants.FORMAT_DATE_FILE).format(new Date());
        File storageDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists()) {
            boolean result = storageDir.mkdir();
            if (!result) {
                return null;
            }
        }

        return File.createTempFile(
                imageFileName,  /* prefix */
                Constants.SUFFIX_FILE_IMAGE, /* suffix */
                storageDir      /* directory */
        );
    }



    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFile = null;
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                //openCalls(false);
                ex.getStackTrace();
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.app",
                        photoFile);

                List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                //openCalls(true);
                super.startActivityForResult(takePictureIntent, Constants.CAMERA_CAPTURE);
                //openCalls(false);
            }
            //openCalls(false);
        }
    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 0) return;

        // Para la galeria de versiones mayores o iguales a Kitkat
        if (requestCode == Constants.GALLERY_KITKAT_AND_HIGHER) {
            resultGalleryKitkatAndHigher(data, data.getData());
        }

        // Para la galeria de versiones menores a Kitkat.
        else if (requestCode == Constants.GALLERY_JELLY_BEAN_AND_LESS) {
            resultGalleryJellyBeanAndLess(data.getData());
        }

        // Para la camara.
        else if (requestCode == Constants.CAMERA_CAPTURE) {
            resultCameraCapture();
        }



        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showGallery() {
        this.tipoAdjunto = TipoAdjunto.Gallery;
        if (Permissions.isGrantedPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showGalleryIntent();

        } else {
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
            Permissions.verifyPermissions(this, permissions);
        }
    }


}
