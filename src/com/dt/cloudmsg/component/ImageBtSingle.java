package com.dt.cloudmsg.component;

import com.dt.cloudmsg.R;

import android.content.Context;  
import android.util.AttributeSet;  
import android.view.LayoutInflater;  
import android.widget.ImageView;   
import android.widget.RelativeLayout;
 
public class ImageBtSingle extends RelativeLayout {  

    private ImageView iv;
  
    public ImageBtSingle(Context context) {  
        this(context, null);  
    }  
  
    public ImageBtSingle(Context context, AttributeSet attrs) {  
        super(context, attrs);  
        // 导入布局  
        LayoutInflater.from(context).inflate(R.layout.imagebt_single, this, true);  
        iv = (ImageView) findViewById(R.id.iv);
    }  
  
    /** 
     * 设置图片资源
     */  
    public void setImageResource(int resId) {  
        iv.setImageResource(resId);  
    }  
  
    //public void setClickable(boolean clickable){
    	
    //}
}  

