package com.example.nursingjournal;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.ActivityNotFoundException;
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
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DiaryActivity extends AppCompatActivity {
    //diary contents
    EditText editDate, editTitle, editAge, editName, editPottyCount, editPottyStatus, editSignificant, editHealth, editDiaryContent;
    TextView ageUnit, textBirthday, fetusName;
    ImageView diaryPic;
    Button btnCancel, btnSave;
    //date picker
    DatePicker datePicker;
    int cYear, cMonth, cDay;
    String date;
    //database access
    MyDBHelper myDBHelper;
    SQLiteDatabase database;
    //age unit
    String[] ageUnits = {"개월","일","살"};
    int index = 0;

    //date difference calculating element
    SimpleDateFormat format;

    //store id for children table in diaryDB
    int childId;

    //takepicture request code
    File file;
    static final int RESULT_LOAD_IMAGE = 101;
    static final int REQUEST_IMAGE_CAPTURE = 102;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);
        editDate = (EditText)findViewById(R.id.editDate);
        editTitle = (EditText)findViewById(R.id.editTitle);
        editAge = (EditText)findViewById(R.id.editAge);
        editName = (EditText)findViewById(R.id.editName);
        editPottyCount = (EditText)findViewById(R.id.editPottyCount);
        editPottyStatus = (EditText)findViewById(R.id.editPottyStatus);
        editSignificant = (EditText)findViewById(R.id.editSignificant);
        editHealth = (EditText)findViewById(R.id.editHealth);
        editDiaryContent = (EditText)findViewById(R.id.editDiaryContent);
        ageUnit = (TextView)findViewById(R.id.ageUnit);
        textBirthday = (TextView)findViewById(R.id.textBirthday);
        fetusName = (TextView)findViewById(R.id.fetusName);
        diaryPic = (ImageView)findViewById(R.id.diaryPic);
        datePicker = (DatePicker)findViewById(R.id.datePicker);
        btnCancel = (Button)findViewById(R.id.btnCancel);
        btnSave = (Button)findViewById(R.id.btnSave);

        Calendar calendar = Calendar.getInstance();
        cYear = calendar.get(Calendar.YEAR);
        cMonth = calendar.get(Calendar.MONTH);
        cDay = calendar.get(Calendar.DAY_OF_MONTH);
        format = new SimpleDateFormat("yyyy-MM-dd");

        Intent intent = getIntent();
        childId = intent.getIntExtra("childid",1);

        myDBHelper = new MyDBHelper(this);
        database = myDBHelper.getWritableDatabase();
        Cursor cursor;
        try {
            cursor = database.rawQuery("SELECT id,name,fetusName,birthdate FROM children WHERE id = "+childId+";",null);
            cursor.moveToFirst();
            while(cursor.moveToNext()){
                childId = cursor.getInt(0);
                editName.setText(cursor.getString(1));
                fetusName.setText(cursor.getString(2));
                textBirthday.setText(cursor.getString(3));

            }
            cursor.close();
            database.close();
        } catch (Exception e){
            showToast("등록된 자녀정보 불러오기 실패");
        }
        datePicker.init(cYear, cMonth, cDay, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                date = year+"-"+(monthOfYear-1)+"-"+dayOfMonth;
                showDiary(date);
            }
        });
        diaryPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DiaryActivity.this);
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

        ageUnit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (index == ageUnits.length){index = 0;}
                switch (ageUnits[index]){
                    case "개월":
                        editAge.setText(monthDiff(textBirthday.getText().toString(),date));
                        break;
                    case "일":
                        editAge.setText(dateDiff(textBirthday.getText().toString(),date));
                        break;
                    case "살":
                        editAge.setText(yearDiff(textBirthday.getText().toString(),date));
                        break;
                }
                index++;

            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database = myDBHelper.getWritableDatabase();
                if(btnSave.getText().toString().equals("저장")){
                    try {
                        database.execSQL("insert into diaries (date,diaryTitle,diaryContent,pottyCount,pottyStatus,significant,health) values( '"
                                + date + "','" + editTitle.getText().toString() + "','" + editDiaryContent.getText().toString() + "'," + editPottyCount.getText().toString() + ",'"
                                + editPottyStatus.getText().toString() + "','" + editSignificant.getText().toString() + "','" + editHealth.getText().toString() + "')");
                        database.close();
                        showToast("일기 저장 완료");
                    } catch (Exception e){
                        showToast("일기 저장 실패");
                    }
                } else {
                    try {
                        database.execSQL("update diaries set diaryTitle = '"+editTitle.getText().toString()+"',diaryContent = '"+editDiaryContent.getText().toString()+
                                         "',pottyCount="+editPottyCount.getText().toString()+", pottyStatus='"+editPottyStatus.getText().toString()+
                                         "',significant='"+editSignificant.getText().toString()+"',health='"+editHealth.getText().toString()+"'");
                        database.close();
                        showToast("일기 수정 완료");
                    } catch (Exception e){
                        showToast("일기 수정 실패");
                    }
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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
        String filename = "capture.jpg";

        File storageDir = Environment.getExternalStorageDirectory();
        File outFile = new File(storageDir, filename);

        return outFile;
    }
    void showToast(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
    public String dateDiff(String start, String end){
        String result = "";
        Date startDate, endDate;
        try {
            startDate = format.parse(start);
            endDate = format.parse(end);
            long difference = endDate.getTime() - startDate.getTime();

            long secondsInMilli = 1000;
            long minutesInMilli = secondsInMilli * 60;
            long hoursInMilli = minutesInMilli *60;
            long daysInMilli = hoursInMilli * 24;

            long diffDays = difference / daysInMilli;
            result = ""+diffDays;

        }catch (ParseException e){
            showToast(e.getMessage());
        }
        return result;
    }

    public String monthDiff(String start, String end){
        String result = "";
        Date startDate, endDate;
        try {
            startDate = format.parse(start);
            endDate = format.parse(end);
            long difference = endDate.getTime() - startDate.getTime();

            long secondsInMilli = 1000;
            long minutesInMilli = secondsInMilli * 60;
            long hoursInMilli = minutesInMilli *60;
            long daysInMilli = hoursInMilli * 24;
            long monthsInMilli = daysInMilli * 30;

            long diffDays = difference / monthsInMilli;
            result = ""+diffDays;

        }catch (ParseException e){
            showToast(e.getMessage());
        }
        return result;
    }
    public String yearDiff(String start, String end){
        String result = "";
        Date startDate, endDate;
        try {
            startDate = format.parse(start);
            endDate = format.parse(end);
            long difference = endDate.getTime() - startDate.getTime();

            long secondsInMilli = 1000;
            long minutesInMilli = secondsInMilli * 60;
            long hoursInMilli = minutesInMilli *60;
            long daysInMilli = hoursInMilli * 24;
            long monthsInMilli = daysInMilli * 30;
            long yearsInMilli = monthsInMilli * 12;

            long diffDays = difference / yearsInMilli;
            result = ""+diffDays;

        }catch (ParseException e){
            showToast(e.getMessage());
        }
        return result;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;
            Bitmap diaryBitmap = BitmapFactory.decodeFile(file.getAbsolutePath(),options);
            diaryPic.setImageBitmap(diaryBitmap);
        }
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK){
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap diaryBitmap = BitmapFactory.decodeStream(imageStream);
                diaryPic.setImageBitmap(diaryBitmap);
            } catch (IOException e){
                e.printStackTrace();
                showToast("사진을 가져오는데 실패했습니다.");
            }
        } else {
            showToast("사진을 선택하지 않았습니다.");
        }

    }
    public void showDiary(String date){
        database = myDBHelper.getReadableDatabase();
        Cursor cursor;
        try {
            cursor = database.rawQuery("SELECT date, diaryTitle, diaryContent, pottyCount, pottyStatus, significant, health FROM diaries WHERE date = '" + date + "'", null);
            if (cursor.moveToFirst()) {
                btnSave.setText("수정");
                editTitle.setText(cursor.getString(2));
                editDiaryContent.setText(cursor.getString(3));
            } else {
                btnSave.setText("저장");
                editTitle.setText("");
                editTitle.setHint("제목");
                editDiaryContent.setText("");
                editDiaryContent.setHint("일기 내용");
            }
            cursor.close();
            database.close();
        } catch (Exception e){
            showToast("일기 가져오기 실패");
        }
    }
}