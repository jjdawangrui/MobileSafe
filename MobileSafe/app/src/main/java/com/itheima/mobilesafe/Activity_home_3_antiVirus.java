package com.itheima.mobilesafe;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.ArcProgress;

import java.util.ArrayList;
import java.util.List;

import bean.VirusBean;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import db.VirusDao;
import utils.MD5Utils;


public class Activity_home_3_antiVirus extends Activity {
    @InjectView(R.id.arc_progress)
    ArcProgress arcProgress;
    @InjectView(R.id.tv_packagename)
    TextView tvPackagename;
    @InjectView(R.id.rl_progress)
    RelativeLayout rlProgress;
    @InjectView(R.id.tv_safe)
    TextView tvSafe;
    @InjectView(R.id.btn_safe)
    Button btnSafe;
    @InjectView(R.id.ll_safe)
    LinearLayout llSafe;
    @InjectView(R.id.iv_left)
    ImageView ivLeft;
    @InjectView(R.id.iv_right)
    ImageView ivRight;
    @InjectView(R.id.ll_anim)
    LinearLayout llAnim;
    @InjectView(R.id.lv_list)
    ListView lvList;
    private List<VirusBean> appList;
    private MyAdapter myAdapter;
    private PackageManager packageManager;
    private ScanTask scanTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_3_antivirus);
        ButterKnife.inject(this);

        initData();
    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        //android:configChanges="keyboardHidden|orientation|screenSize"
    //如果配置以上属性在activity清单文件中，则横竖屏幕切换的时候不调用oncreate调用onConfigurationChanged方法
//        super.onConfigurationChanged(newConfig);
//    }

    private void initData() {
        //AsyncTask（异步任务）
        startTask();
    }

    /**
     * 开启ScanTask异步任务
     */
    private void startTask() {
        new ScanTask().execute("doInBackground 接收此参数");//不是并发
        //execute方法中可以传递参数，此处传递的参数就是doInBackground方法接收的参数
//        scanTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"doInBackground 接收此参数");//并发开启任务
    }

    @OnClick(R.id.btn_safe)
    public void onClick() {
        //在点击事件中开启关门动画
        startCloseAnimation();
    }

    /**
     * 指定扫描完成后的开门动画
     */
    private void startOpenAnimation() {
        //属性动画集合
        AnimatorSet animatorSet = new AnimatorSet();
        //左边的图片（由右向左，向X轴负方向平移）
        ObjectAnimator oa1 = ObjectAnimator.ofFloat(ivLeft, "translationX", 0, -ivLeft.getWidth());
        ObjectAnimator oa2 = ObjectAnimator.ofFloat(ivLeft, "alpha", 1.0f, 0.0f);//1是不透明，0是透明

        //右侧图片
        ObjectAnimator oa3 = ObjectAnimator.ofFloat(ivRight, "translationX", 0, ivRight.getWidth());
        ObjectAnimator oa4 = ObjectAnimator.ofFloat(ivRight, "alpha", 1.0f, 0.0f);

        //扫描完成view淡入效果
        ObjectAnimator oa5 = ObjectAnimator.ofFloat(llSafe, "alpha", 0.0f, 1.0f);

        //让多个动画一起执行的方法
        animatorSet.playTogether(oa1, oa2, oa3, oa4, oa5);
        animatorSet.setDuration(2000);

        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                btnSafe.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                btnSafe.setEnabled(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        animatorSet.start();
    }

    private void startCloseAnimation() {
        //属性动画集合
        AnimatorSet animatorSet = new AnimatorSet();
        //左边的图片（由右向左，向X轴负方向平移）
        ObjectAnimator oa1 = ObjectAnimator.ofFloat(ivLeft, "translationX", -ivLeft.getWidth(), 0);
        ObjectAnimator oa2 = ObjectAnimator.ofFloat(ivLeft, "alpha", 0.0f, 1.0f);

        //右侧图片
        ObjectAnimator oa3 = ObjectAnimator.ofFloat(ivRight, "translationX", ivRight.getWidth(), 0);
        ObjectAnimator oa4 = ObjectAnimator.ofFloat(ivRight, "alpha", 0.0f, 1.0f);

        //扫描完成view淡入效果
        ObjectAnimator oa5 = ObjectAnimator.ofFloat(llSafe, "alpha", 1.0f, 0.0f);

        //让多个动画一起执行的方法
        animatorSet.playTogether(oa1, oa2, oa3, oa4, oa5);
        animatorSet.setDuration(2000);

        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override//在动画开始的时候，重新扫描按钮不可被点击
            public void onAnimationStart(Animator animation) {
                btnSafe.setEnabled(false);
            }

            @Override//动画结束的时候可以
            public void onAnimationEnd(Animator animation) {
                btnSafe.setEnabled(true);
                //关门动画结束以后，重新扫描一次
                startTask();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        animatorSet.start();
    }

    //泛型一：doInBackground参数类型
    //泛型二：publishProgress参数类型
    //泛型三：doInBackground方法的返回值类型
    class ScanTask extends AsyncTask<String, VirusBean, String> {
        //需扫描应用的总数
        private int max = 0;
        //目前扫描到的应用的个数
        private int progress = 0;
        //记录病毒数量变量
        private int virusNum = 0;
        //true 终止 false 继续
        private boolean isStop = false;

        @Override
        protected void onPreExecute() {
            Log.i("", "onPreExecute....");
            //onPreExecute优先于doInBackground方法执行，并且此方法运行在主线程中，操作UI,比如展示进度条
            appList = new ArrayList<>();
            //1.获取手机上安装的所有应用
            packageManager = getPackageManager();

            myAdapter = new MyAdapter();
            lvList.setAdapter(myAdapter);

            //在扫描前，显示进度的view，隐藏动画view，隐藏扫描结果的view
            rlProgress.setVisibility(View.VISIBLE);
            llSafe.setVisibility(View.INVISIBLE);
            llAnim.setVisibility(View.INVISIBLE);

            super.onPreExecute();
        }

        //此方法中定义要执行的后台任务，在这个方法中可以调用publishProgress来更新任务进度（publishProgress内部会调用onProgressUpdate方法）
        //onProgressUpdate(Progress... values) 由publishProgress内部调用，表示任务进度更新
        @Override
        protected String doInBackground(String... params) {
            Log.i("", "doInBackground....");
            Log.i("", params[0]);
            //此方法已经由AsyncTask帮助放置在子线程中，耗时操作，比如请求网络  json
            //doInBackground方法返回的结果，作为onPostExecute方法的参数传递进去
            List<PackageInfo> installedPackages = packageManager.getInstalledPackages(
                    PackageManager.GET_SIGNATURES);
            max = installedPackages.size();
            //2.循环遍历installedPackages集合获取每一个应用信息
            for (PackageInfo packageInfo : installedPackages) {
                if (isStop) {
                    break;
                }
                String packageName = packageInfo.packageName;
                ApplicationInfo applicationInfo = packageInfo.applicationInfo;
                Drawable drawable = applicationInfo.loadIcon(packageManager);
                String name = applicationInfo.loadLabel(packageManager).toString();
                //获取应用签名的数组
                Signature[] signatures = packageInfo.signatures;
                //获取应用签名
                Signature signature = signatures[0];
                //获取签名信息对应的字符串
                String charsString = signature.toCharsString();
                //通过MD5加密算法，对charsString进行加密，获取32位的16进制字符
                String md5Encoder = MD5Utils.encode(charsString);

                Log.i("", "应用名称  name=" + name + "   md5 = " + md5Encoder);
                //判断encode是否存在于数据库中,如果在数据库中，则认为是病毒，否则不是
                boolean isVirus = VirusDao.isVirus(getApplication(), md5Encoder);

                VirusBean bean = new VirusBean(name, drawable, packageName, isVirus);
//                appList.add(bean);

                //不能在此处直接刷新数据适配
                // （原因一：myAdapter是在onPostExecute方法中创建的）
                //(原因二：刷新数据适配器，涉及UI操作，不能在子线程中执行)
//                myAdapter.notifyDataSetChanged();
                publishProgress(bean);//在扫描过程中更新进度条方法，一旦此方法被调用则会转调onProgressUpdate

                SystemClock.sleep(100);
            }
            return null;
        }

        //在扫描完成后，也就是我们的异步任务执行完成，此时，会调用onPostExecute方法，在该方法中让ListView回滚到顶部。
        @Override
        protected void onPostExecute(String s) {
            if (isStop) {
                return;
            }
            Log.i("", "onPostExecute....");
            //后于doInBackground方法执行，并且此方法运行在主线程中，操作UI,比如，隐藏进度条
            myAdapter.notifyDataSetChanged();

            //所有的应用扫描完成后，需要让ListView滚动到最顶部
            lvList.smoothScrollToPosition(0);

            //将扫描完成，进度条是100%图片截取下来
            rlProgress.setDrawingCacheEnabled(true);//开启图片缓存功能，可以被截取图片
            //设置图片是否是高清
            rlProgress.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            //获取需要截取的图片Bitmap
            Bitmap drawingCache = rlProgress.getDrawingCache();

            //截取原图左侧的图片
            Bitmap leftBitmap = getLeftBitmap(drawingCache);
            ivLeft.setImageBitmap(leftBitmap);
            //截取原图右侧的图片
            Bitmap rightBitmap = getRightBitmap(drawingCache);
            ivRight.setImageBitmap(rightBitmap);

            //执行开门动画
            startOpenAnimation();

            //如果扫描完成后，病毒数量大于0
            if (virusNum > 0) {
                //在文本中显示手机不安全
                tvSafe.setText("你的手机不安全");
            } else {
                tvSafe.setText("你的手机安全");
            }

            //扫描完成后，需要执行开门动画，以及显示扫描结果
            rlProgress.setVisibility(View.INVISIBLE);
            llSafe.setVisibility(View.VISIBLE);
            llAnim.setVisibility(View.VISIBLE);

            super.onPostExecute(s);
        }

        //在扫描的过程中，每扫描一个应用，就将这个应用添加在扫描应用的集合中，通知数据适配器刷新
        //一旦数据适配器刷新，则会多一个条目出现，如果这个条目不能够完整的显示，则需要让ListView向上滚动
        //在ScanTask中重写OnProgressUpdate方法，更新数据适配器，显示扫描过程，并且让ListView实现向上滚动效果：
        @Override
        protected void onProgressUpdate(VirusBean... values) {
            if (isStop) {
                return;
            }
            //onProgressUpdate方法接收的参数，就是publishProgress方法传递过来的
            VirusBean value = values[0];
            //如果value是病毒，则将其放置集合的最顶部，不是病毒的顺序排列
            if (value.isVirus()) {
                //每扫描一次，就将扫描过的应用，放置在填充数据适配器的集合中
                virusNum++;
                appList.add(0, value);
            } else {
                appList.add(value);
            }

            //数据适配器需要刷新
            myAdapter.notifyDataSetChanged();
            //如果数据适配器中的条目已经不能显示出来，则需要让listView滚动
            lvList.smoothScrollToPosition(appList.size() - 1);
            //每扫描一次应用，就需要更新进度条，更新显示的扫描应用的包名
            progress++;
            arcProgress.setProgress((int) (progress * 100.0f / max));
            //设置包名
            tvPackagename.setText(value.getPackageName());
            super.onProgressUpdate(values);
        }

        public void stop() {
            //维护一个终止doInBackground循环变量
            isStop = true;
        }
    }

    /**
     * @param drawingCache 原图
     * @return 截取右侧图片
     */
    private Bitmap getRightBitmap(Bitmap drawingCache) {
        //1.指定被截取出来的图片的大小（和原图高度一致，宽度是原图的一半）
        int width = drawingCache.getWidth() / 2;
        int height = drawingCache.getHeight();
        //2.创建一个config对象，图片的配置对象和截取后的图片保存一致
        Bitmap.Config config = drawingCache.getConfig();
        //3.生成一个空白的宽高大小是截取后大小的空白图片
        Bitmap rightBitmap = Bitmap.createBitmap(width, height, config);
        //4.将原图的一半绘制在画布上
        Canvas canvas = new Canvas(rightBitmap);
        //矩阵
        Matrix matrix = new Matrix();
        //需要截取图片的右半边，则需要让图片由右向左移动原图宽度的一半，然后从原点开始截取原图宽度一半的大小
        matrix.setTranslate(-width, 0);
        //画笔
        Paint paint = new Paint();

        //在画布上绘制图片
        canvas.drawBitmap(drawingCache, matrix, paint);
        return rightBitmap;
    }

    /**
     * @param drawingCache 原图
     * @return 从原图上截取坐半边用于返回
     */
    private Bitmap getLeftBitmap(Bitmap drawingCache) {
        //1.指定被截取出来的图片的大小（和原图高度一致，宽度是原图的一半）
        int width = drawingCache.getWidth() / 2;
        int height = drawingCache.getHeight();
        //2.创建一个config对象，图片的配置对象和截取后的图片保存一致
        Bitmap.Config config = drawingCache.getConfig();
        //3.生成一个空白的宽高大小是截取后大小的空白图片
        Bitmap leftBitmap = Bitmap.createBitmap(width, height, config);
        //4.将原图的一半绘制在画布上
        Canvas canvas = new Canvas(leftBitmap);
        //矩阵
        Matrix matrix = new Matrix();
        //画笔
        Paint paint = new Paint();

        //在画布上绘制图片
        canvas.drawBitmap(drawingCache, matrix, paint);
        return leftBitmap;
    }


    class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return appList.size();
        }

        @Override
        public VirusBean getItem(int position) {
            return appList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = View.inflate(getApplicationContext(), R.layout.item_app_info, null);

                viewHolder = new ViewHolder();

                viewHolder.ivAppIcon = (ImageView) convertView.findViewById(R.id.iv_app_icon);
                viewHolder.tvAppName = (TextView) convertView.findViewById(R.id.tv_app_name);
                viewHolder.tvSafe = (TextView) convertView.findViewById(R.id.tv_safe);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            VirusBean bean = getItem(position);
            viewHolder.ivAppIcon.setImageDrawable(bean.getDrawable());
            viewHolder.tvAppName.setText(bean.getName());
            if (bean.isVirus()) {
                viewHolder.tvSafe.setText("病毒");
                viewHolder.tvSafe.setTextColor(Color.RED);
            } else {
                viewHolder.tvSafe.setText("安全");
                viewHolder.tvSafe.setTextColor(Color.GREEN);
            }
            return convertView;
        }
    }

    class ViewHolder {
        ImageView ivAppIcon;
        TextView tvAppName;
        TextView tvSafe;
    }

    @Override
    protected void onDestroy() {
        //取消AsyncTask中的任务
        if (scanTask != null) {
//            scanTask.cancel(true);
            scanTask.stop();
        }
        super.onDestroy();
    }
}
