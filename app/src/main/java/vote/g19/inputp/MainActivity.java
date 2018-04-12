package vote.g19.inputp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.sql.SQLOutput;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import static android.Manifest.permission.READ_SMS;

public class MainActivity extends AppCompatActivity {
    public EditText editText;
    public EditText editText2;
    public EditText editText3;
    public EditText passEdit;
    public Button button;
    public Button button2;
    public Connector connector;
    public static Queue<KeyValueList> q;
    public Thread t;
    public static int w;
    public Handler handler;
    public Looper looper;
    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        connector=new Connector();
        w = 0;
        q = new LinkedList<>();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = (EditText) findViewById(R.id.editText);
        editText2 = (EditText) findViewById(R.id.editText2);
        editText3 = (EditText) findViewById(R.id.editText3);
        passEdit=(EditText)findViewById(R.id.passedit);
        passEdit.setFocusable(false);
        passEdit.setEnabled(false);
        button = (Button) findViewById(R.id.button);
        button2 = (Button) findViewById(R.id.button2);
        if (checkSelfPermission(READ_SMS) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        final View.OnClickListener ls1=new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String severAddress = editText.getText().toString();
                final int Port = Integer.parseInt(editText2.getText().toString());
                if (w == 0) {
                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                connector = new Connector(severAddress, Port,handler);
                            } catch (IOException e) {
                                Message m=Message.obtain();
                                m.what=1;
                                m.obj=e.getMessage();
                                handler.sendMessage(m);
                            }


                        }
                    };
                    t = new Thread(r);
                    t.start();

                }
            }
        };
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if(msg.what==1){
                    super.handleMessage(msg);
                    editText3.append(msg.obj.toString());

                }
                else if (msg.what==2){
                    button.setText("Disconnect");
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            t.interrupt();
                            button.setOnClickListener(ls1);
                            button.setText("Connect");
                            connector=null;
                            w=0;
                        }
                    });
                    super.handleMessage(msg);
                    editText3.append(msg.obj.toString());
                }


            }
        };
        String DefaultAddress = "10.0.0.107";
        editText.setText(DefaultAddress.toCharArray(), 0, DefaultAddress.length());
        String port = "53217";
        editText2.setText(port.toCharArray(), 0, port.length());

        button.setOnClickListener(ls1);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                editText3.append(connector.getResult(1));
            }
        });


    }
}

