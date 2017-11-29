package view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.itheima.mobilesafe.R;

/**
 * Created by Rayn on 2017/5/25 0025.
 */

public class ProcessItemView extends LinearLayout{
    private TextView tvLeft;
    private TextView tvMiddle;
    private TextView tvRight;
    private ProgressBar progressBar;

    public ProcessItemView(Context context) {
        this(context,null);
    }
    public ProcessItemView(Context context, AttributeSet attrs) {
        this(context, attrs,-1);
    }
    public ProcessItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    private void initView() {
        View view = View.inflate(getContext(), R.layout.layout_progress_item_view,null);
        addView(view);

        tvLeft = (TextView) view.findViewById(R.id.tv_left);
        tvMiddle = (TextView) view.findViewById(R.id.tv_middle);
        tvRight = (TextView) view.findViewById(R.id.tv_right);
        progressBar = (ProgressBar) view.findViewById(R.id.progressbar);
    }

    //修改左侧文本内容
    public void setTvLeft(String leftString) {
        tvLeft.setText(leftString);
    }
    //修改中间文本内容
    public void setTvMiddle(String middleString) {
        tvMiddle.setText(middleString);
    }
    //修改右侧文本内容
    public void setTvRight(String rightString) {
        tvRight.setText(rightString);
    }
    //修改进度条百分比
    public void setProgress(int progress) {
        progressBar.setProgress(progress);
    }
}
