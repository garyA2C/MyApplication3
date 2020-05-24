package fr.insalyon.painttheworldapp.Navigation_drawer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.insalyon.painttheworldapp.R;

public class Fragment_second extends Fragment {
    private GridView grid_view;
    private SimpleAdapter mAdapter;
    private Integer[] img = {R.drawable.phone, R.drawable.camera};
    private List<Map<String, Object>> data;
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_second, container, false);

        grid_view = root.findViewById(R.id.grid_view);

        data = new ArrayList<Map<String, Object>>();
        mAdapter = new SimpleAdapter(getContext(),getdata(),
                R.layout.grid_view,new String[]{"img"},new int[]{R.id.grid});
        grid_view.setAdapter(mAdapter);
        return root;
    }

    private List<Map<String,Object>> getdata(){
        for (int i=0; i<img.length; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("img", img[i]);
            map.put("txt", "1");
            data.add(map);
        }
        return data;
    }
}


