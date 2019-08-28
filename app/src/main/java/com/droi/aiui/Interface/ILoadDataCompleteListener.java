package com.droi.aiui.Interface;


import com.droi.aiui.bean.Contact;
import java.util.List;

/**
 * Created by cuixiaojun on 18-3-5.
 */

public interface ILoadDataCompleteListener {
    void loadComplete(List<Contact> contacts);
}