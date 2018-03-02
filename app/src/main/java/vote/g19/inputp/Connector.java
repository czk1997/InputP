package vote.g19.inputp;

import android.telephony.SmsMessage;

import java.io.IOException;
import java.net.Socket;

public class Connector {
    private String ServerAddress;
    private int Port;
    private Socket socket;
    private static MsgEncoder msgEncoder;
    public Connector(String ServerAddress, int Port) throws IOException {
        this.ServerAddress=ServerAddress;
        this.Port=Port;
        socket =new Socket(ServerAddress, Port);
        socket.setKeepAlive(true);
        connect();
    }
    public void connect(){
        try{
            msgEncoder=new MsgEncoder(socket.getOutputStream());
            KeyValueList k= new KeyValueList();
            k.putPair("Scope","VotingSystem");
            k.putPair("MessageType","Register");
            k.putPair("Role","Basic");
            k.putPair("Name","Voting System");
            msgEncoder.sendMsg(k);
            KeyValueList k2= new KeyValueList();
            k2.putPair("Scope","VotingSystem");
            k2.putPair("MessageType","Connect");
            k2.putPair("Role","Basic");
            k2.putPair("Name","Voting System");
            msgEncoder.sendMsg(k2);
            KeyValueList k3= new KeyValueList();
            k3.putPair("Scope","VotingSystem");
            k3.putPair("MsgID","21");
            k3.putPair("MessageType","Setting");
            k3.putPair("Passcode","1234567");
            k3.putPair("SecurityLevel","3");
            k3.putPair("Name","Voting System");
            k3.putPair("InputMsgID 1","701");
            k3.putPair("InputMsgID 2","702");
            k3.putPair("InputMsgID 3","703");
            k3.putPair("OutputMsgID 1","711");
            k3.putPair("OutputMsgID 2","712");
            k3.putPair("OutputMsgID 3","726");
            msgEncoder.sendMsg(k3);
            while (true){
                if(MainActivity.q.peek()!=null){
                    msgEncoder.sendMsg(MainActivity.q.poll());
                }
            }
        }catch (IOException e){
        e.printStackTrace();
        }
    }
}
