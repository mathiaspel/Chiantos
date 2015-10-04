package com.example.mathias.chiantos;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Hashtable;
import java.util.Vector;


public class MainActivity extends Activity {

    private AutoCompleteTextView contact;
    private EditText number;
    private EditText message;
    private TextView done;
    private Button start;
    private Button stop;
    private Thread t;
    View.OnClickListener stopListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(t.isAlive())
                t.interrupt();
        }
    };
    private Integer number_repeat;
    private String text;
    private String phoneNumber;
    private String name;
    private ArrayAdapter<String> contactAdaptater;
    private Hashtable<String, String> hashContacts;
    View.OnClickListener okListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            done.setText("0");
            name = contact.getText().toString();
            if(hashContacts.get(name)!=null){
            phoneNumber = hashContacts.get(name);
            text = message.getText().toString();
            number_repeat = Integer.valueOf(number.getText().toString());
            t = new Thread(new Runnable() {
                private boolean interrupt;
                public void interrupt(){interrupt=true;}
                @Override
                public void run() {
                    for (int i = 0; i < number_repeat; i++) {
                        if(interrupt)
                            break;
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(phoneNumber, null, text, null, null);
                        SystemClock.sleep((long) 250);
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                done.setText(""+(Integer.valueOf(done.getText().toString())+1));
                            }
                        });
                    }
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,"Terminé! "+number_repeat+" messages envoyés à "+name+" ("+phoneNumber+")",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
            t.start();
            }else{
                Toast.makeText(getApplicationContext(),"Contact inexistant",Toast.LENGTH_SHORT).show();}
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        contact = (AutoCompleteTextView) findViewById(R.id.contact);
        number = (EditText) findViewById(R.id.repeat);
        message = (EditText) findViewById(R.id.message);
        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.abort);
        done=(TextView)findViewById(R.id.done);

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //Récupération des contacts(relou)
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,null, null, null, null);
        String[] projection    = new String[] {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER };
        Cursor names = getContentResolver().query(uri, projection, null, null, null);
        hashContacts=new Hashtable<>(names.getCount());
        Vector<String> listContacts=new Vector<>(names.getCount());
        int indexName = names.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int indexNumber =names.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        names.moveToFirst();
        do {
                hashContacts.put(names.getString(indexName), names.getString(indexNumber));
                listContacts.add(names.getString(indexName));
        }while(names.moveToNext());
        names.close();
        cur.close();
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        contactAdaptater = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listContacts);
        contact.setAdapter(contactAdaptater);
        contact.setDropDownAnchor(R.id.contact);
        contact.setThreshold(2);

        start.setOnClickListener(okListener);
        stop.setOnClickListener(stopListener);


    }
}
