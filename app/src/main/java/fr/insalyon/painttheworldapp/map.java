package fr.insalyon.painttheworldapp;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.core.content.FileProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import fr.insalyon.painttheworldapp.util.PermissionUtils;

public class map extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private File ext;
    private TextView email, prenom;
    private ImageView head;
    private Uri uriSavedImage;
    private File file, image;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ext = Environment.getExternalStorageDirectory();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.drawer_first, R.id.drawer_second, R.id.drawer_third)
                .setDrawerLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        View header = navigationView.getHeaderView(0);
        email = header.findViewById(R.id.email_nav);
        prenom = header.findViewById(R.id.prenom_nav);
        head = header.findViewById(R.id.imageView);
        image = new File(ext, "head.jpeg");
        uriSavedImage = FileProvider.getUriForFile(this,
                this.getPackageName() + ".provider", image);
        PermissionUtils.verify(this);
        file = new File(ext,"user.txt");
        loadinfo(file, image, uriSavedImage, email, prenom, head);
    }

    public static void loadinfo(File file,File image, Uri uri, TextView email, TextView prenom, ImageView head) {
        head.setImageResource(R.drawable.ic_launcher_foreground);
        try {
            if(!image.exists()) {
                FileReader reader = new FileReader(file);
                BufferedReader br = new BufferedReader(reader);
                String text = br.readLine();
                String[] arr = text.split("#");
                prenom.setText(arr[2] + arr[3]);
                head.setImageResource(R.drawable.ic_launcher_foreground);
                email.setText(arr[0]);
                br.close();
            }else{
                FileReader reader = new FileReader(file);
                BufferedReader br = new BufferedReader(reader);
                String text = br.readLine();
                String[] arr = text.split("#");
                prenom.setText(arr[2] + arr[3]);
                head.setImageURI(uri);
                email.setText(arr[0]);
                br.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


}
