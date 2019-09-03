package com.droi.aiui.widget;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.droi.aiui.Interface.IonDialogRemindInfoClickListen;
import com.droi.aiui.R;
import com.droi.aiui.bean.RemindDataTime;
import com.droi.aiui.bean.RemindInfo;
import com.droi.aiui.util.FunctionUtil;


/**
 * Created by cuixiaojun on 18-1-18.
 */

public class DialogRemindInfo extends Dialog implements View.OnClickListener{

    private final String TAG = "DialogRemindInfo";
    private IonDialogRemindInfoClickListen dialogRemindInfoClickListen;
    private RemindInfo mRemindInfo;
    private RemindDataTime remindDataTime;
    private TextView date,week,content,time;
    private Button delete,cancel;

    public DialogRemindInfo( Context context) {
        super(context, R.style.common_dialog);

        findView(context);
        setCanceledOnTouchOutside(false);
        //出现时外部变暗
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        attributes.dimAmount = 0.5f;
        // 设置位置
        attributes.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        attributes.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        attributes.gravity = Gravity.CENTER;
        getWindow().setAttributes(attributes);
    }

    /**
     * 初始化视图
     * @param context
     */
    private void findView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_remind_info,null);
        setContentView(view);
        date = (TextView)view.findViewById(R.id.tv_date);
        week = (TextView)view.findViewById(R.id.tv_week);
        content = (TextView)view.findViewById(R.id.tv_content);
        time = (TextView)view.findViewById(R.id.tv_time);
        delete = (Button)view.findViewById(R.id.bt_delete);
        cancel = (Button)view.findViewById(R.id.bt_cancel);
        delete.setOnClickListener(this);
        cancel.setOnClickListener(this);
    }

    /**
     * 设置当前的remindinfo
     */
    public void showCurrentRemindInfo(RemindInfo remindInfo){
        mRemindInfo = remindInfo;
        if(mRemindInfo != null){
            content.setText(mRemindInfo.getContent());
            remindDataTime = FunctionUtil.getRemindDateTime(mRemindInfo.getTime());
        }
        if(remindDataTime != null){
            date.setText(remindDataTime.getYear()+remindDataTime.getDay());
            week.setText(remindDataTime.getWeek());
            time.setText(remindDataTime.getTime());
        }
        Log.d(TAG,"DialogRemindInfo--->remindInfo===="+mRemindInfo.toString()+",remindDataTime = "+remindDataTime.toString());
        show();
    }

    @Override
    public void onClick(View view) {
        if (view.getId()==R.id.bt_delete) {
         if (dialogRemindInfoClickListen!=null){
             dialogRemindInfoClickListen.onRemindInfoDel();
         }
         dismiss();
        }else if(view.getId()==R.id.bt_cancel) {
            dismiss();
        }
    }

    public void setDialogRemindInfoClickListen(IonDialogRemindInfoClickListen dialogRemindInfoClickListen) {
        this.dialogRemindInfoClickListen = dialogRemindInfoClickListen;
    }

}