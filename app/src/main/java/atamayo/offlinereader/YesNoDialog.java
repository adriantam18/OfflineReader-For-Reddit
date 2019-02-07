package atamayo.offlinereader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

public class YesNoDialog extends DialogFragment {
    public static final String TAG = "YesNoDialog";
    private YesNoDialogListener mCallback;
    private static final String YESNO_DIALOG_TITLE = "title";
    private static final String YESNO_DIALOG_MESSAGE = "message";
    private static final String YESNO_DIALOG_ACTION = "action";

    public static YesNoDialog newInstance(String title, String message, String action){
        YesNoDialog dialog = new YesNoDialog();
        Bundle args = new Bundle();
        args.putString(YESNO_DIALOG_TITLE, title);
        args.putString(YESNO_DIALOG_MESSAGE, message);
        args.putString(YESNO_DIALOG_ACTION, action);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        try{
            if(getTargetFragment() == null)
                mCallback = (YesNoDialogListener) context;
        }catch (ClassCastException e){
            Log.e("Dialog", e.toString());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        try{
            if(mCallback == null)
                mCallback = (YesNoDialogListener) getTargetFragment();
        }catch (ClassCastException e){
            Log.e("DIALOG", e.toString());
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        String title = getArguments().getString(YESNO_DIALOG_TITLE);
        String message = getArguments().getString(YESNO_DIALOG_MESSAGE);
        final String action = getArguments().getString(YESNO_DIALOG_ACTION);

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mCallback.onYesClick(action);
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mCallback.onNoClick(action);
                    }
                })
                .create();
        dialog.setCanceledOnTouchOutside(true);

        return dialog;
    }
}
