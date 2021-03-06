package com.timecapsule.app.profilefragment.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.timecapsule.app.R;
import com.timecapsule.app.profilefragment.model.Capsule;

/**
 * Created by catwong on 3/10/17.
 */

public class ProfileCreatedViewHolder extends RecyclerView.ViewHolder {

    private final TextView buildingName;
    private TextView tv_numbers;
    private TextView tv_location_name;
    private TextView tv_date;
    private Bitmap bmImg;
    private ImageView imageView;


    public ProfileCreatedViewHolder(View itemView) {
        super(itemView);
        tv_numbers = (TextView) itemView.findViewById(R.id.profile_card_tc_created_num);
        tv_location_name = (TextView) itemView.findViewById(R.id.profile_card_tc_created_location_address);
        tv_date = (TextView) itemView.findViewById(R.id.profile_card_date);
        imageView = (ImageView) itemView.findViewById(R.id.iv_profile_card_tc_logo);
        buildingName = (TextView) itemView.findViewById(R.id.profile_card_tc_created_location_name);

    }


    public void bind(Capsule capsule, int numbers, Context context) {
        tv_numbers.setText(String.valueOf(numbers + 1));
        //tv_location_name.setText(capsule.getAddress());
        Picasso.with(context).load(capsule.getStorageUrl()).into(imageView);
        tv_date.setText(capsule.getDate());
        buildingName.setText(capsule.getAddress());


//        URL url = null;
//        try {
//            url = new URL(capsule.getStorageUrl().toString());
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
//        HttpURLConnection conn = null;
//        try {
//            conn = (HttpURLConnection) url.openConnection();
//            conn.connect();
//            InputStream is = conn.getInputStream();
//            bmImg = BitmapFactory.decodeStream(is);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        conn.setDoInput(true);
    }
}
