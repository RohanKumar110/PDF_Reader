package com.rk.pdfreader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    File directory;
    ListView listView;
    CustomAdapter customAdapter;
    public static List<FileModel> modelList;
    public final static int PERMISSION_CODE = 123;
    public final static String READ_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE;
    public final static  String WRITE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public boolean isPermissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        listView = findViewById(R.id.listview_pdf);
        directory = new File(Environment.getExternalStorageDirectory().toString());
        modelList = new ArrayList<>();

        checkPermission();

        if(isPermissionGranted)
            setCustomAdapter();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this,ViewPDF.class);
                intent.putExtra("position",position);
                startActivity(intent);
            }
        });

    }

    private void checkPermission() {

        if(ContextCompat.checkSelfPermission(getApplicationContext(),READ_PERMISSION) == PackageManager.PERMISSION_GRANTED &&
           ContextCompat.checkSelfPermission(getApplicationContext(),WRITE_PERMISSION) == PackageManager.PERMISSION_GRANTED){
            isPermissionGranted = true;
        }
        else{
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{READ_PERMISSION,WRITE_PERMISSION},PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case PERMISSION_CODE:
                if(hasAllPermissionsGranted(grantResults)){
                    isPermissionGranted = true;
                    setCustomAdapter();
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Please Allow the Permission.",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean hasAllPermissionsGranted(int[] grantResults) {

        for(int grantResult:grantResults){
            if(grantResult == PackageManager.PERMISSION_DENIED)
                return false;
        }
        return true;
    }

    public void setCustomAdapter()
    {
        readFiles();
        customAdapter = new CustomAdapter(getApplicationContext(),modelList);
        listView.setAdapter(customAdapter);
    }

    private void readFiles() {
        Uri uri = MediaStore.Files.getContentUri("external");
        String[] projection = {MediaStore.Files.FileColumns.TITLE,MediaStore.Files.FileColumns.DATA};
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf");
        String selectionMimetype = MediaStore.Files.FileColumns.MIME_TYPE + "=?";
        String[] selectionArgs = {mimeType};
        Cursor cursor = getContentResolver().query(uri,projection,selectionMimetype,selectionArgs,null);
        while (cursor.moveToNext()){

            FileModel model = new FileModel(cursor.getString(0),new File(cursor.getString(1)));
            modelList.add(model);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.custom_menu,menu);
        MenuItem  menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Type Here to Search");
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)){
                    customAdapter.getFilter().filter(null);
                }
                else {
                    customAdapter.getFilter().filter(newText);
                    customAdapter.notifyDataSetChanged();
                }
                return true;
            }
        });
        return true;
    }
}