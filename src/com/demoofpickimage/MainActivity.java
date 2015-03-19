package com.demoofpickimage;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends Activity {

    private Uri UrlGambar;
    private ImageView SetImageView;

    private static final int CAMERA = 1;
    private static final int FILE = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final String[] pilih = new String[]{"Camera", "SD Card"};
        ArrayAdapter<String> arr_adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, pilih);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Pilih Gambar");
        builder.setAdapter(arr_adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int pilihan) {
                if (pilihan == 0) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File file = new File(Environment.getExternalStorageDirectory(), "image_picker/img_"
                            + String.valueOf(System.currentTimeMillis()) + ".jpg");
                    UrlGambar = Uri.fromFile(file);
                    try {
                        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, UrlGambar);
                        intent.putExtra("return-data", true);
                        startActivityForResult(intent, CAMERA);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    dialog.cancel();
                } else if (pilihan == 1) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Pilih Aplikasi"), FILE);
                }
            }
        });

        final AlertDialog dialog = builder.create();
        SetImageView = (ImageView) findViewById(R.id.img_set);
        Button tmb_pilih = (Button) findViewById(R.id.btn_pilih);
        tmb_pilih.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return;
        Bitmap bitmap = null;
        String path = "";

        if (requestCode == FILE) {
            UrlGambar = data.getData();
            path = getRealPath(UrlGambar);
            if (path == null) {
                path = UrlGambar.getPath();
            } else {
                bitmap = BitmapFactory.decodeFile(path);
            }
        } else {
            path = UrlGambar.getPath();
            bitmap = BitmapFactory.decodeFile(path);
        }
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int size = getBitmapSize(bitmap);
        Log.e("e", "size is " + size);
        if (size > 1024 * 1024 * 2)//当图片大小大于2M时，对图片进行压缩处理，以防止OOM异常
            bitmap = decodeSampledBitmapFromResource(path, displayMetrics.widthPixels, displayMetrics.heightPixels);
        Toast.makeText(this, path, Toast.LENGTH_LONG).show();
        SetImageView.setImageBitmap(bitmap);
    }

    public String getRealPath(Uri contentUri) {
        String path = null;
        String[] images_data = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, images_data, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            path = cursor.getString(column_index);
        }
        cursor.close();
        return path;
    }

    public int getBitmapSize(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= 19) {
            return bitmap.getAllocationByteCount();
        } else if (Build.VERSION.SDK_INT >= 12) {
            return bitmap.getByteCount();
        }
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    public Bitmap decodeSampledBitmapFromResource(String filePath, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }
}