package atamayo.offlinereader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

public class ConfirmDialog extends DialogFragment {
    public static final String TAG = "ConfirmDialog";
    private ConfirmDialogListener  mCallback;
    private static final String CONFIRM_DIALOG_TITLE = "title";
    private static final String CONFIRM_DIALOG_MESSAGE = "message";
    private static final String CONFIRM_DIALOG_ACTION = "action";

    public static ConfirmDialog newInstance(String title, String message, String action){
        ConfirmDialog dialog = new ConfirmDialog();
        Bundle args = new Bundle();
        args.putString(CONFIRM_DIALOG_TITLE, title);
        args.putString(CONFIRM_DIALOG_MESSAGE, message);
        args.putString(CONFIRM_DIALOG_ACTION, action);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            if (getTargetFragment() == null)
                mCallback = (ConfirmDialogListener) context;
        } catch (ClassCastException e) {
            Log.e("Dialog", e.toString());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        try{
            if(mCallback == null && getTargetFragment() != null)
                mCallback = (ConfirmDialogListener) getTargetFragment();
        }catch (ClassCastException e){
            Log.e("DIALOG", e.toString());
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        String title = getArguments().getString(CONFIRM_DIALOG_TITLE);
        String message = getArguments().getString(CONFIRM_DIALOG_MESSAGE);
        final String action = getArguments().getString(CONFIRM_DIALOG_ACTION);

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mCallback.onConfirmClick(action);
                    }
                })
                .create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialogInterface){
        super.onCancel(dialogInterface);

        mCallback.onConfirmClick(getArguments().getString(CONFIRM_DIALOG_ACTION));
    }
}
