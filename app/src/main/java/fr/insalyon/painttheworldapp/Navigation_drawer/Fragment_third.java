package fr.insalyon.painttheworldapp.Navigation_drawer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import fr.insalyon.painttheworldapp.R;

public class Fragment_third extends Fragment {
    private ListView list_view;
    private View root;
    private View header;
    private ImageView head;
    private static final int CODE_GALLERY_REQUEST = 0xa0;
    private static final int CODE_RESULT_REQUEST = 0xa2;
    private File imagesFolder;
    private Uri uriSavedImage;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        root= inflater.inflate(R.layout.fragment_third, container, false);
        header =  inflater.inflate(R.layout.nav_header_map, container, false);
        head = header.findViewById(R.id.imageView);
        list_view = root.findViewById(R.id.list_view);
        String[] arr = new String[]{"Photo","Contact us"};
        Adapter_list adaptateur = new Adapter_list(getContext(), arr);
        list_view.setAdapter(adaptateur);
        imagesFolder = new File(Environment.getExternalStorageDirectory(), "head.jpeg");
        uriSavedImage = FileProvider.getUriForFile(getContext(),
                root.getContext().getPackageName() + ".provider", imagesFolder);

        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        showTypeDialog();
                        break;
                    case 1:
                        if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE}, 1);
                        }else {
                            contact_us("0698819698");
                        }
                        break;
                }

            }
        });
        return root;
    }

    private void contact_us(String number){
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + number));
        startActivity(intent);
    }
    private void showTypeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final AlertDialog dialog = builder.create();
        View view = View.inflate(getContext(), R.layout.dialog_select_photo, null);
        TextView tv_select_gallery = (TextView) view.findViewById(R.id.tv_select_gallery);
        TextView tv_select_camera = (TextView) view.findViewById(R.id.ttv_select_camera);
        tv_select_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(Intent.ACTION_PICK, null);
                intent1.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent1, CODE_GALLERY_REQUEST);
                dialog.dismiss();
            }
        });
        tv_select_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
                dialog.dismiss();

            }
        });
        dialog.setView(view);
        dialog.show();
    }

    private void takePhoto() {
        Intent imageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
        startActivityForResult(imageIntent, CODE_RESULT_REQUEST);
    }

    public void onActivityResult(int requestCode, int resultCode,
                                 Intent intent) {

        switch (requestCode) {
            case CODE_RESULT_REQUEST:
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(getContext().getContentResolver().openInputStream(uriSavedImage));
                    setImageToHeadView(intent,bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                getActivity().finish();
                startActivity(getActivity().getIntent());
                break;
            case CODE_GALLERY_REQUEST:
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(intent.getData()));
                    FileOutputStream outputStream = new FileOutputStream(new File(Environment.getExternalStorageDirectory(), "head.jpeg"));
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.flush();
                    setImageToHeadView(intent,bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                getActivity().finish();
                startActivity(getActivity().getIntent());
                break;
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    private void setImageToHeadView(Intent intent,Bitmap b) {
        try {
            if (intent != null) {
                head.setImageBitmap(b);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

