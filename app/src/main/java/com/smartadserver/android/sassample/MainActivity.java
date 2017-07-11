package com.smartadserver.android.sassample;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.content.Intent;
import android.content.Context;

public class MainActivity extends AppCompatActivity {

    private ListView mListView = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Context context = this;

        //Set Title
        setTitle(R.string.title_activity_main);

        //Prepare listView
        mListView = createListView();

        //Setup clickListener
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int selectedIndex = position;

                switch (selectedIndex) {
                    case 1:
                        startBannerActivity();
                        break;
                    case 2:
                        startInterstitialActivity();
                        break;
                    case 3:
                        startSimpleRecyclerActivity();
                        break;
                    case 4:
                        startRecyclerActivity();
                        break;
                    case 5:
                        startNativeActivity(false);
                        break;
                    case 6:
                        startNativeActivity(true);
                        break;
                    case 7:
                        startSwipeActivity();
                        break;
                    default: break;
                }
            }

        });
    }

    //////////////////////////
    // ListView Init
    //////////////////////////

    private ListView createListView() {

        //Create data to display
        Resources res = getResources();
        String[] itemList = res.getStringArray(R.array.activity_main_implementations_array);

        //Find listView and set Adapter
        ListView listView = (ListView) findViewById(R.id.list_view);

        //Create adapter
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, itemList);

        //Create header and footer
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.activity_main_header, mListView, false);
        ViewGroup footer = (ViewGroup) inflater.inflate(R.layout.activity_main_footer, mListView, false);

        //Add Header and footer
        listView.addHeaderView(header, null, false);
        listView.addFooterView(footer, null, false);

        //Set ListView Adapter
        listView.setAdapter(adapter);

        return listView;
    }

    //////////////////////////
    // Starting Activities
    //////////////////////////

    private void startBannerActivity () {
        Intent intent = new Intent(this, BannerActivity.class);
        startActivity(intent);
    }

    private void startInterstitialActivity () {
        Intent intent = new Intent(this, InterstitialActivity.class);
        startActivity(intent);
    }

    private void startSimpleRecyclerActivity () {
        Intent intent = new Intent(this, SimpleRecyclerActivity.class);
        startActivity(intent);
    }


    private void startRecyclerActivity () {
        Intent intent = new Intent(this, RecyclerActivity.class);
        startActivity(intent);
    }

    private void startNativeActivity (boolean withMedia) {
        Intent intent = new Intent(this, NativeActivity.class);

        if (withMedia) {
            intent.putExtra("withMedia", true);
        }

        startActivity(intent);
    }

    private void startSwipeActivity () {
        Intent intent = new Intent(this, SwipeActivity.class);
        startActivity(intent);
    }

}
