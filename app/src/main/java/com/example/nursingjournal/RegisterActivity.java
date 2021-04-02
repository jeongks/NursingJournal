package com.example.nursingjournal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends AppCompatActivity implements AutoPermissionsListener {
    EditText birthDateInput, birthTimeInput, birthWeightInput, birthHeightInput, fetusNameInput, bloodTypeInput, hospitalNameInput,
            actualNameInput, ageInput, weightInput, heightInput ;
    Button btnSaveBirthInfo, btnSaveCurrentInfo, btnRegister;
    CircleImageView babyProfilePic;
    File file;
    static Bitmap profilePicBitmap;

    static final int REQUEST_IMAGE_CAPTURE = 10;
    static final int RESULT_LOAD_IMAGE = 11;

    MyDBHelper myDBHelper;
    SQLiteDatabase database;
    String filename = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //connect EditTexts to this activity
        birthDateInput = (EditText)findViewById(R.id.birthDateInput);
        birthTimeInput = (EditText)findViewById(R.id.birthTimeInput);
        birthWeightInput = (EditText)findViewById(R.id.birthWeightInput);
        birthHeightInput = (EditText)findViewById(R.id.birthHeightInput);
        fetusNameInput = (EditText)findViewById(R.id.fetusNameInput);
        bloodTypeInput = (EditText)findViewById(R.id.bloodTypeInput);
        hospitalNameInput = (EditText)findViewById(R.id.hospitalNameInput);
        actualNameInput = (EditText)findViewById(R.id.actualNameInput);
        ageInput = (EditText)findViewById(R.id.ageInput);
        weightInput = (EditText)findViewById(R.id.weightInput);
        heightInput = (EditText)findViewById(R.id.heightInput);

        //connect ImageView to this activity
        babyProfilePic = (CircleImageView) findViewById(R.id.babyProfilePic);

        //connect buttons to this activity
        btnSaveBirthInfo = (Button)findViewById(R.id.btnSaveBirthInfo);
        btnSaveCurrentInfo = (Button)findViewById(R.id.btnSaveCurrentInfo);
        btnRegister = (Button)findViewById(R.id.btnRegister);
        AutoPermissions.Companion.loadAllPermissions(this,REQUEST_IMAGE_CAPTURE);
        dbCreate();



        //출생 정보 저장 버튼
        btnSaveBirthInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check required input information is filled and enable next input field if all required information is submitted.
                if(birthInputValidate()){
                    showToast("출생정보 임시 저장 완료");
                } else {
                    showToast("출생정보 저장이 실패했습니다.");
                }
//                database = myDBHelper.getWritableDatabase();
//                database.execSQL("insert into childrenTBL values(20,'홍길동');");
//                database.close();
//                showToast("저장 성공");
            }
        });

        babyProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                builder.setTitle("자녀 프로필 사진 등록하기");
                builder.setPositiveButton("사진 가져오기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //get image from the gallery
                        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                        photoPickerIntent.setType("image/*");
                        startActivityForResult(photoPickerIntent,RESULT_LOAD_IMAGE);
                    }
                });
                builder.setNegativeButton("사진 촬영하기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try{
                            takePicture();
                        } catch (ActivityNotFoundException e){
                            Toast.makeText(getApplicationContext(),"사진촬영 기능 접근 불가",Toast.LENGTH_SHORT).show();
                        } catch (Exception e){
                            Toast.makeText(getApplicationContext(),"에러발생: "+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                AlertDialog dialog =builder.create();
                dialog.show();
            }
        });

        //현재 정보 저장 버튼
        btnSaveCurrentInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if all required data is input.
                //save information to database

                if(currentInputValidate()){
                    showToast("현재 정보 임시 저장 완료");
                } else {
                    showToast("현재 정보의 자녀의 본명을 입력해주세요.");
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //save image to the database
//                Bitmap storeBitmap = profilePicBitmap;
//                ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                storeBitmap.compress(Bitmap.CompressFormat.JPEG,100,bos);
//                byte[] img = bos.toByteArray();
//                ContentValues values = new ContentValues();
//                values.put("image",img);
//                Log.i("test",""+values.get("image"));
                database = myDBHelper.getWritableDatabase();
                String fetusName = fetusNameInput.getText().toString(),
                        birthTime = birthTimeInput.getText().toString(),
                        hospitalName = hospitalNameInput.getText().toString(),
                        birthWeight = birthWeightInput.getText().toString(),
                        weight = weightInput.getText().toString(),
                        birthHeight = birthHeightInput.getText().toString(),
                        height = heightInput.getText().toString();
                if(fetusName.equals("")|| fetusName == null){ fetusName = null; }
                if(birthTime.equals("") || birthTime == null){ birthTime = null; }
                if(birthWeight.equals("")||birthWeight == null){ birthWeight = "null"; }
                if(weight.equals("")|| weight ==null){ weight = "null"; }
                if(birthHeight.equals("")|| birthHeight ==null){ birthHeight = "null"; }
                if(height.equals("") || height == null){ height = "null"; }
                if(hospitalName.equals("") || hospitalName == null){ hospitalName ="null"; }
                //in query statement, instead of using getText().toString() use variables.
                database.execSQL("insert into children (name,fetusName,birthdate,birthtime,birthWeight,currentWeight,birthHeight,currentHeight,bloodType,bornHospital,profilePicture) values ('"
                        + actualNameInput.getText().toString()+"','"+ fetusName+"','" + birthDateInput.getText().toString() + "','"+ birthTime + "',"
                        + birthWeight + ","+ weight +"," + birthHeight +","+height+ ",'"
                        + bloodTypeInput.getText().toString() + "','" + hospitalName + "','"+filename+"');");
                database.close();
                showToast("저장 성공");
                Cursor cursor;
                database = myDBHelper.getReadableDatabase();
                cursor = database.rawQuery("SELECT id FROM children WHERE name = '"+actualNameInput.getText().toString()+"' AND birthdate = '"+birthDateInput.getText().toString()+"' AND bloodType='"+bloodTypeInput.getText().toString()+"';",null);
                cursor.moveToFirst();
                int childid = 0;
                while(cursor.moveToNext()){
                    childid = cursor.getInt(0);
                }
                cursor.close();
                database.close();
                Intent outintent = new Intent(getApplicationContext(),MainActivity.class);
                outintent.putExtra("Visit",true);
                outintent.putExtra("childId",childid);
                setResult(RESULT_OK, outintent);
                finish();
            }
        });
    }
    void dbCreate() {
        myDBHelper = new MyDBHelper(this);
    }

    boolean birthInputValidate(){
        if ( birthDateInput.getText().toString().trim().length() == 0 ||  bloodTypeInput.getText().toString().trim().length() == 0) {
            Toast.makeText(getApplicationContext(),"필수 입력란에 정보를 입력해주세요.",Toast.LENGTH_SHORT).show();
            return false;
        } else {
            actualNameInput.setEnabled(true);
            ageInput.setEnabled(true);
            weightInput.setEnabled(true);
            heightInput.setEnabled(true);
            btnSaveCurrentInfo.setEnabled(true);
            return true;
        }
    }

    boolean currentInputValidate(){
        if(actualNameInput.getText().toString().trim().length() == 0){
            Toast.makeText(getApplicationContext(),"필수 입력란에 정보를 입력해주세요.",Toast.LENGTH_SHORT).show();
            return false;
        } else {
            btnRegister.setEnabled(true);
            return true;
        }
    }

    public void takePicture(){
        if(file == null){
            file = createFile();
        }
        Uri fileUri = FileProvider.getUriForFile(this,"com.example.nursingjournal.fileprovider",file);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        if( cameraIntent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(cameraIntent,REQUEST_IMAGE_CAPTURE);
        }
    }

    private File createFile(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());
        filename = "capture"+timestamp+".jpg";

        File storageDir = Environment.getExternalStorageDirectory();
        File outFile = new File(storageDir, filename);

        return outFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //my way
            //profilePicBitmap = (Bitmap) data.getExtras().get("data");
            //babyProfilePic.setImageBitmap(profilePicBitmap);
            //book way
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;
            profilePicBitmap = BitmapFactory.decodeFile(file.getAbsolutePath(),options);
            babyProfilePic.setImageBitmap(profilePicBitmap);
        }
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK){
            try {
                final Uri imageUri = data.getData();
                String[] filepathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(imageUri, filepathColumn, null,null, null);
                cursor.moveToFirst();
                int index = cursor.getColumnIndex(filepathColumn[0]);
                String filepath = cursor.getString(index);
                cursor.close();
                File f = new File(filepath);
                filename = f.getName();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                profilePicBitmap = BitmapFactory.decodeStream(imageStream);
                babyProfilePic.setImageBitmap(profilePicBitmap);
            } catch (IOException e){
                e.printStackTrace();
                showToast("사진을 가져오는데 실패했습니다.");
            }
        } else {
            showToast("사진을 선택하지 않았습니다.");
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AutoPermissions.Companion.parsePermissions(this, requestCode, permissions, this);
    }

    @Override
    public void onDenied(int i, String[] strings) {

    }

    @Override
    public void onGranted(int i, String[] strings) {

    }

    void showToast(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }
}
