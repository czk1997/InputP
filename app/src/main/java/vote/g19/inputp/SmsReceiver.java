package vote.g19.inputp;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {
    private static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    @Override
    public void onReceive(Context context, Intent intent) {
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
                    KeyValueList temp=new KeyValueList();
                    temp.putPair("Scope","VotingSystem");
                    temp.putPair("VoterPhoneNo",messages[i].getOriginatingAddress());
                    temp.putPair("MsgID","701");
                    temp.putPair("CandidateID",messages[i].getMessageBody());
                    MainActivity.q.offer(temp);
                    String t=new String("ID: "+messages[i].getMessageBody()+", Num: "+messages[i].getOriginatingAddress());
                    Toast.makeText(context, t, Toast.LENGTH_LONG).show();
                }

                // prevent any other broadcast receivers from receiving broadcast
                // abortBroadcast();
            }
        }
    }
}
