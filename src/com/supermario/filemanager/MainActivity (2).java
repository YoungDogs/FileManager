package com.supermario.filemanager;

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
			mMap.put("image", imageResourceArray[i]);
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
					menuPosition = 1;
					 initFileListInfo(mRootPath);
					break;
				//回到SD卡根目录
				case 1:
					menuPosition = 2;
			        initFileListInfo(mSDCard);
					break;
				//显示搜索对话框
				case 2:
					//searchDilalog();
					break;
				//创建文件夹
				case 3:
					createFolder();
					break;
				//粘贴文件
				case 4:
					palseFile();
					break;
				//退出
				case 5:
					MainActivity.this.finish();
					break;
				}
			}  		
    	});
    }
    
    //用静态变量存储 当前目录路径信息
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
    
    //长按列表项的事件监听:对长按需要进行一个控制，当列表中包括”返回根目录“和”返回上一级“时，需要对这两列进行屏蔽
	  public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int position, long arg3) {
		if(isAddBackUp == true){//说明存在返回根目录和返回上一级两列，接下来要对这两列进行屏蔽
			if(position != 0 && position != 1){
				initItemLongClickListener(new File(mFilePaths.get(position)));
			}
		}
		if(mCurrentFilePath.equals(mRootPath)||mCurrentFilePath.equals(mSDCard)){
			initItemLongClickListener(new File(mFilePaths.get(position)));
		}
		return false;
	  }
    /**列表项点击时的事件监听*/
    @Override
     protected void onListItemClick(ListView listView, View view, int position, long id){
    	final File mFile = new File(mFilePaths.get(position));
    	//如果该文件是可读的，我们进去查看文件
    	if(mFile.canRead()){
    		if(mFile.isDirectory()){
    			//如果是文件夹，则直接进入该文件夹，查看文件目录
    			initFileListInfo(mFilePaths.get(position));
    		}else{
    			//如果是文件，则用相应的打开方式打开
    			String fileName = mFile.getName();
    	    	String  fileEnds = fileName.substring(fileName.lastIndexOf(".")+1,fileName.length()).toLowerCase();
    			if(fileEnds.equals("txt")){
    				//显示进度条，表示正在读取
					initProgressDialog(ProgressDialog.STYLE_HORIZONTAL);
    				new Thread(new Runnable(){
    					public void run(){
    						//打开文本文件
    						openTxtFile(mFile.getPath());
    					}
    				}).start();
    				new Thread(new Runnable(){
    					public void run(){
    						while(true){
    							if(isTxtDataOk == true){
    								//关闭进度条
    								mProgressDialog.dismiss();
    								executeIntent(txtData.toString(),mFile.getPath());
    								break;
    							}
    							if(isCancleProgressDialog == true){
    								//关闭进度条
    								mProgressDialog.dismiss();
    								break;
    							}
    						}
    					}
    				}).start();
    				//如果是html文件则用自己写的工具打开
    			} else if(fileEnds.equals("html")||fileEnds.equals("mht")||fileEnds.equals("htm")){
    				Intent intent = new Intent(MainActivity.this,WebActivity.class);
    				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    				intent.putExtra("filePath", mFile.getPath());
    				startActivity(intent);
    			} else {
    				openFile(mFile);
				}
    		}
    	}else{
    		//如果该文件不可读，我们给出提示不能访问，防止用户操作系统文件造成系统崩溃等
    		Toast.makeText(MainActivity.this, "对不起，您的访问权限不足!", Toast.LENGTH_SHORT).show();
    	}
     }
     private String mCopyFileName;
     private boolean isCopy = false;
	   /**长按文件或文件夹时弹出的带ListView效果的功能菜单*/
	   private void initItemLongClickListener(final File file){
		OnClickListener listener = new DialogInterface.OnClickListener(){
			//item的值就是从0开始的索引值(从列表的第一项开始)
			public void onClick(DialogInterface dialog, int item) {
				if(file.canRead()){//注意，所有对文件的操作必须是在该文件可读的情况下才可以，否则报错
					if(item == 0){//复制
						if(file.isFile()&&"txt".equals((file.getName().substring(file.getName().lastIndexOf(".")+1, file.getName().length())).toLowerCase())){
							Toast.makeText(MainActivity.this, "已复制!", Toast.LENGTH_SHORT).show();
							//复制标志位，表明已复制文件
							isCopy = true;
							//取得复制文件的名字
							mCopyFileName = file.getName();
							//记录复制文件的路径
							mOldFilePath = mCurrentFilePath+java.io.File.separator+mCopyFileName;
						}else{
							Toast.makeText(MainActivity.this, "对不起,目前只支持复制文本文件!", Toast.LENGTH_SHORT).show();
						}
					}else if(item == 1){//重命名
						//initRenameDialog(file);
					}else if(item == 2){//删除
						//initDeleteDialog(file);
					}
				}else{
					Toast.makeText(MainActivity.this, "对不起，您的访问权限不足!", Toast.LENGTH_SHORT).show();
				}
			}	
    	};
    	//列表项名称
    	String[] mMenu = {"复制","重命名","删除"};
    	//显示操作选择对话框
    	new AlertDialog.Builder(MainActivity.this)
    								.setTitle("请选择操作!")
    								.setItems(mMenu, listener)
    								.setPositiveButton("取消",null).show();
	   }

     /**调用系统的方法，来打开文件的方法*/
    private void openFile(File file){
    	if(file.isDirectory()){
    		initFileListInfo(file.getPath());
    	}else{
    		Intent intent = new Intent();
        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	intent.setAction(android.content.Intent.ACTION_VIEW);
        	//设置当前文件类型
        	intent.setDataAndType(Uri.fromFile(file), getMIMEType(file));
        	startActivity(intent);
    	}
    }    String txtData = "";
    boolean isTxtDataOk = false;
    //打开文本文件的方法之读取文件数据
    private void openTxtFile(String file){
    	isTxtDataOk = false;
    	try {
			FileInputStream fis = new FileInputStream(new File(file));
			StringBuilder mSb = new StringBuilder(); 
			int m;
			//读取文本文件内容
			while((m = fis.read()) != -1){
				mSb.append((char)m);
			}
			fis.close();
			//保存读取到的数据
			txtData = mSb.toString();
			//读取完毕
			isTxtDataOk = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    /**获得MIME类型的方法*/
    private String getMIMEType(File file){
    	String type = "";
    	String fileName = file.getName();
    	//取出文件后缀名并转成小写
    	String  fileEnds = fileName.substring(fileName.lastIndexOf(".")+1,fileName.length()).toLowerCase();
    	if(fileEnds.equals("m4a")||fileEnds.equals("mp3")||fileEnds.equals("mid")||fileEnds.equals("xmf")||fileEnds.equals("ogg")||fileEnds.equals("wav")){
    		type = "audio/*";// 系统将列出所有可能打开音频文件的程序选择器
    	}else if(fileEnds.equals("3gp")||fileEnds.equals("mp4")){
    		type = "video/*";// 系统将列出所有可能打开视频文件的程序选择器
    	}else if(fileEnds.equals("jpg")||fileEnds.equals("gif")||fileEnds.equals("png")||fileEnds.equals("jpeg")||fileEnds.equals("bmp")){
    		type = "image/*";// 系统将列出所有可能打开图片文件的程序选择器
    	}else{
    		type = "*/*"; // 系统将列出所有可能打开该文件的程序选择器
    	}
    	return type;
    }
    
    //执行Intent跳转的方法
    private void executeIntent(String data,String file){   	
    	Intent intent = new Intent(MainActivity.this,EditTxtActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		//传递文件的路径，标题和内容
		intent.putExtra("path", file);
		intent.putExtra("title", new File(file).getName());
		intent.putExtra("data", data.toString());
		//跳转到EditTxtActivity
		startActivity(intent);
    }
    
    //自定义Adapter内部类
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
    
    //进度条  
    ProgressDialog mProgressDialog;
    boolean isCancleProgressDialog = false;
    /**弹出正在解析文本数据的ProgressDialog*/
    private void initProgressDialog(int style){
    	isCancleProgressDialog = false;
    	mProgressDialog = new ProgressDialog(this);
    	mProgressDialog.setTitle("提示");
    	mProgressDialog.setMessage("正在为你解析文本数据，请稍后...");
    	mProgressDialog.setCancelable(true);
    	mProgressDialog.setButton("取消", new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface arg0, int arg1) {
				isCancleProgressDialog = true;
				mProgressDialog.dismiss();
			}   		
    	});
    	mProgressDialog.show();
    }
    
    private int i;
    FileInputStream fis;
	  FileOutputStream fos;
    //复制文件
    private void copyFile(String oldFile,String newFile){
    	try {
			fis =  new FileInputStream(oldFile);
			fos = new FileOutputStream(newFile);
			do{
				//逐个byte读取文件，并写入另一个文件中
				if((i = fis.read()) != -1){
					fos.write(i);
				}
			}while(i != -1);
			//关闭输入文件流
			if(fis != null){
				fis.close();
			}
			//关闭输出文件流
			if(fos != null){
				fos.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    /**粘贴*/
    private void palseFile(){
    	mNewFilePath = mCurrentFilePath+java.io.File.separator+mCopyFileName;//得到新路径
		Log.d("copy", "mOldFilePath is "+mOldFilePath+"| mNewFilePath is "+mNewFilePath+"| isCopy is "+isCopy);
		if(!mOldFilePath.equals(mNewFilePath)&&isCopy == true){//在不同路径下复制才起效
			if(!new File(mNewFilePath).exists()){
				copyFile(mOldFilePath,mNewFilePath);
				Toast.makeText(MainActivity.this, "执行了粘贴", Toast.LENGTH_SHORT).show();
				initFileListInfo(mCurrentFilePath);
			}else{
				new AlertDialog.Builder(MainActivity.this)
				.setTitle("提示!")
				.setMessage("该文件名已存在，是否要覆盖?")
				.setPositiveButton("确定", new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog,int which){
						copyFile(mOldFilePath,mNewFilePath);
						initFileListInfo(mCurrentFilePath);
					}
				})
				.setNegativeButton("取消", null).show();
			}
		}else{
			Toast.makeText(MainActivity.this, "未复制文件！", Toast.LENGTH_LONG).show();
		}
    }
}