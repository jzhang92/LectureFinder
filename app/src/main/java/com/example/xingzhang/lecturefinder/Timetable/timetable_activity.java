package com.example.xingzhang.lecturefinder.Timetable;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.xingzhang.lecturefinder.Beacon.lecture_activity;
import com.example.xingzhang.lecturefinder.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class timetable_activity extends ActionBarActivity {

    ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);
        new TTParser().execute();

        list = (ListView)findViewById(R.id.TTlistView);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position,
                                                                long id) {
                                            String event = list.getItemAtPosition(position).toString();
                                            Intent intent = new Intent(timetable_activity.this, lecture_activity.class);
                                            intent.putExtra("event", event);
                                            startActivity(intent);
                                        }
                                    }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_timetable, menu);
        return true;
    }

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

    public class TTParser extends AsyncTask<String, String, String> {

        final String TAG = "TTParser.java";
        String textID = getIntent().getExtras().getString("ID");
        String baseurl = "http://stafftimetables.lincoln.ac.uk/V2/UL/Reports/StudentTT_std.asp?";
        //Student=12274789&Staff=Staff
        ArrayList<String> events = new ArrayList<String>();

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... arg0) {


            HTMLParser htmlParser = new HTMLParser();
            List timetable = new ArrayList();
            Document doc = Jsoup.parse(htmlParser.doPost(baseurl+"Student="+textID+"&Staff=Staff",textID));
            //System.out.println(doc.text());
            Elements tags = doc.select("table[rules=rows] tr td");

            for (Element td : tags) {
                if (td != null && td.text() != null) {
                    timetable.add(td.text());
                }
            }
            TTEvent(timetable);

            return null;
        }

        @Override
        protected void onPostExecute(String strFromDoInBg) {
            ListView list = (ListView) findViewById(R.id.TTlistView);
            ArrayAdapter<String> mArrayAdapter =
                    new ArrayAdapter<String>(timetable_activity.this, android.R.layout.simple_expandable_list_item_1, events);
            list.setAdapter(mArrayAdapter);
        }

        private void TTEvent(List tt) {

            for (int i = 0; i < tt.size(); i++) {
                if (i == 0 || i % 7 == 0) {

                    String module = (String) tt.get(i);
                    String location = (String) tt.get(i + 1);
                    String l_name = (String) tt.get(i + 2);
                    String type = (String) tt.get(i + 3);
                    String weektime = (String) tt.get(i + 5);
                    String time = (String) tt.get(i + 6);

                    String event = module+" "+type+"\n"+l_name +
                            "\n"+weektime+"\nTime "+time + "\nRoom "+location;

                    events.add(event);
                }
            }
        }
    }

}
