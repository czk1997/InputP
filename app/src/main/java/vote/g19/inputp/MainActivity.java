package vote.g19.inputp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
    public Button button;
    public Button button2;
public Connector connector;
    public static Queue<KeyValueList> q;
    public Thread t;
    public int w;

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
        button = (Button) findViewById(R.id.button);
        button2 = (Button) findViewById(R.id.button2);
        if (checkSelfPermission(READ_SMS) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        }

        String DefaultAddress = "10.0.0.107";
        editText.setText(DefaultAddress.toCharArray(), 0, DefaultAddress.length());
        String port = "53217";
        editText2.setText(port.toCharArray(), 0, port.length());
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String severAddress = editText.getText().toString();
                final int Port = Integer.parseInt(editText2.getText().toString());
                if (w == 0) {
                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                connector = new Connector(severAddress, Port);
                                System.out.println("here:   ===== "+(connector==null));
                            } catch (IOException e) {
                                editText3.append(e.getCause().toString());
                                editText3.append("\n");
                            }
                        }
                    };
                    t = new Thread(r);
                    t.start();
                    System.out.println("ririririri");
                    w = 1;
                }
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("ISCONN"+(connector==null));
                Map<String, Integer> PollMap = connector.getMap();
                if (!PollMap.isEmpty()) {
                    String topID = "";
                    int polls = 0;
                    for (String key : PollMap.keySet()) {
                        if (PollMap.get(key) >= polls) {
                            topID = key;
                            polls = PollMap.get(key);
                        }
                    }
                    editText3.append("The top one is " + topID + " and he/she has " + polls + " polls.\n");
                } else {
                    String e = "ERROR, NO ONE VOTED YET.";
                    editText3.append(e);
                }
            }
        });
    }
}

