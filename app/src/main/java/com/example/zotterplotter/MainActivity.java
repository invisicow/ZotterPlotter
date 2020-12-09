package com.example.zotterplotter;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;

public class MainActivity extends AppCompatActivity {

    // Selected image as drawable and bitmap
    Drawable imageDrawable;
    Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // !!! MIGHT HAVE TO BE content_main.xml

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        // Set button click listeners
        Button selectBtn = findViewById(R.id.select_button);
        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage(view);
            }
        });

        Button startBtn = findViewById(R.id.start_button);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPrint(view);
            }
        });

        Button stopBtn = findViewById(R.id.stop_button);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopPrint(view);
            }
        });
    }

    // Select image to print
    private void selectImage(View view){
        selectImage();
    }

    // Start printing
    private void startPrint(View view){

        // Upload instructions via FTP
        sendFileToRaspi();
    }

    // Stop printing
    private void stopPrint(View view){
        // TODO: Create method
    }

    int CHOSEN_IMAGE_CODE = 0;

    // Creates intent for gallery
    private void selectImage() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, CHOSEN_IMAGE_CODE);
    }

    // Handle chosen image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode == RESULT_OK && requestCode == CHOSEN_IMAGE_CODE){

            // Get ImageView of selected image
            ImageView selectedImageView = new ImageView(this);
            selectedImageView.setImageURI(data.getData());

            // Get Drawable from ImageView
            imageDrawable = selectedImageView.getDrawable();

            // Get Bitmap from Drawable
            imageBitmap = ((BitmapDrawable) selectedImageView.getDrawable()).getBitmap();

            // Set ImageView source as image bitmap
            ImageView imagePreview = findViewById(R.id.image_preview);
            imagePreview.setImageBitmap(imageBitmap);
        }
    }

    // Upload file to Raspberry Pi via FTP
    private void sendFileToRaspi(){

        try{

            FTPClient ftp = new FTPClient();

            ftp.connect("192.168.###:port");

            if (ftp.login("pi", "151200")){

                // Change file type from ASCII to Binary
                ftp.setFileType(FTP.BINARY_FILE_TYPE);

                // Open data transfers
                ftp.enterLocalPassiveMode();

                // Define directory of local file
                FileInputStream inputStream = new FileInputStream(new File("/storage/emulated/0/Download/billclinton.png"));

                // Define remote directory to store file
                boolean returnCode = ftp.storeFile("PATH/billclinton.png", inputStream);

                inputStream.close();

                if(ftp.isConnected()){
                    ftp.logout();
                    ftp.disconnect();
                }

            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    // Toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // Toolbar menu options
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
