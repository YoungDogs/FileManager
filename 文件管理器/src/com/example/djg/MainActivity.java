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
    // ������Ա������
	//�����ʾ���ļ��б������
	private List<String> mFileName = null;
	//�����ʾ���ļ��б�����Ӧ��·��
	private List<String> mFilePaths = null;
	//��ʼĿ¼��/�� 
	private String mRootPath = java.io.File.separator;
	// SD����Ŀ¼
	private String mSDCard = Environment.getExternalStorageDirectory().toString();
	private String mOldFilePath = "";
	private String mNewFilePath = "";
	private String keyWords;
	//������ʾ��ǰ·��
	private TextView mPath;
	//���ڷ��ù�����
	private GridView mGridViewToolbar;
	private int[] girdview_menu_image = {R.drawable.menu_phone,R.drawable.menu_sdcard,R.drawable.menu_search,
														R.drawable.menu_create,R.drawable.menu_palse,R.drawable.menu_exit};
	private String[] gridview_menu_title = {"�ֻ�","SD��","����","����","ճ��","�˳�"};
	// �����ֻ���SD����1�����ֻ���2����SD��
	private static int menuPosition = 1;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //��ʼ���˵���ͼ
        initGridViewMenu();
        //��ʼ���˵�������
        initMenuListener();
        //Ϊ�б���󶨳���������
        getListView().setOnItemLongClickListener(this);
        mPath = (TextView)findViewById(R.id.mPath);
        //һ��ʼ�����ʱ������ֻ�Ŀ¼�µ��ļ��б�
		 initFileListInfo(mRootPath);
    }
    
    /**ΪGridView���β˵���Դ*/
    private void initGridViewMenu(){
    	 mGridViewToolbar = (GridView)findViewById(R.id.file_gridview_toolbar);
    	 //����ѡ��ʱ��ı���ͼƬ
         mGridViewToolbar.setSelector(R.drawable.menu_item_selected);
         //���ñ���ͼƬ
         mGridViewToolbar.setBackgroundResource(R.drawable.menu_background);
         //��������
         mGridViewToolbar.setNumColumns(6);
         //���þ��ж���
         mGridViewToolbar.setGravity(Gravity.CENTER);
         //����ˮƽ����ֱ���Ϊ10
         mGridViewToolbar.setVerticalSpacing(10);
         mGridViewToolbar.setHorizontalSpacing(10);
         //����������
         mGridViewToolbar.setAdapter(getMenuAdapter(gridview_menu_title,girdview_menu_image));
    }
    
    /**�˵�������*/
    private SimpleAdapter getMenuAdapter(String[] menuNameArray,
			int[] imageResourceArray) {
    	//�����б����ڴ��ӳ���
		ArrayList<HashMap<String, Object>> mData = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < menuNameArray.length; i++) {
			HashMap<String, Object> mMap = new HashMap<String, Object>();
			//����image��ӳ���ͼƬ��Դ
			//mMap.put("image", imageResourceArray[i]);
			//����title��ӳ��ɱ���
			mMap.put("title", menuNameArray[i]);		
			mData.add(mMap);
		}
		//�½����������������������Ĳ����ļ���ӳ���ϵ
		SimpleAdapter mAdapter = new SimpleAdapter(this, mData,R.layout.item_menu, new String[] { "image", "title" },new int[] { R.id.item_image, R.id.item_text });
		return mAdapter;
	}
    
    /**�˵���ļ���*/
    protected void initMenuListener(){
    	mGridViewToolbar.setOnItemClickListener(new OnItemClickListener(){

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				switch(arg2){
				//�ص���Ŀ¼
				case 0:
					//menuPosition = 1;
					// initFileListInfo(mRootPath);
					break;
				//�ص�SD����Ŀ¼
				case 1:
					//menuPosition = 2;
			       // initFileListInfo(mSDCard);
					break;
				//��ʾ�����Ի���
				case 2:
					//searchDilalog();
					break;
				//�����ļ���
				case 3:
					//createFolder();
					break;
				//ճ���ļ�
				case 4:
					//palseFile();
					break;
				//�˳�
				case 5:
					MainActivity.this.finish();
					break;
				}
			}  		
    	});
    }
    /**ճ��*/
   
    public static String mCurrentFilePath = "";
    /**���ݸ�����һ���ļ���·���ַ��������������
     * �����а������ļ����Ʋ����õ�ListView�б���*/
    private void initFileListInfo(String filePath){
    	isAddBackUp = false;
    	mCurrentFilePath = filePath;
    	//��ʾ��ǰ��·��
    	mPath.setText(filePath);
    	mFileName = new ArrayList<String>();
    	mFilePaths = new ArrayList<String>();
    	File mFile = new File(filePath);
    	//���������ļ���·���µ������ļ�/�ļ���
    	File[] mFiles = mFile.listFiles();
    	//ֻҪ��ǰ·�������ֻ���Ŀ¼������sd����Ŀ¼����ʾ�����ظ�Ŀ¼���͡�������һ����
    	if(menuPosition == 1&&!mCurrentFilePath.equals(mRootPath)){
    		initAddBackUp(filePath,mRootPath);
    	}else if(menuPosition == 2&&!mCurrentFilePath.equals(mSDCard)){
        	initAddBackUp(filePath,mSDCard);
    	}
    	
    	/*�������ļ���Ϣ��ӵ�������*/
    	for(File mCurrentFile:mFiles){
    		mFileName.add(mCurrentFile.getName());
    		mFilePaths.add(mCurrentFile.getPath());
    	}
    	
    	/*��������*/
    	setListAdapter(new FileAdapter(MainActivity.this,mFileName,mFilePaths));
    }
    
    private boolean isAddBackUp = false;
    /**���ݵ�����ֻ������ǡ�SD�������ӡ����ظ�Ŀ¼���͡�������һ����*/
    private void initAddBackUp(String filePath,String phone_sdcard){
    	
    	if(!filePath.equals(phone_sdcard)){
    		/*�б���ĵ�һ������Ϊ���ظ�Ŀ¼*/
    		mFileName.add("BacktoRoot");
    		mFilePaths.add(phone_sdcard);
    		/*�б���ĵڶ�������Ϊ������һ��*/
    		mFileName.add("BacktoUp");
    		//�ص���ǰĿ¼�ĸ�Ŀ¼���ص��ϼ�
    		mFilePaths.add(new File(filePath).getParent());
    		//����ӷ��ذ�����ʶλ��Ϊtrue
    		isAddBackUp = true;
    	}
    	
    }

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO �Զ����ɵķ������
		return false;
	}
    
	 class FileAdapter extends BaseAdapter{
	    	//���ؼ������ָ�ʽ���ļ���ͼ��
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
	    	//�ļ����б�
	    	private List<String> mFileNameList;
	    	//�ļ���Ӧ��·���б�
	    	private List<String> mFilePathList;
	    	
	    	public FileAdapter(Context context,List<String> fileName,List<String> filePath){
	    		mContext = context;
	    		mFileNameList = fileName;
	    		mFilePathList = filePath;
	    		//��ʼ��ͼƬ��Դ
	    		//���ص���Ŀ¼
	    		mBackRoot = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.back_to_root);
	    		//���ص���һ��Ŀ¼
	    		mBackUp = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.back_to_up);
	    		//ͼƬ�ļ���Ӧ��icon
	    		mImage = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.image);
	    		//��Ƶ�ļ���Ӧ��icon
	    		mAudio = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.audio);
	    		//��Ƶ�ļ���Ӧ��icon
	    		mVideo = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.video);
	    		//��ִ���ļ���Ӧ��icon
	    		mApk = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.apk);
	    		//�ı��ĵ���Ӧ��icon
	    		mTxt = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.txt);
	    		//���������ļ���Ӧ��icon
	    		mOthers = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.others);
	    		//�ļ��ж�Ӧ��icon
	    		mFolder = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.folder);
	    		//zip�ļ���Ӧ��icon
	    		mRar = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.zip_icon);
	    		//��ҳ�ļ���Ӧ��icon
	    		mWeb = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.web_browser);
	    	}
	    	//����ļ�������
			public int getCount() {
				return mFilePathList.size();
			}
			//��õ�ǰλ�ö�Ӧ���ļ���
			public Object getItem(int position) {
				return mFileNameList.get(position);
			}
			//��õ�ǰ��λ��
			public long getItemId(int position) {
				return position;
			}
			//�����ͼ
			public View getView(int position, View convertView, ViewGroup viewgroup) {
				ViewHolder viewHolder = null;
				if (convertView == null) {
					viewHolder = new ViewHolder();
					LayoutInflater mLI = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					//��ʼ���б�Ԫ�ؽ���
					convertView = mLI.inflate(R.layout.list_child, null);
					//��ȡ�б��ֽ���Ԫ��
					viewHolder.mIV = (ImageView)convertView.findViewById(R.id.image_list_childs);
					viewHolder.mTV = (TextView)convertView.findViewById(R.id.text_list_childs);
					//��ÿһ�е�Ԫ�ؼ������óɱ�ǩ
					convertView.setTag(viewHolder);
				} else {
					//��ȡ��ͼ��ǩ
					viewHolder = (ViewHolder) convertView.getTag();
				}
				File mFile = new File(mFilePathList.get(position).toString());
				//���
				if(mFileNameList.get(position).toString().equals("BacktoRoot")){
					//��ӷ��ظ�Ŀ¼�İ�ť
					viewHolder.mIV.setImageBitmap(mBackRoot);
					viewHolder.mTV.setText("���ظ�Ŀ¼");
				}else if(mFileNameList.get(position).toString().equals("BacktoUp")){
					//��ӷ�����һ���˵��İ�ť
					viewHolder.mIV.setImageBitmap(mBackUp);
					viewHolder.mTV.setText("������һ��");
				}else if(mFileNameList.get(position).toString().equals("BacktoSearchBefore")){
					//��ӷ�������֮ǰĿ¼�İ�ť
					viewHolder.mIV.setImageBitmap(mBackRoot);
					viewHolder.mTV.setText("��������֮ǰĿ¼");
				}else{
					String fileName = mFile.getName();
					viewHolder.mTV.setText(fileName);
					if(mFile.isDirectory()){
						viewHolder.mIV.setImageBitmap(mFolder);
					}else{
				    	String fileEnds = fileName.substring(fileName.lastIndexOf(".")+1,fileName.length()).toLowerCase();//ȡ���ļ���׺����ת��Сд
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
	    	//���ڴ洢�б�ÿһ��Ԫ�ص�ͼƬ���ı�
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
	    //��ʾ�����Ի���
	    private void searchDilalog(){
	    	//����ȷ�����ڵ�ǰĿ¼����������������Ŀ¼�����ı�־
	    	mRadioChecked = 1;
	    	LayoutInflater mLI = LayoutInflater.from(MainActivity.this);
	    	final View mLL = (View)mLI.inflate(R.layout.serach_dialog, null);
	    	mRadioGroup = (RadioGroup)mLL.findViewById(R.id.radiogroup_search);
	    	final RadioButton mCurrentPathButton = (RadioButton)mLL.findViewById(R.id.radio_currentpath);
	    	final RadioButton mWholePathButton = (RadioButton)mLL.findViewById(R.id.radio_wholepath);
	    	//����Ĭ��ѡ���ڵ�ǰ·������
	    	mCurrentPathButton.setChecked(true);
	    	mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
	    		//��ѡ��ı�ʱ����
				public void onCheckedChanged(RadioGroup radiogroup, int checkId) {
					//��ǰ·���ı�־Ϊ1
					if(checkId == mCurrentPathButton.getId()){
						mRadioChecked = 1;
						//����Ŀ¼�ı�־Ϊ2
					}else if(checkId == mWholePathButton.getId()){
						mRadioChecked = 2;
					}
				}	
	    	});
	    	Builder mBuilder = new AlertDialog.Builder(MainActivity.this)
	    								.setTitle("����").setView(mLL)
	    								.setPositiveButton("ȷ��", new OnClickListener(){
											public void onClick(DialogInterface arg0, int arg1) {
												keyWords = ((EditText)mLL.findViewById(R.id.edit_search)).getText().toString();
												if(keyWords.length() == 0){
													Toast.makeText(MainActivity.this, "�ؼ��ֲ���Ϊ��!", Toast.LENGTH_SHORT).show();
													searchDilalog();
												}else{
													if(menuPosition == 1){
														mPath.setText(mRootPath);
													}else{
														mPath.setText(mSDCard);
													}
													//��ȡ�û�����Ĺؼ��ֲ����͹㲥-��ʼ
													Intent keywordIntent = new Intent();
													keywordIntent.setAction(KEYWORD_BROADCAST);
													//���������ķ�Χ����:1.��ǰ·�������� 2.SD��������
													if(mRadioChecked == 1){
														keywordIntent.putExtra("searchpath", mCurrentFilePath);
													}else{
														keywordIntent.putExtra("searchpath", mSDCard);
													}
													//���ݹؼ���
													keywordIntent.putExtra("keyword", keyWords);
													//������Ϊֹ��Я���ؼ�����Ϣ�������˹㲥������Service�����н��ոù㲥����ȡ�ؼ��ֽ�������
													getApplicationContext().sendBroadcast(keywordIntent);
													//��ȡ�û�����Ĺؼ��ֲ����͹㲥-����
													serviceIntent = new Intent("com.android.service.FILE_SEARCH_START");
													MainActivity.this.startService(serviceIntent);//����������������
													//isComeBackFromNotification = false;
												}
											}
	    								}).setNegativeButton("ȡ��", null);
	    	mBuilder.create().show();
	    }
	    
	    /**ע��㲥*/
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
  