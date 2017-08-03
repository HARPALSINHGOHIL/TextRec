package com.example.harpal5inh.textrec;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;


public class MainActivity extends AppCompatActivity {


    private static final int CAMERA_PIC_REQUEST = 22;
    private String TAG = "OCR";
    private LinearLayout linearLayout_Vertical;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        linearLayout_Vertical=(LinearLayout)findViewById(R.id.linearLayout_Vertical);
    }

    public void imageclicked(View v)
    {
//        imgView.setVisibility(View.INVISIBLE);
    }

    public void buttonclicked(View v) {
        //linearLayout_Vertical.removeAllViews();
        EasyImage.openChooserWithGallery(MainActivity.this,"Choose from",1);
        EasyImage.configuration(this).setImagesFolderName("My app images").setAllowMultiplePickInGallery(false);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                Log.d("Error",e.getMessage());
                Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_LONG);
            }

            @Override
            public void onImagesPicked(List<File> imagesFiles, EasyImage.ImageSource source, int type) {

                Bitmap bitmap=null;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                ExifInterface ei = null;
                try {
                    ei = new ExifInterface(imagesFiles.get(0).getAbsolutePath());
                    bitmap = BitmapFactory.decodeStream(new FileInputStream(imagesFiles.get(0)), null, options);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("ImageRotation","Error in ImageRotation");
                }
                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);

                switch(orientation) {

                    case ExifInterface.ORIENTATION_ROTATE_90:
                        bitmap=rotateImage(bitmap, 90);
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_180:
                        bitmap=rotateImage(bitmap, 180);
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_270:
                        bitmap=rotateImage(bitmap, 270);
                        break;

                    case ExifInterface.ORIENTATION_NORMAL:

                    default:
                        break;
                }
                handllimage(bitmap);

            }
        });
    }

    public void handllimage(Bitmap bitmap)
    {

       //Setting whole image
        // imgView.setImageBitmap(bitmap);
        if (bitmap != null) {
            TextRecognizer textRecognizer = new TextRecognizer.Builder(this).build();
            if (!textRecognizer.isOperational()) {
                Log.w(TAG, "Detector dependencies are not yet available.");
                IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
                boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;
                if (hasLowStorage) {
                    Toast.makeText(this, "Low Storage", Toast.LENGTH_LONG).show();
                    Log.w(TAG, "Low Storage");
                }
            }
            //Frame Builder for Extraction
            Frame imageFrame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> textBlocks = textRecognizer.detect(imageFrame);
            String fina = "";
            Bitmap workingFrame=imageFrame.getBitmap();
            for (int i = 0; i < textBlocks.size(); i++) {

                TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));
                List<Text> temp= (List<Text>) textBlock.getComponents();
                for(int j=0;j<temp.size();j++)
                {
                    Text tt=temp.get(j);
                    TextView textView_Temp=new TextView(this);
                    textView_Temp.setText(tt.getValue());
                    ImageView imageView_Temp=new ImageView(this);
                    Rect tem=tt.getBoundingBox();
                    imageView_Temp.setImageBitmap(Bitmap.createBitmap(workingFrame,tem.left,tem.top,tem.width(),tem.height()));
//

                    //imageView_Temp.setPadding(10,10,10,10);
                    linearLayout_Vertical.addView(textView_Temp);
                    linearLayout_Vertical.addView(imageView_Temp);
                    //String text = tt.getValue();
                //fina += text + "\t\t" + tt.getBoundingBox().flattenToString() + "\n\n";
                }
//                String text = textBlock.getValue();
//                fina += text + "\t\t" + textBlock.getBoundingBox().flattenToString() + "\n\n";

            }
           // TextView tv=(TextView)findViewById(R.id.textView);
          //  tv.setText(fina);
        }
    }
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return  source;
       // return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
       //         matrix, true);
    }
}
