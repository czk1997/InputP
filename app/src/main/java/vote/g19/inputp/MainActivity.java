package vote.g19.inputp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.security.Key;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Queue;
public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    public EditText editText;
    public EditText editText2;
    public EditText editText3;
    public Button button;
    public Connector connector;
    public static Queue<KeyValueList> q;
    public Thread t;
    public int w;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        w=0;
        q = new LinkedList<>();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText=(EditText)findViewById(R.id.editText);
        editText2=(EditText)findViewById(R.id.editText2);
        editText3=(EditText)findViewById(R.id.editText3);
        button=(Button)findViewById(R.id.button);
        String DefaultAddress=new String("74.98.248.94");
        editText.setText(DefaultAddress.toCharArray(),0,DefaultAddress.length());
        String port=new String("53217");
        editText2.setText(port.toCharArray(),0,port.length());
        button.setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {
        final String severAddress = editText.getText().toString();
        final int Port = Integer.parseInt(editText2.getText().toString());
        if(w==0){

            Runnable r=new Runnable() {
                @Override
                public void run() {
                    try {
                        connector=new Connector(severAddress,Port);
                        connector.connect();
                        System.out.println("ALL right");
                    } catch (IOException e) {
                        editText3.append(e.getCause().toString());
                        editText3.append("\n");
                    }
                }
            };
            t = new Thread(r);
            t.start();
            System.out.println("ririririri");
            w=1;
        }



    }

}
