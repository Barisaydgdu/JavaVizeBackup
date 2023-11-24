package com.barisaydgdu.barisaydgdupharmacy;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;



import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class ListScreen extends AppCompatActivity {

    private ListView lv;



   private static String API_URL = "https://www.nosyapi.com/apiv2/pharmacy?city=istanbul&county=avcilar";
    ArrayList<HashMap<String, String>> pharmacyList;
    AutoCompleteTextView autoCompleteTxt;
    ArrayAdapter<String> adapterItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_screen);
        autoCompleteTxt = findViewById(R.id.auto_complete_txt);

        autoCompleteTxt.setText(MainActivity.item);
        adapterItems = new ArrayAdapter<>(this, R.layout.list_item, MainActivity.items);
        autoCompleteTxt.setAdapter(adapterItems);
        pharmacyList = new ArrayList<>();
        lv = findViewById(R.id.listview);
        lv.setClickable(true);
        autoCompleteTxt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pharmacyList.clear();
                lv.setAdapter(null);
                GetData getData = new GetData();
                getData.execute();
                MainActivity.item = parent.getItemAtPosition(position).toString();
                Toast toast = Toast.makeText(getApplicationContext(), "Seçilen İl: " + MainActivity.item, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String address_ = ((TextView) view.findViewById(R.id.textview4)).getText().toString() + " " + ((TextView) view.findViewById(R.id.textview)).getText().toString() + " " + ((TextView) view.findViewById(R.id.textview2)).getText().toString();
                Uri mapUri = Uri.parse("geo:0,0?q=" + Uri.encode(address_));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

        GetData getData = new GetData();
        getData.execute();
    }


    public class GetData extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            String current = "";
            try {
                URL url = new URL(API_URL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("content-type", "application/json");
                urlConnection.setRequestProperty("authorization", "Bearer AUA79sBkUTAWmEkw2VXMrh8kFSdxeogJos6XCMAPbIZqRCQZVPMfn59m4z14");

                InputStream in = urlConnection.getInputStream();
                InputStreamReader isr = new InputStreamReader(in);

                int data = isr.read();
                while (data != -1) {
                    current += (char) data;
                    data = isr.read();
                }

                urlConnection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return current;
        }
        @Override
        protected void onPostExecute(String s) {
            try {
                JSONObject jsonObject = new JSONObject(s);

                // Success kontrolü
                boolean success = jsonObject.getBoolean("success");

                if (success) {
                    JSONArray jsonArray = jsonObject.getJSONArray("result");
                    pharmacyList.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject pharmacyObject = jsonArray.getJSONObject(i);

                        String name = pharmacyObject.getString("name");
                        String dist = pharmacyObject.getString("dist");
                        String address = pharmacyObject.getString("address");
                        String phone = pharmacyObject.getString("phone");
                        String loc = pharmacyObject.getString("loc");



                        HashMap<String, String> pharmacy = new HashMap<>();
                        pharmacy.put("name", name);
                        pharmacy.put("cityTown", dist);
                        pharmacy.put("phone", phone);
                        pharmacy.put("address", address);
                        pharmacy.put("loc", loc);

                        pharmacyList.add(pharmacy);
                    }
                } else {
                    // API başarısız olduysa burada bir hata mesajı gösterebilirsiniz
                    Toast.makeText(ListScreen.this, "API çağrısı başarısız.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            ListAdapter adapter = new SimpleAdapter(
                    ListScreen.this,
                    pharmacyList,
                    R.layout.row_layout,
                    new String[]{"name", "cityTown", "phone", "address"},
                    new int[]{R.id.textview, R.id.textview2, R.id.textview3, R.id.textview4});
            lv.setAdapter(adapter);
        }
    }
}
