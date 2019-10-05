package emn.southcoder.e_jeep;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private TextView textViewBlock, textViewGreetings;
    //TextView textViewMccNo, textViewSerialNo, textViewFName, textViewMName, textViewLName, textViewIssueDate, textViewExpiryDate, textViewInfo, textViewTagInfo;
    private Button btnSyncUserList, btnUploadRidersLogs;
    private boolean isNFCSupported = false;
    private boolean loggedIn  = false;
    private boolean loginAlertShown = false;
    private AlertDialog loginAlert;
    private Tag tag;
    private MifareClassic mifareClassicTag;
//    Timer timerObj;
//    TimerTask timerTaskObj;

    private Handler handler;
    private Runnable runnable;
    private int delay = 2*1000;

    private DatabaseHelper dbHelper;
    private ProgressDialog pdialog;
    private String deviceID = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textViewGreetings = findViewById(R.id.txt_greetings);
        textViewBlock = findViewById(R.id.block);

        TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        //-- Get unique device ID
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 100);
        }
        else deviceID = tm.getDeviceId();

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        handler = new Handler();
        dbHelper = DatabaseHelper.getInstance(this);

        if (nfcAdapter != null)
        {
            isNFCSupported = true;

            if (!nfcAdapter.isEnabled()) {
                Toast.makeText(this, "NFC is not enabled!", Toast.LENGTH_LONG).show();
                showNFCSettings();
            }
        }
        else {
            Toast.makeText(this, "NFC is not supported on this device!", Toast.LENGTH_LONG).show();
            finish();
        }

        showLoginAlert(this);
    }

    @Override
    protected void onResume() {
//        handler.postDelayed( runnable = new Runnable() {
//            public void run() {
//                clearMessage();
//                handler.postDelayed(runnable, delay);
//            }
//        }, delay);
//        btnSyncUserList = findViewById(R.id.btn_sync_user_list);
//        btnUploadRidersLogs = findViewById(R.id.btn_upload_data);

        super.onResume();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        if (isNFCSupported)
            setupForegroundDispatch(this, nfcAdapter);

//        if (!loggedIn) {
//            showLoginAlert(this);
//            //OpenLoginActivityDialog();
//        }
    }

    @Override
    protected void onPause() {
        //handler.removeCallbacks(runnable); //stop handler when activity not visible

        super.onPause();

        if (isNFCSupported)
            stopForegroundDispatch(this, nfcAdapter);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        handleIntent(intent);
    }

    public void VerifyUser(String mccno, String access) {
        //-- ToDo: Verify if user is valid to use this app
        loggedIn = true; //-- Flag for user login status
        this.loginAlert.dismiss();
//        if (dbHelper.isValidLogin(mccno, access)) {
//            loggedIn = true; //-- Flag for user login status
//            this.loginAlert.dismiss();
//        }
//        else Toast.makeText(this, "Invalid user", Toast.LENGTH_LONG).show();
    }

    public void CloseMainActivity() {
        finish();
    }

//    public void OpenLoginActivityDialog() {
//        loginActivity = new LoginActivity();
//        loginActivity.show(getSupportFragmentManager(), "Login");
//    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();

        //Toast.makeText(this, action, Toast.LENGTH_SHORT).show();

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            //Toast.makeText(this, "ACTION_TECH_DISCOVERED", Toast.LENGTH_SHORT).show();
            tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            if (tag != null) {
                //clearMessage();
                readMifareClassic(tag);
            }

//            if (tag == null) {
//                textViewInfo.setText("tag == null");
//            }
//            else {
//                String tagInfo = tag.toString() + "\n";
//
//                tagInfo += "\nTag Id: \n";
//                byte[] tagId = tag.getId();
//                tagInfo += "length = " + tagId.length +"\n";
//
//                for(int i=0; i<tagId.length; i++){
//                    tagInfo += String.format("%02X", tagId[i] & 0xff) + " ";
//                }
//
//                tagInfo += "\n";
//
//                String[] techList = tag.getTechList();
//                tagInfo += "\nTech List\n";
//                tagInfo += "length = " + techList.length +"\n";
//
//                for(int i=0; i<techList.length; i++) {
//                    tagInfo += techList[i] + "\n ";
//                }
//
//                textViewInfo.setText(tagInfo);
//
//                //Only android.nfc.tech.MifareClassic specified in nfc_tech_filter.xml,
//                //so must be MifareClassic
//
                //nfcAdapter.ignore(tag, 1000, NfcAdapter.OnTagRemovedListener, null);
//            }
        }
        else {
            Toast.makeText(this, "onResume() : " + action, Toast.LENGTH_SHORT).show();
        }
    }

    public void readMifareClassic(Tag tag) {
        mifareClassicTag = MifareClassic.get(tag);

//        String typeInfoString = "--- MifareClassic tag ---\n";
//        int type = mifareClassicTag.getType();
//        switch(type){
//            case MifareClassic.TYPE_PLUS:
//                typeInfoString += "MifareClassic.TYPE_PLUS\n";
//                break;
//            case MifareClassic.TYPE_PRO:
//                typeInfoString += "MifareClassic.TYPE_PRO\n";
//                break;
//            case MifareClassic.TYPE_CLASSIC:
//                typeInfoString += "MifareClassic.TYPE_CLASSIC\n";
//                break;
//            case MifareClassic.TYPE_UNKNOWN:
//                typeInfoString += "MifareClassic.TYPE_UNKNOWN\n";
//                break;
//            default:
//                typeInfoString += "unknown...!\n";
//        }
//
//        int size = mifareClassicTag.getSize();
//        switch(size){
//            case MifareClassic.SIZE_1K:
//                typeInfoString += "MifareClassic.SIZE_1K\n";
//                break;
//            case MifareClassic.SIZE_2K:
//                typeInfoString += "MifareClassic.SIZE_2K\n";
//                break;
//            case MifareClassic.SIZE_4K:
//                typeInfoString += "MifareClassic.SIZE_4K\n";
//                break;
//            case MifareClassic.SIZE_MINI:
//                typeInfoString += "MifareClassic.SIZE_MINI\n";
//                break;
//            default:
//                typeInfoString += "unknown size...!\n";
//        }

//        int blockCount = mifareClassicTag.getBlockCount();
//        typeInfoString += "BlockCount \t= " + blockCount + "\n";
//        int sectorCount = mifareClassicTag.getSectorCount();
//        typeInfoString += "SectorCount \t= " + sectorCount + "\n";
//
//        textViewTagInfo.setText(typeInfoString);

        new ReadMifareClassicTask(mifareClassicTag).execute();
    }

    private class ReadMifareClassicTask extends AsyncTask<Void, Void, Void> {
        /*
        MIFARE Classic tags are divided into sectors, and each sector is sub-divided into blocks.
        Block size is always 16 bytes (BLOCK_SIZE). Sector size varies.
        MIFARE Classic 1k are 1024 bytes (SIZE_1K), with 16 sectors each of 4 blocks.
        */

        MifareClassic mifareClassic;
        boolean success;
        final int numOfSector = 16;
        final int numOfBlockInSector = 4;
        byte[][][] buffer = new byte[numOfSector][numOfBlockInSector][MifareClassic.BLOCK_SIZE];
        byte[] accessKeyA = new byte[] { 0x45, 0x52, 0x57, 0x49, 0x4E, 0x00 };
        //byte[] accessKeyA = new byte[] { 0xD3, 0xF7, 0xD3, 0xF7, 0xD3, 0xF7 };
        byte[] accessKeyB = new byte[] { (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF };
        byte[] mccNo = new byte[MifareClassic.BLOCK_SIZE];
        byte[] cardNo = new byte[MifareClassic.BLOCK_SIZE];
        byte[] fName = new byte[MifareClassic.BLOCK_SIZE];
        byte[] mName = new byte[MifareClassic.BLOCK_SIZE];
        byte[] lName = new byte[MifareClassic.BLOCK_SIZE];
        byte[] issueDate = new byte[MifareClassic.BLOCK_SIZE];
        byte[] expiryDate = new byte[MifareClassic.BLOCK_SIZE];
        byte[] access1 = new byte[MifareClassic.BLOCK_SIZE];
        byte[] access2 = new byte[MifareClassic.BLOCK_SIZE];
        byte[] access3 = new byte[MifareClassic.BLOCK_SIZE];
        byte[] southcoderTag = new byte[MifareClassic.BLOCK_SIZE];

        ReadMifareClassicTask(MifareClassic tag){
            mifareClassic = tag;
            success = false;
        }

        @Override
        protected void onPreExecute() {
            if (loggedIn)
                textViewBlock.setText("Reading Tag, do not remove...");
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                mifareClassic.connect();

                success = true;
                //tagScanned = true;

                //-- Read values from sector 0
                if (mifareClassic.authenticateSectorWithKeyB(0, accessKeyB)) {
                    cardNo = mifareClassic.readBlock(1);
                }
                else success = false;

                //-- Read values from sector 1
                if (mifareClassic.authenticateSectorWithKeyB(1, accessKeyB)) {
                    mccNo = mifareClassic.readBlock(4);
                    issueDate = mifareClassic.readBlock(5);
                    expiryDate = mifareClassic.readBlock(6);
                }
                else success = false;
//
                //-- Read values from sector 2
                if (mifareClassic.authenticateSectorWithKeyB(2, accessKeyB)) {
                    fName = mifareClassic.readBlock(8);
                    mName = mifareClassic.readBlock(9);
                    lName = mifareClassic.readBlock(10);
                }
                else success = false;

                //-- Read values from sector 3
                if (mifareClassic.authenticateSectorWithKeyB(3, accessKeyB)) {
                    access1 = mifareClassic.readBlock(12);
                    access2 = mifareClassic.readBlock(13);
                    access3 = mifareClassic.readBlock(14);
                }
                else success = false;

                //-- Read values from sector 4
                if (mifareClassic.authenticateSectorWithKeyB(4, accessKeyB)) {
                    southcoderTag = mifareClassic.readBlock(16);
                }
                else success = false;

//                for(int s=0; s<numOfSector; s++) {
//                    if(mifareClassic.authenticateSectorWithKeyA(s, accessKeyA)) {
//                        for(int b=0; b<numOfBlockInSector; b++) {
//                            int blockIndex = (s * numOfBlockInSector) + b;
//                            buffer[s][b] = mifareClassic.readBlock(blockIndex);
//                        }
//                    }
//                }

                //acknowledgeBeep();
            } catch (IOException e) {
                clearMessage();
                e.printStackTrace();
            } finally {
                if(mifareClassic != null){
                    try {
                        mifareClassic.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //display block

            if (success) {
//                try {
//                    //-- Serial
//                    textViewSerialNo.setText("Card Serial: " + hexToAscii(byteToString(cardNo, MifareClassic.BLOCK_SIZE)));
//                    //-- MCC Number
//                    textViewMccNo.setText("MCC Number: " + hexToAscii(byteToString(mccNo, MifareClassic.BLOCK_SIZE)));
//                    //-- First Name
//                    textViewFName.setText("First Name: " + hexToAscii(byteToString(fName, MifareClassic.BLOCK_SIZE)));
//                    //-- Middle Name
//                    textViewMName.setText("Middle Name: " + hexToAscii(byteToString(mName, MifareClassic.BLOCK_SIZE)));
//                    //-- Last Name
//                    textViewLName.setText("Last Name: " + hexToAscii(byteToString(lName, MifareClassic.BLOCK_SIZE)));
//                    //-- Issue Date
//                    textViewIssueDate.setText("Issue Date: " + hexToAscii(byteToString(issueDate, MifareClassic.BLOCK_SIZE)));
//                    //-- Expiry Date
//                    textViewExpiryDate.setText("Expiry Date: " + hexToAscii(byteToString(expiryDate, MifareClassic.BLOCK_SIZE)));
//
//                    textViewBlock.setText("");
//
////                StringBuilder stringBlock = new StringBuilder();
////                for(int i=0; i<numOfSector; i++){
////                    stringBlock.append(i + " :\n");
////                    for(int j=0; j<numOfBlockInSector; j++){
////                        for(int k=0; k<MifareClassic.BLOCK_SIZE; k++){
////                            stringBlock.append(String.format("%02X", buffer[i][j][k] & 0xff) + " ");
////                        }
////                        stringBlock.append("\n");
////                    }
////                    stringBlock.append("\n");
////                }
////                textViewBlock.setText(stringBlock);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

                String mccno = hexToAscii(byteToString(mccNo, MifareClassic.BLOCK_SIZE));
                String name = hexToAscii(byteToString(fName, MifareClassic.BLOCK_SIZE));
                String access = hexToAscii(byteToString(access1, MifareClassic.BLOCK_SIZE)) +
                                hexToAscii(byteToString(access2, MifareClassic.BLOCK_SIZE)) +
                                hexToAscii(byteToString(access3, MifareClassic.BLOCK_SIZE));
                String scTag = hexToAscii(byteToString(southcoderTag, MifareClassic.BLOCK_SIZE));

                if (loggedIn) {
                    try {
                        if (mccno != "") {
                            textViewGreetings.setBackground(getResources().getDrawable(R.drawable.rounded_corner_tv_blue));
                            textViewGreetings.setTextColor(getResources().getColor(R.color.colorWhite));
                            textViewGreetings.setText("Welcome " + name + "!\n Enjoy your free ride.");
                        }
                        else {
                            textViewGreetings.setBackground(getResources().getDrawable(R.drawable.rounded_corner_tv_red));
                            textViewGreetings.setTextColor(getResources().getColor(R.color.colorWhite));
                            textViewGreetings.setText("Your may have an invalid card.");
                        }

                        textViewBlock.setText("Reading NFC card successful.");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else VerifyUser(mccno.substring(0, 6), "ejeep"); //VerifyUser(mccno, access);
            }
            else {
                if (loggedIn) {
                    textViewGreetings.setBackground(getResources().getDrawable(R.drawable.rounded_corner_tv_red));
                    textViewGreetings.setTextColor(getResources().getColor(R.color.colorWhite));
                    textViewGreetings.setText("Unable to read your card. Please try again.");
                    textViewBlock.setText("Failed to read NFC card!");
                }
            }
        }
    }

    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, intent, 0);
        adapter.enableForegroundDispatch(activity, pendingIntent, null, null);
    }

    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    private void showNFCSettings() {
        Toast.makeText(this, "You need to enable NFC", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
        startActivity(intent);
    }

    private String byteToString(byte[] b, int byteLen) {
        String retVal = "";

        try {
            for (int i = 0; i < byteLen; i++) {
                retVal += String.format("%02X", b[i] & 0xff);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return retVal;
    }

    private String hexToAscii(String s) {
        if (s.length() <= 0) return "";

        int n = s.length();
        StringBuilder sb = new StringBuilder(n / 2);

        for (int i = 0; i < n; i += 2) {
            char a = s.charAt(i);
            char b = s.charAt(i + 1);
            sb.append((char) ((hexToInt(a) << 4) | hexToInt(b)));
        }

        return sb.toString();
    }

    private static int hexToInt(char ch) {
        if ('a' <= ch && ch <= 'f') { return ch - 'a' + 10; }
        if ('A' <= ch && ch <= 'F') { return ch - 'A' + 10; }
        if ('0' <= ch && ch <= '9') { return ch - '0'; }

        throw new IllegalArgumentException(String.valueOf(ch));
    }

    private void acknowledgeBeep() {
        ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 500);

        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 500);
    }

    private void showLoginAlert(Activity activity) {
        if (!loginAlertShown) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);

            LayoutInflater inflater = activity.getLayoutInflater();
            View view = inflater.inflate(R.layout.activity_login, null);

            builder.setView(view)
                    .setTitle("EJRF Login")
                    .setCancelable(false)
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            CloseMainActivity();
                        }
                    });
//                    .setPositiveButton("Login", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//
//                        }
//                    });

            loginAlert = builder.create();
            loginAlert.show();
            loginAlertShown = true;

            btnSyncUserList = view.findViewById(R.id.btn_sync_user_list);
            btnUploadRidersLogs = view.findViewById(R.id.btn_upload_data);

            btnSyncUserList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //new JsonTask().execute("https://");
                    Toast.makeText(btnSyncUserList.getContext(), "Sync User List Pressed", Toast.LENGTH_LONG).show();
                }
            });

            btnUploadRidersLogs.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(btnUploadRidersLogs.getContext(), "Upload Riders Logs Pressed", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

//    private void checkIfCardIsStillInRange() {
//        timerObj = new Timer();
//        timerTaskObj = new TimerTask() {
//            public void run() {
//                readMifareClassic(tag);
//            }
//        };
//        timerObj.schedule(timerTaskObj, 0, 1000);
//    }

    private void clearMessage() {
        textViewGreetings.setBackground(getResources().getDrawable(R.drawable.rounded_corner_tv));
        textViewGreetings.setTextColor(getResources().getColor(R.color.colorYellow));
        textViewGreetings.setText("Welcome!\n Enjoy your free ride.");
        textViewBlock.setText("");
    }

    private boolean IsConnectedToTheInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pdialog = new ProgressDialog(MainActivity.this);
            pdialog.setMessage("Syncing User list please wait...");
            pdialog.setCancelable(false);
            pdialog.show();
        }

        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)
                }

                Gson jsonData = new Gson();

                return jsonData.toJson(buffer.toString());

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pdialog.isShowing()){
                pdialog.dismiss();
            }

            //txtJson.setText(result);
            //-- ToDo: Save result in table
        }
    }
}
