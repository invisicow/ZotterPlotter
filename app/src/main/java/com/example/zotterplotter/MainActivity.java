package com.example.zotterplotter;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPSClient;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
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

        // Start a new thread for the SFTP
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {

                    // Upload instructions via FTP
                    sendFileToRaspi();

                } catch (Exception e) {
                    Log.e("NewThread", e.getMessage());
                }
            }
        });
        thread.start();
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

        Channel channel = null;
        Session session = null;

        try {

            // Connect to local host
            /* HOST IP MAY CHANGE ABRUPTLY. FOR ERRORS, USE IFCONFIG TO CHECK */
            JSch ssh = new JSch();
            session = ssh.getSession("pi", "192.168.50.175", 22);
            session.setPassword("151200");

            // Disable known hosts list before connecting to session
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            // Establish SFTP session
            session.connect();
            channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftp = (ChannelSftp) channel;

            // Set local Android directory
            File dir = Environment.getExternalStorageDirectory();
            String path2 = dir.getAbsolutePath();
            path2 = path2 + "/Download/Peter.jpg";

            // Set remote Raspberry Pi directory
            sftp.put(path2, "/home/pi/Downloads/");

        } catch (JSchException e) {
            e.printStackTrace();
        } catch (SftpException e) {
            e.printStackTrace();
        } finally {

            // Terminate SFTP session
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
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
