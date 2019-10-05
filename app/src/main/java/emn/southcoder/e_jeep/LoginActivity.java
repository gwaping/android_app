package emn.southcoder.e_jeep;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;

public class LoginActivity extends AppCompatDialogFragment {
    //private EditText editTextUsername;
    //private EditText editTextPassword;
    private LoginActivityListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_login, null);

        builder.setView(view)
                .setTitle("Login")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.CloseMainActivity();
                    }
                })
                .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //String username = editTextUsername.getText().toString();
                        //String password = editTextPassword.getText().toString();
                        //listener.applyTexts(username, password);
                        listener.VerifyUser();
                    }
                });

        //editTextUsername = view.findViewById(R.id.edit_username);
        //editTextPassword = view.findViewById(R.id.edit_password);

        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        listener.CloseMainActivity();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (LoginActivityListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement LoginActivityListener");
        }
    }

    public void closeMe() {
        //this.dismiss();

    }

    public interface LoginActivityListener {
        //void applyTexts(String username, String password);
        void VerifyUser();
        void CloseMainActivity();
    }
}
