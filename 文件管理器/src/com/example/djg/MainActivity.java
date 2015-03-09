package com.example.djg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class MainActivity extends ListActivity implements OnItemLongClickListener{
    // 声明成员变量：
	//存放显示的文件列表的名称
	private List<String> mFileName = null;
	//存放显示的文件列表的相对应的路径
	private List<String> mFilePaths = null;
	//起始目录“/” 
	private String mRootPath = java.io.File.separator;
	// SD卡根目录
	private String mSDCard = Environment.getExternalStorageDirectory().toString();
	private String mOldFilePath = "";
	private String mNewFilePath = "";
	private String keyWords;
	//用于显示当前路径
	private TextView mPath;
	//用于放置工具栏
	private GridView mGridViewToolbar;
	private int[] girdview_menu_image = {R.drawable.menu_phone,R.drawable.menu_sdcard,R.drawable.menu_search,
														R.drawable.menu_create,R.drawable.menu_palse,R.drawable.menu_exit};
	private String[] gridview_menu_title = {"手机","SD卡","搜索","创建","粘贴","退出"};
	// 代表手机或SD卡，1代表手机，2代表SD卡
	private static int menuPosition = 1;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //初始化菜单视图
        initGridViewMenu();
        //初始化菜单监听器
        initMenuListener();
        //为列表项绑定长按监听器
        getListView().setOnItemLongClickListener(this);
        mPath = (TextView)findViewById(R.id.mPath);
        //一开始程序的时候加载手机目录下的文件列表
		 initFileListInfo(mRootPath);
    }
    
    /**为GridView配饰菜单资源*/
    private void initGridViewMenu(){
    	 mGridViewToolbar = (GridView)findViewById(R.id.file_gridview_toolbar);
    	 //设置选中时候的背景图片
         mGridViewToolbar.setSelector(R.drawable.menu_item_selected);
         //设置背景图片
         mGridViewToolbar.setBackgroundResource(R.drawable.menu_background);
         //设置列数
         mGridViewToolbar.setNumColumns(6);
         //设置剧中对齐
         mGridViewToolbar.setGravity(Gravity.CENTER);
         //设置水平，垂直间距为10
         mGridViewToolbar.setVerticalSpacing(10);
         mGridViewToolbar.setHorizontalSpacing(10);
         //设置适配器
         mGridViewToolbar.setAdapter(getMenuAdapter(gridview_menu_title,girdview_menu_image));
    }
    
    /**菜单适配器*/
    private SimpleAdapter getMenuAdapter(String[] menuNameArray,
			int[] imageResourceArray) {
    	//数组列表用于存放映射表
		ArrayList<HashMap<String, Object>> mData = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < menuNameArray.length; i++) {
			HashMap<String, Object> mMap = new HashMap<String, Object>();
			//将“image”映射成图片资源
			//mMap.put("image", imageResourceArray[i]);
			//将“title”映射成标题
			mMap.put("title", menuNameArray[i]);		
			mData.add(mMap);
		}
		//新建简单适配器，设置适配器的布局文件，映射关系
		SimpleAdapter mAdapter = new SimpleAdapter(this, mData,R.layout.item_menu, new String[] { "image", "title" },new int[] { R.id.item_image, R.id.item_text });
		return mAdapter;
	}
    
    /**菜单项的监听*/
    protected void initMenuListener(){
    	mGridViewToolbar.setOnItemClickListener(new OnItemClickListener(){

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				switch(arg2){
				//回到根目录
				case 0:
					//menuPosition = 1;
					// initFileListInfo(mRootPath);
					break;
				//回到SD卡根目录
				case 1:
					//menuPosition = 2;
			       // initFileListInfo(mSDCard);
					break;
				//显示搜索对话框
				case 2:
					//searchDilalog();
					break;
				//创建文件夹
				case 3:
					//createFolder();
					break;
				//粘贴文件
				case 4:
					//palseFile();
					break;
				//退出
				case 5:
					MainActivity.this.finish();
					break;
				}
			}  		
    	});
    }
    /**粘贴*/
   
    public static String mCurrentFilePath = "";
    /**根据给定的一个文件夹路径字符串遍历出这个文
     * 件夹中包含的文件名称并配置到ListView列表中*/
    private void initFileListInfo(String filePath){
    	isAddBackUp = false;
    	mCurrentFilePath = filePath;
    	//显示当前的路径
    	mPath.setText(filePath);
    	mFileName = new ArrayList<String>();
    	mFilePaths = new ArrayList<String>();
    	File mFile = new File(filePath);
    	//遍历出该文件夹路径下的所有文件/文件夹
    	File[] mFiles = mFile.listFiles();
    	//只要当前路径不是手机根目录或者是sd卡根目录则显示“返回根目录”和“返回上一级”
    	if(menuPosition == 1&&!mCurrentFilePath.equals(mRootPath)){
    		initAddBackUp(filePath,mRootPath);
    	}else if(menuPosition == 2&&!mCurrentFilePath.equals(mSDCard)){
        	initAddBackUp(filePath,mSDCard);
    	}
    	
    	/*将所有文件信息添加到集合中*/
    	for(File mCurrentFile:mFiles){
    		mFileName.add(mCurrentFile.getName());
    		mFilePaths.add(mCurrentFile.getPath());
    	}
    	
    	/*适配数据*/
    	setListAdapter(new FileAdapter(MainActivity.this,mFileName,mFilePaths));
    }
    
    private boolean isAddBackUp = false;
    /**根据点击“手机”还是“SD卡”来加“返回根目录”和“返回上一级”*/
    private void initAddBackUp(String filePath,String phone_sdcard){
    	
    	if(!filePath.equals(phone_sdcard)){
    		/*列表项的第一项设置为返回根目录*/
    		mFileName.add("BacktoRoot");
    		mFilePaths.add(phone_sdcard);
    		/*列表项的第二项设置为返回上一级*/
    		mFileName.add("BacktoUp");
    		//回到当前目录的父目录即回到上级
    		mFilePaths.add(new File(filePath).getParent());
    		//将添加返回按键标识位置为true
    		isAddBackUp = true;
    	}
    	
    }

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO 自动生成的方法存根
		return false;
	}
    
	 class FileAdapter extends BaseAdapter{
	    	//返回键，各种格式的文件的图标
	    	private Bitmap mBackRoot;
	    	private Bitmap mBackUp;
	    	private Bitmap mImage;
	    	private Bitmap mAudio;
	    	private Bitmap mRar;
	    	private Bitmap mVideo;
	    	private Bitmap mFolder;
	    	private Bitmap mApk;
	    	private Bitmap mOthers;
	    	private Bitmap mTxt;
	    	private Bitmap mWeb;
	    	
	    	private Context mContext;
	    	//文件名列表
	    	private List<String> mFileNameList;
	    	//文件对应的路径列表
	    	private List<String> mFilePathList;
	    	
	    	public FileAdapter(Context context,List<String> fileName,List<String> filePath){
	    		mContext = context;
	    		mFileNameList = fileName;
	    		mFilePathList = filePath;
	    		//初始化图片资源
	    		//返回到根目录
	    		mBackRoot = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.back_to_root);
	    		//返回到上一级目录
	    		mBackUp = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.back_to_up);
	    		//图片文件对应的icon
	    		mImage = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.image);
	    		//音频文件对应的icon
	    		mAudio = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.audio);
	    		//视频文件对应的icon
	    		mVideo = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.video);
	    		//可执行文件对应的icon
	    		mApk = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.apk);
	    		//文本文档对应的icon
	    		mTxt = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.txt);
	    		//其他类型文件对应的icon
	    		mOthers = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.others);
	    		//文件夹对应的icon
	    		mFolder = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.folder);
	    		//zip文件对应的icon
	    		mRar = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.zip_icon);
	    		//网页文件对应的icon
	    		mWeb = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.web_browser);
	    	}
	    	//获得文件的总数
			public int getCount() {
				return mFilePathList.size();
			}
			//获得当前位置对应的文件名
			public Object getItem(int position) {
				return mFileNameList.get(position);
			}
			//获得当前的位置
			public long getItemId(int position) {
				return position;
			}
			//获得视图
			public View getView(int position, View convertView, ViewGroup viewgroup) {
				ViewHolder viewHolder = null;
				if (convertView == null) {
					viewHolder = new ViewHolder();
					LayoutInflater mLI = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					//初始化列表元素界面
					convertView = mLI.inflate(R.layout.list_child, null);
					//获取列表布局界面元素
					viewHolder.mIV = (ImageView)convertView.findViewById(R.id.image_list_childs);
					viewHolder.mTV = (TextView)convertView.findViewById(R.id.text_list_childs);
					//将每一行的元素集合设置成标签
					convertView.setTag(viewHolder);
				} else {
					//获取视图标签
					viewHolder = (ViewHolder) convertView.getTag();
				}
				File mFile = new File(mFilePathList.get(position).toString());
				//如果
				if(mFileNameList.get(position).toString().equals("BacktoRoot")){
					//添加返回根目录的按钮
					viewHolder.mIV.setImageBitmap(mBackRoot);
					viewHolder.mTV.setText("返回根目录");
				}else if(mFileNameList.get(position).toString().equals("BacktoUp")){
					//添加返回上一级菜单的按钮
					viewHolder.mIV.setImageBitmap(mBackUp);
					viewHolder.mTV.setText("返回上一级");
				}else if(mFileNameList.get(position).toString().equals("BacktoSearchBefore")){
					//添加返回搜索之前目录的按钮
					viewHolder.mIV.setImageBitmap(mBackRoot);
					viewHolder.mTV.setText("返回搜索之前目录");
				}else{
					String fileName = mFile.getName();
					viewHolder.mTV.setText(fileName);
					if(mFile.isDirectory()){
						viewHolder.mIV.setImageBitmap(mFolder);
					}else{
				    	String fileEnds = fileName.substring(fileName.lastIndexOf(".")+1,fileName.length()).toLowerCase();//取出文件后缀名并转成小写
				    	if(fileEnds.equals("m4a")||fileEnds.equals("mp3")||fileEnds.equals("mid")||fileEnds.equals("xmf")||fileEnds.equals("ogg")||fileEnds.equals("wav")){
				    		viewHolder.mIV.setImageBitmap(mVideo);
				    	}else if(fileEnds.equals("3gp")||fileEnds.equals("mp4")){
				    		viewHolder.mIV.setImageBitmap(mAudio);
				    	}else if(fileEnds.equals("jpg")||fileEnds.equals("gif")||fileEnds.equals("png")||fileEnds.equals("jpeg")||fileEnds.equals("bmp")){
				    		viewHolder.mIV.setImageBitmap(mImage);
				    	}else if(fileEnds.equals("apk")){
				    		viewHolder.mIV.setImageBitmap(mApk);
				    	}else if(fileEnds.equals("txt")){
				    		viewHolder.mIV.setImageBitmap(mTxt);
				    	}else if(fileEnds.equals("zip")||fileEnds.equals("rar")){
				    		viewHolder.mIV.setImageBitmap(mRar);
				    	}else if(fileEnds.equals("html")||fileEnds.equals("htm")||fileEnds.equals("mht")){
				    		viewHolder.mIV.setImageBitmap(mWeb);
				    	}else {
				    		viewHolder.mIV.setImageBitmap(mOthers);
				    	}
					}				
				}
				return convertView;
			}
	    	//用于存储列表每一行元素的图片和文本
			class ViewHolder {
				ImageView mIV;
				TextView mTV;
			}
	    }
	    
	    Intent serviceIntent;
	    ServiceConnection mSC;
	    RadioGroup mRadioGroup;
	    static int mRadioChecked;
	    public static final String KEYWORD_BROADCAST = "com.supermario.file.KEYWORD_BROADCAST";
	    //显示搜索对话框
	    private void searchDilalog(){
	    	//用于确定是在当前目录搜索或者是在整个目录搜索的标志
	    	mRadioChecked = 1;
	    	LayoutInflater mLI = LayoutInflater.from(MainActivity.this);
	    	final View mLL = (View)mLI.inflate(R.layout.serach_dialog, null);
	    	mRadioGroup = (RadioGroup)mLL.findViewById(R.id.radiogroup_search);
	    	final RadioButton mCurrentPathButton = (RadioButton)mLL.findViewById(R.id.radio_currentpath);
	    	final RadioButton mWholePathButton = (RadioButton)mLL.findViewById(R.id.radio_wholepath);
	    	//设置默认选择在当前路径搜索
	    	mCurrentPathButton.setChecked(true);
	    	mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
	    		//当选择改变时触发
				public void onCheckedChanged(RadioGroup radiogroup, int checkId) {
					//当前路径的标志为1
					if(checkId == mCurrentPathButton.getId()){
						mRadioChecked = 1;
						//整个目录的标志为2
					}else if(checkId == mWholePathButton.getId()){
						mRadioChecked = 2;
					}
				}	
	    	});
	    	Builder mBuilder = new AlertDialog.Builder(MainActivity.this)
	    								.setTitle("搜索").setView(mLL)
	    								.setPositiveButton("确定", new OnClickListener(){
											public void onClick(DialogInterface arg0, int arg1) {
												keyWords = ((EditText)mLL.findViewById(R.id.edit_search)).getText().toString();
												if(keyWords.length() == 0){
													Toast.makeText(MainActivity.this, "关键字不能为空!", Toast.LENGTH_SHORT).show();
													searchDilalog();
												}else{
													if(menuPosition == 1){
														mPath.setText(mRootPath);
													}else{
														mPath.setText(mSDCard);
													}
													//获取用户输入的关键字并发送广播-开始
													Intent keywordIntent = new Intent();
													keywordIntent.setAction(KEYWORD_BROADCAST);
													//传递搜索的范围区间:1.当前路径下搜索 2.SD卡下搜索
													if(mRadioChecked == 1){
														keywordIntent.putExtra("searchpath", mCurrentFilePath);
													}else{
														keywordIntent.putExtra("searchpath", mSDCard);
													}
													//传递关键字
													keywordIntent.putExtra("keyword", keyWords);
													//到这里为止是携带关键字信息并发送了广播，会在Service服务当中接收该广播并提取关键字进行搜索
													getApplicationContext().sendBroadcast(keywordIntent);
													//获取用户输入的关键字并发送广播-结束
													serviceIntent = new Intent("com.android.service.FILE_SEARCH_START");
													MainActivity.this.startService(serviceIntent);//开启服务，启动搜索
													//isComeBackFromNotification = false;
												}
											}
	    								}).setNegativeButton("取消", null);
	    	mBuilder.create().show();
	    }
	    
	    /**注册广播*/
	    private IntentFilter mFilter;
	   // private FileBroadcast mFileBroadcast;
	    private IntentFilter mIntentFilter;
		//private SearchBroadCast mServiceBroadCast;
		@Override
	    protected void onStart() {
	    	super.onStart();
	    	mFilter = new IntentFilter();
	    	//mFilter.addAction(FileService.FILE_SEARCH_COMPLETED);
	    	//mFilter.addAction(FileService.FILE_NOTIFICATION);
	    	mIntentFilter = new IntentFilter();
			mIntentFilter.addAction(KEYWORD_BROADCAST);
//	    	if(mFileBroadcast == null){
//	    		mFileBroadcast = new FileBroadcast();
//	    	}
//	    	if(mServiceBroadCast == null){
//	    		mServiceBroadCast = new SearchBroadCast();
//	    	}
//	    	this.registerReceiver(mFileBroadcast, mFilter);
//	    	this.registerReceiver(mServiceBroadCast, mIntentFilter);
	    }

}
  