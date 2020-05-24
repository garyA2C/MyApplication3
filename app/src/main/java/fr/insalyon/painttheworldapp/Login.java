package fr.insalyon.painttheworldapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.insalyon.painttheworldapp.util.HttpUtils;
import fr.insalyon.painttheworldapp.util.PermissionUtils;

public class Login extends AppCompatActivity {

    private Button Button_confirmer;
    private Button Button_inscription;
    private EditText Username;
    private EditText Password;
    private CheckBox Rembme;
    File ext = Environment.getExternalStorageDirectory();
    private static String baseURL = "https://paint.antoine-rcbs.ovh/login";
    private File file;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button_confirmer = (Button) findViewById(R.id.confirmer);
        Button_inscription = (Button) findViewById(R.id.inscription);
        Username = findViewById(R.id.email);
        Password = findViewById(R.id.password);
        Rembme = findViewById(R.id.rembme);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        loadinfo();



        Button_confirmer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = Username.getText().toString().trim();
                String password = Password.getText().toString().trim();
                Map<String, String> params =  new HashMap<String, String>();

                params.put("username", username);
                params.put("password", password);

                //String strResult= HttpUtils.submitPostData(baseURL,params, "utf-8");
                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                    Toast.makeText(Login.this, "Mot de passe ou email est vide", Toast.LENGTH_SHORT).show();
                    //return;
                } else if (!Rembme.isChecked()) {
                    file = new File(ext, "user.txt");
                }
//                if(!isEmail(Email.getText().toString())) {
//                    Toast.makeText(Login.this, "Votre email est non correct.", Toast.LENGTH_SHORT).show();
                 if(!isPassword(Password.getText().toString())){
                    Toast.makeText(Login.this, "Votre password < 5 bytes",Toast.LENGTH_SHORT).show();
                }else {
                    Intent intent = new Intent();
                    intent.setClass(Login.this, map.class);
                    startActivity(intent);
                }
            }
        });


        Button_inscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent intent = new Intent();
                intent.setClass(Login.this, Inscription.class);

                startActivity(intent);
            }
        });

    }

    private void loadinfo(){
        File file = new File(ext,"user.txt");
        if(!file.exists()){
            return;
        }
        try{
            FileReader reader = new FileReader(file);
            BufferedReader br = new BufferedReader(reader);
            String text = br.readLine();
            String[] arr = text.split("#");
            Username.setText(arr[4]);
            Password.setText(arr[1]);
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public boolean isPassword(String password){
        if(password.length()<5){
            return false;
        }else{
            return true;
        }
    }


}
