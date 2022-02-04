package com.harman.android.myprojectforinternship;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final int CAMERA_PERMISSIONS_CODE = 322;
    private static final int MAP_CONTENT_PERMISSIONS_CODE = 323;
    private static final int defaultWidth = 20;
    private static final int defaultHeight = 20;
    private static final int initWidth = 1000;
    private static final int initHeight = 1000;

    private float currentZoom = -1;
    Dialog dialog;
    SupportMapFragment mapFragment;
    GoogleMap map;
    private List<String> noGetPermissions = new ArrayList<>();

    private enum MapActions {
        GetPhoto,
        OpenCamera
    }

    private static final Map<MapActions, List<String>> permissions = new HashMap<MapActions, List<String>>() {{
        put(MapActions.OpenCamera, new ArrayList<String>(Arrays.asList(Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_MEDIA_LOCATION)));
        put(MapActions.GetPhoto, new ArrayList<String>(Arrays.asList(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_MEDIA_LOCATION
        )));
    }};

    private boolean checkPermissions(MapActions mapActions) {
        noGetPermissions.clear();
        List<String> getPermissions = permissions.get(mapActions);
        for (String permissionName : getPermissions) {
            if (ContextCompat.checkSelfPermission(this, permissionName)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED)
                noGetPermissions.add(permissionName);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
            noGetPermissions.remove(Manifest.permission.ACCESS_MEDIA_LOCATION);
        if (noGetPermissions.size() > 0) {
            checkMandatoryPermissions(noGetPermissions, mapActions);
            return false;
        }
        return true;
    }

    private void checkMandatoryPermissions(List<String> permissions, MapActions mapActions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions.toArray(new String[0]), getPermissionRequestCode(mapActions));
        } else {
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (mapActions == MapActions.GetPhoto)
                    checkPermissionAndGetMapContent();
                else checkPermissionAndOpenCamera();
            });
        }
    }

    private int getPermissionRequestCode(MapActions mapActions) {
        switch (mapActions) {
            case OpenCamera:
                return CAMERA_PERMISSIONS_CODE;

            case GetPhoto:
                return MAP_CONTENT_PERMISSIONS_CODE;

            default:
                return -1;
        }
    }

    private Map<PhotoInfo, Bitmap> photoInfoBitmapMap = new HashMap<>();
    private Map<Marker, PhotoInfo> markerToPhotoInfoMap = new HashMap<>();

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);


        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final Button button1 = findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionAndOpenCamera();
            }
        });

        mapFragment = (SupportMapFragment) getSupportFragmentManager().
                findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSIONS_CODE && grantResults.length > 0) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == android.content.pm.PackageManager.PERMISSION_GRANTED)
                    noGetPermissions.remove(permissions[i]);
            }
            if (noGetPermissions.size() > 0) {
                showOpenCameraPermissionDescriptionDialog();
            } else checkPermissionAndOpenCamera();
        }
        if (requestCode == MAP_CONTENT_PERMISSIONS_CODE && grantResults.length > 0) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == android.content.pm.PackageManager.PERMISSION_GRANTED)
                    noGetPermissions.remove(permissions[i]);
            }
            if (noGetPermissions.size() > 0) {
                showMapContentPermissionDescriptionDialog();
            } else checkPermissionAndGetMapContent();
        }
    }

    ChooseAndTakePhoto chooseAndTakePhoto = new ChooseAndTakePhoto(this);

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.setOnMarkerClickListener(this);

        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

            @Override
            public void onCameraChange(@NonNull CameraPosition cameraPosition) {
                if (cameraPosition.zoom != currentZoom) {
                    currentZoom = cameraPosition.zoom;
                    addPhotoOnMap(currentZoom);
                }
            }
        });
        dialog = new Dialog(MapActivity.this);
        dialog.setContentView(R.layout.preview_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        Button btnCon = dialog.findViewById(R.id.btn_continue);
        btnCon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                LatLng startPoint = new LatLng(56.325084, 43.965249);
                map.addMarker(new MarkerOptions().position(startPoint).title("StartPoint"));
                map.moveCamera(CameraUpdateFactory.newLatLng(startPoint));
                checkPermissionAndGetMapContent();

            }
        });
        dialog.show();
    }

    private void checkPermissionAndGetMapContent() {
        if (checkPermissions(MapActions.GetPhoto)) {
            initPhotosInfoMap(chooseAndTakePhoto.getPhotosWithLocationsFromGallery());
        }
    }

    private void showMapContentPermissionDescriptionDialog() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.map_content_description_text)
                .setCancelable(true)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void checkPermissionAndOpenCamera() {
        if (checkPermissions(MapActions.OpenCamera)) {
            launchCamera();
        }
    }

    private void showOpenCameraPermissionDescriptionDialog() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.open_camera_description_text)
                .setCancelable(true)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    static final int REQUEST_IMAGE_CAPTURE = 1213;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            galleryAddPic();
        }
    }

    private void launchCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.harman.android.myprojectforinternship.provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            }
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    String currentPhotoPath;

    private File createImageFile() throws IOException {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp;
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath();

        return image;
    }

    private void initPhotosInfoMap(List<PhotoInfo> photos) {
        for (PhotoInfo photo : photos) {
            initPhotoInfoAndAddToTheMap(photo);
        }
    }

    private void initPhotoInfoAndAddToTheMap(PhotoInfo photo) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inScaled = false;
        Bitmap b = BitmapFactory.decodeFile(photo.path, bmOptions);
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, initWidth, initHeight, true);
        photoInfoBitmapMap.put(photo, smallMarker);
    }

    private void addPhotoOnMap(float zoom) {

        map.clear();
        markerToPhotoInfoMap.clear();
        for (PhotoInfo photo : photoInfoBitmapMap.keySet()) {
            try {
                Bitmap smallMarker = Bitmap.createScaledBitmap(photoInfoBitmapMap.get(photo), Math.round(defaultWidth * zoom), Math.round(defaultHeight * zoom), true);

                markerToPhotoInfoMap.put(map.addMarker(new MarkerOptions()

                        .position(new LatLng(photo.Latitude, photo.Longitude))
                        .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))), photo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        if (markerToPhotoInfoMap.containsKey(marker)) {
            showInfoDialogForPhoto(markerToPhotoInfoMap.get(marker));
        }
        return true;
    }

    private void hideKeyboard(View view) {
        if (view == null)
            view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.clearFocus();
        }
    }

    private void showInfoDialogForPhoto(PhotoInfo photo) {
        dialog = new Dialog(MapActivity.this);
        dialog.setContentView(R.layout.photo_info_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        ImageView imgView = dialog.findViewById(R.id.photo_preview);
        if (imgView != null) {
            imgView.setImageBitmap(photoInfoBitmapMap.get(photo));
        }
        TextView commentTextView = dialog.findViewById(R.id.photo_comments);

        ImageButton btnAddComment = dialog.findViewById(R.id.comments_button_check);
        ImageButton btnChangeComment = dialog.findViewById(R.id.comments_button_change);
        EditText addComments = dialog.findViewById(R.id.photo_comments_edit_text);

        btnChangeComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addComments.setVisibility(View.VISIBLE);
                commentTextView.setVisibility(View.INVISIBLE);
                btnChangeComment.setVisibility(View.INVISIBLE);
                btnAddComment.setVisibility(View.VISIBLE);
                addComments.setText(commentTextView.getText());
                hideKeyboard(commentTextView);
                addComments.requestFocus();

            }
        });
        btnAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!addComments.getText().toString().isEmpty()) {
                    addComments.setVisibility(View.INVISIBLE);
                    commentTextView.setVisibility(View.VISIBLE);
                    btnChangeComment.setVisibility(View.VISIBLE);
                    btnAddComment.setVisibility(View.INVISIBLE);
                    commentTextView.setText(addComments.getText());
                    hideKeyboard(addComments);
                    photo.comments = addComments.getText().toString();
                }
            }
        });


        Button btnClose = dialog.findViewById(R.id.close_button);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        if (commentTextView != null) {
            String text;
            text = photo.comments;
            if(text.isEmpty())
            {
                addComments.setVisibility(View.VISIBLE);
                commentTextView.setVisibility(View.INVISIBLE);
                btnChangeComment.setVisibility(View.INVISIBLE);
                btnAddComment.setVisibility(View.VISIBLE);
            }
            commentTextView.setText(text);
        }

        dialog.show();

    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        if (currentPhotoPath != null) {
            File f = new File(currentPhotoPath);
            Uri contentUri = Uri.fromFile(f);
            try {
                PhotoInfo newPhotoInfo = chooseAndTakePhoto.getPhotoInfoByURI(f.getAbsolutePath());
                initPhotoInfoAndAddToTheMap(newPhotoInfo);
                addPhotoOnMap(map.getCameraPosition().zoom);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);
        }
    }
}


