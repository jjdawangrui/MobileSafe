package com.itheima.mobilesafe;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import bean.ProcessInfo;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import engine.ProcessInfoProvider;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import service.LockClearService;
import utils.Constant;
import utils.SPUtils;
import utils.ServiceUtils;
import view.ItemSettingView;
import view.ProcessItemView;

public class Activity_home_2_process extends Activity {

    @InjectView(R.id.tv_title)
    TextView tvTitle;
    @InjectView(R.id.piv_process)
    ProcessItemView pivProcess;
    @InjectView(R.id.piv_memory)
    ProcessItemView pivMemory;
    @InjectView(R.id.imageview_clear)
    ImageView imageviewClear;
    StickyListHeadersListView listView;
    @InjectView(R.id.lv_process_test)
    ListView lvProcessTest;
    @InjectView(R.id.listview)
    StickyListHeadersListView listview;
    @InjectView(R.id.arrow_1)
    ImageView arrow1;
    @InjectView(R.id.arrow_2)
    ImageView arrow2;
    @InjectView(R.id.handle)
    RelativeLayout handle;
    @InjectView(R.id.siv_show_sys_process)
    ItemSettingView sivShowSysProcess;
    @InjectView(R.id.siv_lock_clean)
    ItemSettingView sivLockClean;
    @InjectView(R.id.content)
    LinearLayout content;
    @InjectView(R.id.slidingDrawer)
    SlidingDrawer slidingDrawer;
    private int runningProcess;

    private List<ProcessInfo> runningProcessInfo;//所有进程的集合
    private ArrayList<ProcessInfo> customerList;//
    private ArrayList<ProcessInfo> systemList;
    private LinearLayout progress;//转圈加载
    private boolean showSys;//是否显示系统进程
    private StickyProcessAdapter stickyProcessAdapter;//奇葩api的适配器
    private PopupWindow popupWindow;//弹出消息的类
    private ProcessInfo processInfo;//进程bin

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_2_process);
        ButterKnife.inject(this);
        //奇葩api
        listView = (StickyListHeadersListView) findViewById(R.id.listview);
        //旋转加载
        progress = (LinearLayout) findViewById(R.id.progress);

        //上面两行的初始化
        initTopData();

        //给listview初始化
        new Thread(new Runnable() {
            @Override
            public void run() {
                //1.获取进程信息集合对象
                runningProcessInfo = ProcessInfoProvider.getRunningProcessInfo(Activity_home_2_process.this);
                //2.将集合中的数据分解成2部分（用户，系统）
                customerList = new ArrayList<>();
                systemList = new ArrayList<>();
                //3.将runningProcessInfo中的数据拆分到2个集合（用户进程集合，系统进程集合）中
                for (ProcessInfo processInfo : runningProcessInfo) {
                    if (processInfo.isSys()) {
                        systemList.add(processInfo);
                    } else {
                        customerList.add(processInfo);
                    }
                }
                //4.因为StickyListHeadersListView自定义控件用到的数据适配器，仅仅需要一个集合进行填充，
                // 项目需求是用户进程先展示，系统进程后展示，将用户进程和系统进程放在一个集合中
                // 要将用户进程的数据放在集合的前面，系统进程的数据放在集合的后面
                runningProcessInfo.clear();
                runningProcessInfo.addAll(customerList);
                runningProcessInfo.addAll(systemList);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress.setVisibility(View.GONE);//旋转进度条消失
                        //是否显示系统进程
                        showSys = SPUtils.getBoolean(Activity_home_2_process.this, Constant.SHOW_SYS, true);
                        stickyProcessAdapter = new StickyProcessAdapter();
                        listView.setAdapter(stickyProcessAdapter);
                    }
                });
            }
        }).start();

        boolean isShowSys = SPUtils.getBoolean(this, Constant.SHOW_SYS, true);
        sivShowSysProcess.setFlag(isShowSys);//初始化开关

        //进程item的点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (showSys) {//这个是显示系统进程的开关，如果显示了，那么用的集合就是所有集合
                    ProcessInfo processInfo = runningProcessInfo.get(position);
                    //如果点中的条目，就是本身应用，则不做任何操作
                    if (getPackageName().equals(processInfo.getPackageName())) {
                        return;
                    } else {
                        //如果点中的应用和本应用包名不一致，则状态置反
                        processInfo.isCheck = !processInfo.isCheck;
                        stickyProcessAdapter.notifyDataSetChanged();
                    }
                } else {//如果不显示系统进程，那么用的集合就是用户集合
                    ProcessInfo processInfo = customerList.get(position);
                    if (getPackageName().equals(processInfo.getPackageName())) {
                        return;
                    } else {
                        processInfo.isCheck = !processInfo.isCheck;
                        stickyProcessAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        //开启抽屉上面的动画
        startAnimation();
        slidingDrawer.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener() {
            @Override
            public void onDrawerOpened() {
                //当抽屉被打开的时候调用
                stopAnimation();
            }
        });
        slidingDrawer.setOnDrawerCloseListener(new SlidingDrawer.OnDrawerCloseListener() {
            @Override
            public void onDrawerClosed() {
                //当抽屉被关闭的时候调用
                startAnimation();
            }
        });

        //根据锁屏清理的服务是否开启的状态，决定按钮图片是绿（开启）还是红（关闭）
        boolean running = ServiceUtils.isRunning(this, LockClearService.class);
        sivLockClean.setFlag(running);

        //长按单个条目显示冒泡弹出
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                processInfo = (ProcessInfo) parent.getItemAtPosition(position);
                showPop(view);
                return false;
            }
        });
    }//初始化结束

    //标题内容api的适配器
    class StickyProcessAdapter extends BaseAdapter implements StickyListHeadersAdapter {
        @Override
        public View getHeaderView(int position, View convertView, ViewGroup parent) {
            TitleViewHolder titleViewHolder = null;
            if (convertView == null) {
                titleViewHolder = new TitleViewHolder();
                convertView = View.inflate(getApplicationContext(), R.layout.item_process_text, null);
                titleViewHolder.textViewDes = (TextView) convertView.findViewById(R.id.textview_des);
                convertView.setTag(titleViewHolder);//将标题这个布局放到view上面
            } else {
                titleViewHolder = (TitleViewHolder) convertView.getTag();//如果回收的view上面有东西，那么直接把这个布局拿下来用
            }
            //根据索引获取索引位置对象，每一个对象身上都有一个是否是系统进程的标志
            //如果是系统进程，则统一处理器headerView显示“系统进程（）”
            ProcessInfo processInfo = runningProcessInfo.get(position);
            if (processInfo.isSys()) {
                titleViewHolder.textViewDes.setText("系统进程(" + systemList.size() + ")");
            } else {
                titleViewHolder.textViewDes.setText("用户进程(" + customerList.size() + ")");
            }
            return convertView;
        }
        @Override
        public long getHeaderId(int position) {
            //return 的情况有几种  返回的头就有几个
            ProcessInfo processInfo = getItem(position);
            if (processInfo.isSys()) {
                return 0;
            } else {
                return 1;
            }
        }
        @Override
        public int getCount() {
            if (showSys) {
                //显示系统进程
                return runningProcessInfo.size();
            } else {
                //不显示系统进程，只显示用户进程
                return customerList.size();
            }
        }
        @Override
        public ProcessInfo getItem(int position) {
            if (showSys) {
                return runningProcessInfo.get(position);
            } else {
                return customerList.get(position);
            }
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // 优化 避免重复findViewById
            ViewHolder viewHolder = null;
            // 复用对象
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = View.inflate(getApplicationContext(), R.layout.item_process_text_img, null);
                //将找到的控件存储在viewHolder内部
                viewHolder.imageViewIcon = (ImageView) convertView.findViewById(R.id.iv_icon);
                viewHolder.textViewName = (TextView) convertView.findViewById(R.id.tv_name);
                viewHolder.textViewUsedMemory = (TextView) convertView.findViewById(R.id.tv_used_memory);
                viewHolder.checkbox = (CheckBox) convertView.findViewById(R.id.cb);

                //将viewHolder又存储在convertView内部
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            ProcessInfo processInfo = getItem(position);
            viewHolder.imageViewIcon.setImageDrawable(processInfo.getDrawable());
            viewHolder.textViewName.setText(processInfo.getName());
            String sizeStr = Formatter.formatFileSize(getApplicationContext(), processInfo.getMemorySize());
            viewHolder.textViewUsedMemory.setText("占用内存：" + sizeStr);
            //判断此条目是否被勾选中，这个isCheck是 进程bean 里面的一个属性，每一个进程都有这个属性
            viewHolder.checkbox.setChecked(processInfo.isCheck());
            //本应用是不能被勾选中的，所以将本应用后方的checkbox的控件隐藏掉
            if (getPackageName().equals(processInfo.getPackageName())) {
                //找到了本应用
                viewHolder.checkbox.setVisibility(View.GONE);
            } else {
                viewHolder.checkbox.setVisibility(View.VISIBLE);
            }
            return convertView;
        }
    }
    //标题的整个布局，只有一个TextView
    static class TitleViewHolder {
        TextView textViewDes;
    }

    //内容的布局，上面有4个东西
    static class ViewHolder {
        ImageView imageViewIcon;
        TextView textViewName;
        TextView textViewUsedMemory;
        CheckBox checkbox;
    }

    private void initTopData() {
        initProcess();
        initMemory();
    }

    //抽屉里面的两个开关，还有全选反选，还有右上角的清理按钮
    @OnClick({R.id.siv_show_sys_process, R.id.siv_lock_clean,
            R.id.btn_select_all, R.id.btn_select_reverse,R.id.imageview_clear})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.siv_show_sys_process://显示系统进程开关
                //将原有的状态设置为反，然后操作图片置反
                sivShowSysProcess.reverseFlag();
                //将置反后的状态存储在sp中
                SPUtils.saveBoolean(this, Constant.SHOW_SYS, sivShowSysProcess.getFlag());
                showSys = sivShowSysProcess.getFlag();
                //点击完成后，是否显示系统进程的状态在变化，数据适配器的列表更新
                stickyProcessAdapter.notifyDataSetChanged();
                break;
            case R.id.siv_lock_clean:
                //状态置反，图片置反
                sivLockClean.reverseFlag();
                boolean running = ServiceUtils.isRunning(this, LockClearService.class);
                Intent intent = new Intent(this, LockClearService.class);
                if (running){
                    //点之前服务是开启的，点击后就关闭
                    stopService(intent);
                }else{
                    //点之前服务是关闭的，点击后就开启
                    startService(intent);
                }

            case R.id.btn_select_all://全选按钮
                selectAll();
                break;
            case R.id.btn_select_reverse://反选按钮
                reverseAll();
                break;
            case R.id.imageview_clear:
                killProcess();
                break;
        }
    }


    private void showPop(View view) {
        View popupView = View.inflate(this,R.layout.layout_popup_window,null);
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //给popupWindow设置背景，如果不设置背景则点击回退按钮或者点击非冒泡窗体的位置，冒泡窗体不会消失

        //让冒泡窗体获取焦点，执行隐藏动画
        popupWindow.setFocusable(true);
        //在此处设置的是透明背景
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //设置自定义动画
        popupWindow.setAnimationStyle(R.style.PopupAnimationSytle);

        //让popupWindow（冒泡窗体）显示
        //参数一：冒泡窗体所在的位置在哪个view的下方
        //参数二、三：指定x轴和y轴的偏移量
        //参数四：指定相对位置
        popupWindow.showAsDropDown(view,60,-60);
        //参数一：让popupwindow知道所在的activity是那个，通过传递此activity中的任意一个view即可
//        popupWindow.showAtLocation(view,Gravity.RIGHT|Gravity.TOP,0,50);

        TextView tvUninstall = (TextView) popupView.findViewById(R.id.tv_uninstall);
        TextView tvOpen = (TextView)popupView.findViewById(R.id.tv_open);
        TextView tvShare = (TextView)popupView.findViewById(R.id.tv_share);
        TextView tvInfo = (TextView)popupView.findViewById(R.id.tv_info);

        tvUninstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uninstall();//卸载用户应用，不能卸载系统应用
                hidePopupWindow();
            }
        });
        tvOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                open();
                hidePopupWindow();
            }
        });
        tvShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share();
                hidePopupWindow();
            }
        });
        tvInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                info();
                hidePopupWindow();
            }
        });
    }

    private void info() {
//       通过日志看到以下信息 I/ActivityManager: START u0
//        {act=android.settings.APPLICATION_DETAILS_SETTINGS dat=package:com.android.music
        //通过隐式意图开启系统的信息界面
        Intent intent = new Intent();
        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.setData(Uri.parse("package:"+processInfo.getPackageName()));
        startActivity(intent);
    }

    private void share() {
        //找到项目中可以对外发送文本的一个应用，传递文本
        /*<intent-filter>
        <action android:name="android.intent.action.SEND" />
        <category android:name="android.intent.category.DEFAULT" />
        <data android:mimeType="text/plain" />
        </intent-filter>*/
        Intent intent = new Intent();
        intent.setAction("android.intent.action.SEND");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT,"分享一个应用"+processInfo.getName());
        startActivity(intent);
    }

    private void open() {
        //packageManager开启应用
        if (getPackageName().equals(processInfo.getPackageName())){
            Toast.makeText(this,"应用已经开启了",Toast.LENGTH_SHORT).show();
            return;
        }
        PackageManager packageManager = getPackageManager();
        //通过包名获取launch界面开启一个应用的意图
        Intent intent = packageManager.getLaunchIntentForPackage(processInfo.getPackageName());
        if (intent!=null){
            startActivity(intent);
        }
    }

    public void uninstall(){
        //Package应用源码，安装卸载
        /*<intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <action android:name="android.intent.action.DELETE" />
        <category android:name="android.intent.category.DEFAULT" />
        <data android:scheme="package" />
        </intent-filter>*/
        Intent intent = new Intent();
        intent.setAction("android.intent.action.DELETE");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setData(Uri.parse("package:"+processInfo.getPackageName()));
        startActivity(intent);
    }
    private void hidePopupWindow() {
        if (popupWindow!=null){
            popupWindow.dismiss();
        }
    }

    private void killProcess() {
        //1.判断系统进程是展示隐藏
        List<ProcessInfo> tempProcessList = new ArrayList<>();
        if (showSys){
            //既显示了用户进程又显示了系统进程，所以添加所有进程的集合
            tempProcessList.addAll(runningProcessInfo);
        }else{
            //仅仅显示了用户进程，添加用户进程的集合
            tempProcessList.addAll(customerList);
        }
        //2.判断该杀死的进程有哪些
        int count = 0;//记录杀死进程的总数量
        int releaseTotalMemeory = 0;//释放的内存总和
        for (ProcessInfo processInfo: tempProcessList) {
            //过滤本身应用
            if (processInfo.getPackageName().equals(getPackageName())){
                continue;
            }
            //判断processInfo中的isCheck变量值，如果是true 要杀死 false 不杀死
            if (processInfo.isCheck){
                //要杀死此进程，进程对应的对象就需要移除掉
                //如果进程的对象在页面中的所有进程的集合中，则将其移除掉
                if (runningProcessInfo.contains(processInfo)){
                    runningProcessInfo.remove(processInfo);
                }
                //如果进程的对象在用户进程集合中，则将其移除掉
                if (customerList.contains(processInfo)){
                    customerList.remove(processInfo);
                }
                //如果进程的对象在系统进程集合中，则将其移除掉
                if (systemList.contains(processInfo)){
                    systemList.remove(processInfo);
                }
                count ++;
                //杀死运行在后台的进程，将此方法封装在ProcessInfoProvider
                ProcessInfoProvider.killProcess(this,processInfo.getPackageName());

                //杀死进程以后，计算释放的内存大小
                releaseTotalMemeory += processInfo.getMemorySize();
            }
        }
        //通知数据适配器刷新
        stickyProcessAdapter.notifyDataSetChanged();

        //格式化释放内存大小
        String strReleaseTotalMemeory = Formatter.formatFileSize(this, releaseTotalMemeory);
        //更新进程数和内存使用情况

        runningProcess -= count;
        pivProcess.setTvMiddle("正在运行"+runningProcess+"个");
        initMemory();

        //用吐司显示杀死进程数和释放内存大小
        Toast.makeText(this,"清理进程"+count+"个,释放内存"+strReleaseTotalMemeory,Toast.LENGTH_SHORT).show();
    }

    private void reverseAll() {
        //1.判断当前是否显示了系统进程
        List<ProcessInfo> tempProcessList = new ArrayList<>();
        if (showSys) {
            //显示了系统进程
            tempProcessList = runningProcessInfo;//(包含用户+系统进程集合)
        } else {
            //没显示系统进程，只显示了用户进程
            tempProcessList = customerList;//（用户进程）
        }
        for (ProcessInfo processInfo : tempProcessList) {
            //全选的时候，跳过本应用
            if (processInfo.getPackageName().equals(getPackageName())) {
                continue;
            }
            processInfo.isCheck = !processInfo.isCheck;
        }
        stickyProcessAdapter.notifyDataSetChanged();
    }
    private void selectAll() {
        //1.判断当前是否显示了系统进程
        List<ProcessInfo> tempProcessList;
        if (showSys) {
            //显示了系统进程
            tempProcessList = runningProcessInfo;//(包含用户+系统进程集合)
        } else {
            //没显示系统进程，只显示了用户进程
            tempProcessList = customerList;//（用户进程）
        }
        for (ProcessInfo processInfo : tempProcessList) {
            //全选的时候，跳过本应用
            if (processInfo.getPackageName().equals(getPackageName())) {
                continue;
            }
            processInfo.isCheck = true;
        }
        stickyProcessAdapter.notifyDataSetChanged();
    }

    private void startAnimation() {
        arrow1.setImageResource(R.mipmap.drawer_arrow_up);
        arrow2.setImageResource(R.mipmap.drawer_arrow_up);

        AlphaAnimation alphaAnimation1 = new AlphaAnimation(0.3f, 1.0f);
        alphaAnimation1.setDuration(500);
        alphaAnimation1.setRepeatCount(Animation.INFINITE);//指定动画执行无限次数
        alphaAnimation1.setRepeatMode(Animation.REVERSE);//动画反转执行
        arrow1.startAnimation(alphaAnimation1);

        AlphaAnimation alphaAnimation2 = new AlphaAnimation(1.0f, 0.3f);
        alphaAnimation2.setDuration(500);
        alphaAnimation2.setRepeatCount(Animation.INFINITE);//指定动画执行无限次数
        alphaAnimation2.setRepeatMode(Animation.REVERSE);//动画反转执行
        arrow2.startAnimation(alphaAnimation2);
    }

    private void stopAnimation() {
        arrow1.clearAnimation();
        arrow2.clearAnimation();

        arrow1.setImageResource(R.mipmap.drawer_arrow_down);
        arrow2.setImageResource(R.mipmap.drawer_arrow_down);
    }




    private void initProcess() {
        //1.设置进程数文本内容到自定义组合控件的最左边
        pivProcess.setTvLeft("进程数：");
        //将正在运行进程数的获取，和总进程数获取，已用内存获取，内存总大小获取都放置在Processprovider
        //2.通过引擎类获取正在运行进程数
        runningProcess = ProcessInfoProvider.getRunningProcess(this);
        //3.设置给自定义组合控件中间的textView
        pivProcess.setTvMiddle("正在运行" + runningProcess + "个");
        //4.获取所有可以开启的进程数
        int allProcess = ProcessInfoProvider.getAllProcess(this);
        pivProcess.setTvRight("可有进程" + allProcess);
        //5.指定已用进程在所有的进程中的占比情况
        int processProgress = runningProcess * 100 / allProcess;
        pivProcess.setProgress(processProgress);
    }

    //在ProcessItemView里面定义了一次设置文本方法，任何一个自定义控件都能用了
    private void initMemory() {
        //1.设置内存文本内容到自定义组合控件的最左边
        pivMemory.setTvLeft("内存：");
        //2.获取占用内存大小  手机的所有内存 - 可用的内存 = 占用内存大小
        long allMemory = ProcessInfoProvider.getAllMemory(this);
        long availMemory = ProcessInfoProvider.getAvailMemory(this);
        long usedMemory = allMemory - availMemory;
        //将内存格式化
        String usedMemoryStr = Formatter.formatFileSize(this, usedMemory);
        pivMemory.setTvMiddle("占用内存" + usedMemoryStr);
        //3.可用内存
        String availMemoryStr = Formatter.formatFileSize(this, availMemory);
        pivMemory.setTvRight("可用内存" + availMemoryStr);
        int memoryProgress = (int) (usedMemory * 100 / allMemory);
        pivMemory.setProgress(memoryProgress);
    }
}
