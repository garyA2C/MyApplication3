package fr.insalyon.painttheworldapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.insalyon.painttheworldapp.util.HttpUtils;
import fr.insalyon.painttheworldapp.util.PermissionUtils;

public class Inscription extends AppCompatActivity {
    private EditText Email,Password,Nom,Prenom,Username;
    private Button Inscription;
    File ext = Environment.getExternalStorageDirectory();

    private static String baseURL = "https://paint.antoine-rcbs.ovh/inscription";
    public Inscription(){}
    private String email;
    private String password;
    private String nom;
    private String prenom;
    private String username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inscription);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }


        Email = findViewById(R.id.email);
        Password = findViewById(R.id.password);
        Nom = findViewById(R.id.prenom);
        Prenom = findViewById(R.id.nom);
        Inscription = findViewById(R.id.inscription);
        Username  =findViewById(R.id.username);

        PermissionUtils.verify(Inscription.this);
        System.out.println("111111111111111111111111");


        Inscription.setOnClickListener(new View.OnClickListener() {


            public void onClick(View v) {
                try {
                    email = Email.getText().toString().trim();
                    password = Password.getText().toString().trim();
                    nom = Nom.getText().toString().trim();
                    prenom = Prenom.getText().toString().trim();
                    username = Username.getText().toString().trim();
                    System.out.println("123");

                    File file = new File(ext, "user.txt");

                    OutputStream out = new FileOutputStream(file);
                    OutputStreamWriter osw = new OutputStreamWriter(out, StandardCharsets.UTF_8);
                    BufferedWriter writer = new BufferedWriter(osw);
                    writer.write(email + "#" + password + "#" + nom + "#" + prenom +
                            "#" + username);
                    writer.flush();
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(TextUtils.isEmpty(username)||TextUtils.isEmpty(nom)||TextUtils.isEmpty(prenom)||TextUtils.isEmpty(password)||TextUtils.isEmpty(email)){
                    Toast.makeText(Inscription.this, "Il faut tout remplir", Toast.LENGTH_SHORT).show();
                }else if (!isEmail(Email.getText().toString())) {
                    Toast.makeText(Inscription.this, "Votre email est non correct.", Toast.LENGTH_SHORT).show();
                }else if (!isPassword(Password.getText().toString())) {
                    Toast.makeText(Inscription.this, "Votre password < 5 bytes", Toast.LENGTH_SHORT).show();
                } else {
                    Map<String, String> data =  new HashMap<String, String>();
                    Map<String,String> params = new HashMap<String, String>();
                    params.put("name", nom);
                    params.put("p_nom", prenom);
                    params.put("email", email);
                    params.put("username", username);
                    params.put("password1", password);
                    params.put("password2", password);

                    String strResult=HttpUtils.submitPostData(baseURL,params, "utf-8");

                    Intent intent = new Intent();
                    intent.setClass(Inscription.this, map.class);
                    startActivity(intent);
                }
            }
        });
    }

    public boolean isPassword(String password){
        if(password.length()<5){
            return false;
        }else{
            return true;
        }
    }

    public boolean isEmail(String email) {
        String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(email);
        return m.matches();
    }
}
