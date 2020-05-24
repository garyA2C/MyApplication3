package fr.insalyon.painttheworldapp.Navigation_drawer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import fr.insalyon.painttheworldapp.R;

public class Adapter_list extends ArrayAdapter<String> {
    private Integer[] tab_images_pour_la_liste = {
            R.drawable.camera, R.drawable.phone};

    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)
                getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.list_view_item, parent, false);

        TextView textView = (TextView) rowView.findViewById(R.id.label);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);

        textView.setText(getItem(position));

        if(convertView == null )
            imageView.setImageResource(tab_images_pour_la_liste[position]);
        else
            rowView = (View)convertView;

        return rowView;
    }

    public Adapter_list(Context context, String[] values) {
        super(context, R.layout.list_view_item, values);
    }
}
