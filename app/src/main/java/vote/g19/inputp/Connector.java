package vote.g19.inputp;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public  class Connector {
    public ArrayList<Long> phoneList;
    public Map<String, Integer> PollMap;
    private String ServerAddress;
    private int Port;
    private Socket socket;
    private static volatile boolean cancelled;
    public Connector(){

    }
    public Connector(String ServerAddress, int Port) throws IOException {
        phoneList=new ArrayList<>();
        PollMap =new HashMap<>();

        this.ServerAddress = ServerAddress;
        this.Port = Port;
        socket = new Socket(ServerAddress, Port);
        socket.setKeepAlive(true);
        connect();

    }

    public void getMessage() {
        MsgDecoder msgDecoder = null;
        try {
            msgDecoder = new MsgDecoder(socket.getInputStream());
            System.out.println("Decoder Int complete!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        KeyValueList t;
        while (true) {
            try {
                System.out.println("we got here");
                t = msgDecoder.getMsg();
                System.out.println("we10 got next");
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
                System.out.println("RIGHT!");
                if (oritinalAddress != 0) {
                    System.out.println(oritinalAddress);
                    if (!phoneList.contains(oritinalAddress)) {
                        if (!PollMap.containsKey(msgContent)) {
                            PollMap.put(msgContent, 1);
                            System.out.println("DONE!");
                        } else {
                            PollMap.put(msgContent, PollMap.get(msgContent) + 1);
                        }
                    }
                }
            }
        }
    }


    public void connect() {
        phoneList = new ArrayList<>();
        PollMap = new HashMap<>();
        try {
            MsgEncoder msgEncoder = new MsgEncoder(socket.getOutputStream());
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
            while (!cancelled)

            {
                if (MainActivity.q.peek() != null) {
                    msgEncoder.sendMsg(MainActivity.q.poll());
                }

            }

        } catch (Exception e)

        {
            e.printStackTrace();
        }
    }
    public Map<String, Integer> getMap(){
        System.out.println("ISEMPTY : "+ PollMap.isEmpty());
            return PollMap;
    }
}
