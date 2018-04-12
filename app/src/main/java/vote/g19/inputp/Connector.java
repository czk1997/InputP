package vote.g19.inputp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsMessage;
import android.widget.Toast;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;


public class Connector extends BroadcastReceiver {
    private static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    public int passcode;
    public int SecurityLevel;
    public static ArrayList<Long> phoneList;
    public static Map<String, Integer> PollMap;
    private String ServerAddress;
    private int Port;
    private Socket socket;
    private static volatile boolean cancelled;
    public MsgEncoder msgEncoder;
    private Handler handler;
    public static Queue<KeyValueList> kvlQueue;
    MsgDecoder msgDecoder ;
    public Connector() {

    }

    public Connector(String ServerAddress, int Port, Handler handler) throws IOException {

        kvlQueue = new LinkedList<>();
        this.handler = handler;
        phoneList = new ArrayList<>();
        PollMap = new TreeMap<>();
        this.ServerAddress = ServerAddress;
        this.Port = Port;
        Socket socket = new Socket(ServerAddress, Port);
        msgEncoder = new MsgEncoder(socket.getOutputStream());
        msgDecoder = new MsgDecoder(socket.getInputStream());
        Message nM = Message.obtain();
        nM.what = 2;
        nM.obj = "Server connected on Address: " + ServerAddress + ":" + Port + "\n";
        handler.sendMessage(nM);
        connect();

    }


    public void getMessage() {
        KeyValueList t;
        while (true) {
            try {
                t = msgDecoder.getMsg();
                System.out.println("msg");
                processMsg(t);

            } catch (Exception e) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e2) {
                    // TODO Auto-generated catch block
                    // e2.printStackTrace();
                }
            }
        }
    }

    public void processMsg(KeyValueList t) {
        if (t.getValue("Scope").contains("VotingSystem")) {
            long oritinalAddress = Long.parseLong(t.getValue("VoterPhoneNo"));
            String msgContent = t.getValue("CandidateID");
            System.out.println("MsgID" + t.getValue("MsgID"));
            if (t.getValue("MsgID").contains("701")) {
                if (oritinalAddress != 0) {
                    System.out.println(oritinalAddress);
                    if (!phoneList.contains(oritinalAddress)) {
                        if (!PollMap.containsKey(msgContent)) {
                            phoneList.add(oritinalAddress);
                            PollMap.put(msgContent, 1);
                            String re = "Received message with candidate ID: " + msgContent + " by Phone number: " + oritinalAddress + "\n";
                            Message msg = Message.obtain();
                            msg.what = 1;
                            msg.obj = re;
                            handler.sendMessage(msg);
                        } else {
                            PollMap.put(msgContent, PollMap.get(msgContent) + 1);
                        }
                    }
                }
            } else if (t.getValue("MsgID").contains("24")) {
                if (!t.getValue("Passcode").isEmpty() && !t.getValue("SecurityLevel").isEmpty()) {
                    passcode = Integer.parseInt(t.getValue("Passcode"));
                    SecurityLevel = Integer.parseInt(t.getValue("SecurityLevel"));
                }
            } else if (t.getValue("MsgID").contains("703")) {
                String ids = t.getValue("MsgID");
                String[] idint = ids.split(";");
                for (String temps : idint) {
                    if (!PollMap.containsKey(temps)) {
                        PollMap.put(temps, 0);
                    }
                }
            } else if (t.getValue("MsgID").contains("702")) {
                if (Integer.parseInt(t.getValue("Passcode")) == passcode && Integer.parseInt(t.getValue("N")) <= PollMap.size()) {
                    int n = Integer.parseInt(t.getValue("N"));
                    String re = getResult(n);
                    Message m = Message.obtain();
                    m.what = 1;
                    m.obj = re;
                    handler.sendMessage(m);
                }
            } else {
                Message msg = Message.obtain();
                msg.what = 1;
                msg.obj = t.getValue("Sender");
                this.handler.sendMessage(msg);
            }
        }
    }

    public String getResult(int n) {
        Comparator<Map.Entry<String, Integer>> valueComparator = new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) {
                // TODO Auto-generated method stub
                return o2.getValue() - o1.getValue();
            }
        };
        List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(PollMap.entrySet());
        Collections.sort(list, valueComparator);
        StringBuilder results = new StringBuilder();
        int i = 0;
        for (Map.Entry<String, Integer> entry : list) {
            if (i < n) {
                results.append(entry.getKey() + " : " + entry.getValue() + " \n");
                i++;
            }
        }
        return results.toString();

    }

    public void connect() {

        phoneList = new ArrayList<>();
        PollMap = new HashMap<>();
        try {

            KeyValueList k = new KeyValueList();
            k.putPair("Scope", "VotingSystem");
            k.putPair("MessageType", "Register");
            k.putPair("Role", "Basic");
            k.putPair("Name", "VotingSystem");
            msgEncoder.sendMsg(k);
            KeyValueList k2 = new KeyValueList();
            k2.putPair("Scope", "VotingSystem");
            k2.putPair("MessageType", "Connect");
            k2.putPair("Role", "Basic");
            k2.putPair("Name", "VotingSystem");
            msgEncoder.sendMsg(k2);
            KeyValueList k3 = new KeyValueList();
            k3.putPair("Scope", "VotingSystem");
            k3.putPair("MsgID", "21");
            k3.putPair("MessageType", "Setting");
            k3.putPair("Passcode", "1234567");
            k3.putPair("SecurityLevel", "3");
            k3.putPair("Name", "VotingSystem");
            k3.putPair("Receiver", "Receiver");
            k3.putPair("InputMsgID 1", "701");
            k3.putPair("InputMsgID 2", "702");
            k3.putPair("InputMsgID 3", "703");
            k3.putPair("OutputMsgID 1", "711");
            k3.putPair("OutputMsgID 2", "712");
            k3.putPair("OutputMsgID 3", "726");
            msgEncoder.sendMsg(k3);
            Runnable runnable = new Runnable() {
                @Override
                @SuppressWarnings("InfiniteLoopStatement")
                public void run() {
                    getMessage();
                }
            };
            Thread tk = new Thread(runnable);
            tk.start();

            while (true) {
                if (kvlQueue.peek() != null) {
                    msgEncoder.sendMsg(kvlQueue.poll());
                }
            }

        } catch (Exception e)

        {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Handler handler = new Handler();
        String action = intent.getAction();
        if (intent.getAction().equals(SMS_RECEIVED_ACTION)) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                // get sms objects
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus.length == 0) {
                    return;
                }
                // large message might be broken into many
                SmsMessage[] messages = new SmsMessage[pdus.length];
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < pdus.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    sb.append(messages[i].getMessageBody());
                    KeyValueList temp = new KeyValueList();
                    temp.putPair("Scope", "VotingSystem");
                    temp.putPair("VoterPhoneNo", messages[i].getOriginatingAddress());
                    temp.putPair("MessageType", "Alert");
                    temp.putPair("Sender", "VotingSystem");
                    temp.putPair("Receiver", "VotingSystem");
                    temp.putPair("Name", "VotingSystem");
                    temp.putPair("MsgID", "701");
                    temp.putPair("CandidateID", messages[i].getMessageBody());
                    kvlQueue.add(temp);
                    String t = new String("ID: " + messages[i].getMessageBody() + ", Num: " + messages[i].getOriginatingAddress());
                    Message msg = new Message();
                    msg.what = 701;
                    msg.obj = t;
                    handler.sendMessage(msg);
                    Toast.makeText(context, t, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

}
