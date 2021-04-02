package com.example.nursingjournal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CameraPreviewActivity extends AppCompatActivity {
    CameraSurfaceView cameraView;
    FrameLayout previewFrame;
    ConstraintLayout constraintLayout;
    ImageButton btnEdit, btnMain, btnSecond;
    CircleImageView btnSwitch;
    Integer mainSrcId, secondarySrcId;
    ArrayList<Bitmap> registeredChildBitmaps =new ArrayList<Bitmap>();
    File file;
    List<RegisteredChild> regChildList;
    ImageAdapter adapter;
    LayoutInflater inflater;
    Display display;

    MyDBHelper myDBHelper;
    SQLiteDatabase database;

    int childId;
    Bitmap childProfile;
    static final int REQUEST_IMAGE_CAPTURE = 10;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_preview);
        previewFrame = (FrameLayout)findViewById(R.id.previewFrame);
        cameraView = (CameraSurfaceView)findViewById(R.id.cameraView);
//        if (cameraView.getParent() != null){
//            ((ViewGroup)cameraView.getParent()).removeView(cameraView);
//        }
        display = ((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
//        previewFrame.addView(cameraView);
        constraintLayout = (ConstraintLayout)findViewById(R.id.buttons);
        registeredChildBitmaps.add(RegisterActivity.profilePicBitmap);
        btnSwitch = (CircleImageView) findViewById(R.id.btnSwitch);
        btnEdit = (ImageButton)findViewById(R.id.btnEdit);
        btnMain = (ImageButton)findViewById(R.id.btnMain);
        btnSecond = (ImageButton)findViewById(R.id.btnSecond);
        //setting image to ImageButtons
        mainSrcId = R.drawable.ic_camera_black_24dp;
        //btnMain.setImageResource(mainSrcId);
        secondarySrcId = R.drawable.ic_fiber_manual_record_black_24dp;
        //btnSecondary.setImageResource(secondarySrcId);
        inflater = LayoutInflater.from(this);
        regChildList = new ArrayList<RegisteredChild>();
        myDBHelper = new MyDBHelper(this);
        Intent intent = getIntent();
        childId = intent.getIntExtra("childId",1);

        database = myDBHelper.getReadableDatabase();
        Cursor initialCursor;
        initialCursor = database.rawQuery("SELECT profilePicture FROM children WHERE id ="+ childId +";",null);
        initialCursor.moveToFirst();
        childProfile = getImageWithFilename(initialCursor.getString(0));
        btnSwitch.setImageBitmap(childProfile);

        btnSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mainSrcId == R.drawable.ic_camera_black_24dp){
                    //switch main and secondary ImageButtons' resource
                    mainSrcId = R.drawable.ic_fiber_manual_record_black_24dp;
                    btnMain.setImageResource(mainSrcId);
                    secondarySrcId = R.drawable.ic_camera_black_24dp;
                    btnSecond.setImageResource(secondarySrcId);
                } else if (mainSrcId == R.drawable.ic_fiber_manual_record_black_24dp){
                    //switch main and secondary ImageButtons' resource
                    mainSrcId = R.drawable.ic_camera_black_24dp;
                    btnMain.setImageResource(mainSrcId);
                    secondarySrcId = R.drawable.ic_fiber_manual_record_black_24dp;
                    btnSecond.setImageResource(secondarySrcId);
                } else {
                    showToast("Something is wrong in switch buttons");
                }
            }
        });
        btnMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mainSrcId == R.drawable.ic_camera_black_24dp){
                    takePicture();
                } else if(mainSrcId == R.drawable.ic_fiber_manual_record_black_24dp){
                    //TODO: record video
                } else {
                    showToast("Something is wrong in stating to record video");
                }
            }
        });

        btnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database = myDBHelper.getReadableDatabase();
                Cursor cursor;
                try {
                    cursor = database.rawQuery("SELECT profilePicture FROM children WHERE id = "+ childId +";", null);
                    cursor.moveToFirst();
                    while(cursor.moveToNext()){
                        regChildList.add(new RegisteredChild(cursor.getBlob(1)));
                    }
                    cursor.close();
                    database.close();
                } catch (Exception e){
                    showToast("등록된 자녀 정보 조회 실패");
                }
                adapter=  new ImageAdapter(getApplicationContext(), regChildList);

            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent diaryIntent = new Intent(getApplicationContext(),DiaryActivity.class);
                diaryIntent.putExtra("childid", childId);
                startActivity(diaryIntent);
            }
        });
    }
    public Bitmap getImageWithFilename(String filename){
        String filepathDCIM = Environment.getExternalStorageDirectory()+"/"+Environment.DIRECTORY_DCIM + "/";
        String filepathPictures = Environment.getExternalStorageDirectory() + "/"+Environment.DIRECTORY_PICTURES+"/";
        File[] filesDCIM = new File(filepathDCIM).listFiles();
        File[] filesPictures = new File(filepathPictures).listFiles();
        File thisFile = null;
        Bitmap imageBitmap = null;
        for (File file : filesDCIM){
            if (filename.equals(file.getName())){
                thisFile = file;
            }
        }
        for (File file: filesPictures){
            if (filename.equals(file.getName())){
                thisFile = file;
            }
        }
        if(thisFile != null) {
            imageBitmap = BitmapFactory.decodeFile(thisFile.getAbsolutePath());
            return imageBitmap;
        }
        return imageBitmap;
    }
    public void takePicture(){
        cameraView.capture(new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                try {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    String outUriStr = MediaStore.Images.Media.insertImage( getContentResolver(), bitmap, "Captured Image", "Captured Image using Camera.");

                    if(outUriStr == null){
                        Log.d("SampleCapture","Image insert failed.");
                        return;
                    } else {
                        Uri outUri = Uri.parse(outUriStr);
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, outUri));
                    }
                    camera.startPreview();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }
    private File createFile(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());
        String filename = "capture"+timestamp+".jpg";


        File storageDir = Environment.getExternalStorageDirectory();
        File outFile = new File(storageDir, filename);

        return outFile;
    }
    public void getBitmapFromData(String filename){
        File storageDir = Environment.getExternalStorageDirectory();
        File outFile = new File(storageDir,filename);

    }

//    public void takePicture(){
//        if(file == null){
//            file = createFile();
//        }
//
//        try{Uri fileUri = FileProvider.getUriForFile(this, "com.example.nursingjournal.fileprovider",file);
//            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
//            if (intent.resolveActivity(getPackageManager()) != null){
//                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
//            }}
//        catch(Exception e){Toast.makeText(getApplicationContext(),"Error: "+e.getMessage(),Toast.LENGTH_SHORT).show();}
//
//    }
    public void showToast(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }

    public class RegisteredChild {
        private Bitmap bitmap;
        private ByteArrayInputStream byteInputStream;

        public RegisteredChild(byte[] blob){
            byteInputStream = new ByteArrayInputStream(blob);
            bitmap = BitmapFactory.decodeStream(byteInputStream);

        }

        public Bitmap getBitmap(){
            return bitmap;
        }

    }

    public class ImageAdapter extends BaseAdapter {
        Context context;
        LayoutInflater layoutInflater;
        List<RegisteredChild> dataList;
        RegisterActivity regAct;

        public ImageAdapter(Context c, List<RegisteredChild> data){
            context = c;
            dataList = data;
            layoutInflater = LayoutInflater.from(context);
            regAct = new RegisterActivity();
        }

        @Override
        public int getCount() { return dataList.size();}

        @Override
        public Object getItem(int position) {
            return dataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view= convertView;
            if (view == null){
                view = layoutInflater.inflate(R.layout.child,null);
            }
            CircleImageView childImage = (CircleImageView)findViewById(R.id.childImage);
            RegisteredChild regChild = dataList.get(position);
            childImage.setImageBitmap(regChild.getBitmap());

            return view;
        }
    }


}
