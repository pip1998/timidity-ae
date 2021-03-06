/*******************************************************************************
 * Copyright (C) 2014 xperia64 <xperiancedapps@gmail.com>
 * 
 * Copyright (C) 1999-2008 Masanao Izumo <iz@onicos.co.jp>
 *     
 * Copyright (C) 1995 Tuukka Toivonen <tt@cgs.fi>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.xperia64.timidityae;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import com.xperia64.timidityae.R;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.provider.DocumentFile;
import android.util.Log;
import android.util.SparseIntArray;
import android.widget.TextView;
import android.widget.Toast;

public class Globals {
	public static boolean libLoaded = false;
public static int isPlaying = 1; // Active low.
public static ArrayList<String> plist; // Because arguments don't like big things.
public static ArrayList<String> tmpplist; // I'm lazy. 
public static Bitmap currArt;
public static boolean hardStop=false;

public static final String autoSoundfontHeader="#<--------Config Generated By Timidity AE (DO NOT MODIFY)-------->";

// Fragment Keys
public static String currFoldKey="CURRENT_FOLDER";
public static String currPlistDirectory="CURRENT_PLIST_DIR";
public static boolean shouldRestore=false;
// Resampling Algorithms
public static String[] sampls = {"Cubic Spline","Lagrange","Gaussian","Newton","Linear","None"};
// File filters
public static String musicFiles = "*.mid*.smf*.kar*.mod*.xm*.s3m*.it*.669*.amf*.dsm*.far*.gdm*.imf*.med*.mtm*.stm*.stx*.ult*.uni*.mp3*.m4a*.wav*.ogg*.flac*";
public static String musicVideoFiles = musicFiles+".mp4*.3gp*";
public static String playlistFiles = "*.tpl*";
public static String configFiles = "*.tcf*.tzf*";
public static String fontFiles = "*.sf2*.sfark*.sfark.exe*";
public static ArrayList<String> knownWritablePaths = new ArrayList<String>();
public static ArrayList<String> knownUnwritablePaths = new ArrayList<String>();
public static int defaultListColor = -1;


@SuppressLint("NewApi")
public static int getBackgroundColor(TextView textView) {
    Drawable drawable = textView.getBackground();
    if (drawable instanceof ColorDrawable) {
        ColorDrawable colorDrawable = (ColorDrawable) drawable;
        if (Build.VERSION.SDK_INT >= 11) {
            return colorDrawable.getColor();
        }
        try {
            Field field = colorDrawable.getClass().getDeclaredField("mState");
            field.setAccessible(true);
            Object object = field.get(colorDrawable);
            field = object.getClass().getDeclaredField("mUseColor");
            field.setAccessible(true);
            return field.getInt(object);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    return 0;
}

@SuppressLint({ "NewApi", "SdCardPath" })
public static File getExternalCacheDir(Context c)
{
	if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.FROYO)
	{
		return c.getExternalCacheDir();
	}else{
		return new File("/sdcard/Android/data/com.xperia64.timidityae/cache/");
	}
}

@SuppressLint({ "NewApi", "SdCardPath" })
public static String getLibDir(Context c)
{
	if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.GINGERBREAD)
	{
		String s = c.getApplicationInfo().nativeLibraryDir;
		if(!s.endsWith(File.separator))
		{
			s+="/";
		}
		return s;
	}else{
		return "/data/data/com.xperia64.timidityae/lib/";
	}
}

public static int[] validRates(boolean stereo, boolean sixteen)
{
	ArrayList<Integer> valid = new ArrayList<Integer>();
	for (int rate : new int[] {8000, 11025, 16000, 22050, 44100, 48000, 88200, 96000}) {
       
		int bufferSize = AudioTrack.getMinBufferSize(rate, (stereo)?AudioFormat.CHANNEL_OUT_STEREO:AudioFormat.CHANNEL_OUT_MONO, (sixteen)?AudioFormat.ENCODING_PCM_16BIT:AudioFormat.ENCODING_PCM_8BIT);
        if (bufferSize > 0) {
        	//System.out.println(rate+" "+bufferSize);
            // buffer size is valid, Sample rate supported
        	valid.add(rate);
        }
    }
	int[] rates = new int[valid.size()];
	for(int i = 0; i<rates.length; i++)
		rates[i]=valid.get(i);
	return rates;
}
/*public static boolean canWrite(String path)
{
	if(!path.endsWith("/"))
	{
		return false;
	}
	if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
	{
		Random r = new Random();
		// Generate a random unique temporary file.
		File f = new File(path+r.nextInt(1000000));
		while(f.exists())
		{
			f = new File(path+r.nextInt(1000000));
			try
			{
				Thread.sleep(10);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		try
		{
			f.createNewFile();
		} catch (IOException e)
		{
			return false;
		}
		if(f.exists())
			f.delete();
		return true;
	}else{
		return new File(path).canWrite();
	}
}*/
/*public static boolean canWrite(DocumentFile path)
{
	return false;
}*/
public static SparseIntArray validBuffers(int[] rates, boolean stereo, boolean sixteen)
{
	SparseIntArray buffers = new SparseIntArray();
	for(int rate : rates)
	{
		buffers.put(rate, AudioTrack.getMinBufferSize(rate, (stereo)?AudioFormat.CHANNEL_OUT_STEREO:AudioFormat.CHANNEL_OUT_MONO, (sixteen)?AudioFormat.ENCODING_PCM_16BIT:AudioFormat.ENCODING_PCM_8BIT));
	}
	return buffers;
	/*HashMap<Integer, Integer> buffers = new HashMap<Integer, Integer>();
	for(int rate : rates)
	{
		buffers.put(rate, AudioTrack.getMinBufferSize(rate, (stereo)?AudioFormat.CHANNEL_OUT_STEREO:AudioFormat.CHANNEL_OUT_MONO, (sixteen)?AudioFormat.ENCODING_PCM_16BIT:AudioFormat.ENCODING_PCM_8BIT));
	}
	return buffers;*/
}

public static int probablyFresh=0;
//---------SETTINGS STORAGE----------
public static SharedPreferences prefs;
public static boolean firstRun;
public static int theme; // 1 = Light, 2 = Dark
public static boolean showHiddenFiles;
public static String defaultFolder;
public static String dataFolder;
public static boolean manConfig;
public static int defSamp;
public static boolean shouldLolNag;
//public static ArrayList<String> soundfonts; // this list should only be touched in SettingsActivity
public static int mono; // 0 = stereo downsampled to mono, 1 = timidity-synthesized mono, 2 = stereo, 3 = downsampled to mono then copied to stereo?
public static boolean sixteen;
public static int aRate;
public static int buff;
public static boolean nativeMidi;
public static boolean keepWav;
public static boolean onlyNative=false;
public static boolean showVideos;
public static boolean useDefaultBack=false;
public static boolean compressCfg = true;
//public static AssetManager assets;
public static boolean nukedWidgets=false;
public static Uri theFold=null;
public static boolean reShuffle = false;
public static boolean preserveSilence = true;
public static boolean freeInsts = true;

public static void reloadSettings(Activity c, AssetManager assets)
{
	
	prefs = PreferenceManager
            .getDefaultSharedPreferences(c);
	firstRun = prefs.getBoolean("tplusFirstRun", true);
	theme = Integer.parseInt(prefs.getString("fbTheme", "1"));
	showHiddenFiles = prefs.getBoolean("hiddenSwitch", false);
	defaultFolder = prefs.getString("defaultPath", Environment.getExternalStorageDirectory().getAbsolutePath());
	dataFolder = prefs.getString("dataDir", Environment.getExternalStorageDirectory()+"/TimidityAE/");
	manConfig = prefs.getBoolean("manualConfig", false);
	JNIHandler.currsamp = defSamp = Integer.parseInt(prefs.getString("tplusResamp", "0"));
	mono = Integer.parseInt(prefs.getString("sdlChanValue", "2"));
	sixteen = true;//prefs.getString("tplusBits", "16").equals("16");
	aRate = Integer.parseInt(prefs.getString("tplusRate", Integer.toString(AudioTrack.getNativeOutputSampleRate(AudioTrack.MODE_STREAM))));
	buff = Integer.parseInt(prefs.getString("tplusBuff", "192000"));
	showVideos=prefs.getBoolean("videoSwitch", true);
	shouldLolNag=prefs.getBoolean("shouldLolNag", true);
	keepWav=prefs.getBoolean("keepPartialWav",false);
	useDefaultBack=prefs.getBoolean("useDefBack", false);
	compressCfg=prefs.getBoolean("compressCfg", true);
	reShuffle=prefs.getBoolean("reShuffle",false);
	freeInsts=prefs.getBoolean("tplusUnload", true);
	preserveSilence=prefs.getBoolean("tplusSilKey", true);
	if(!onlyNative)
		nativeMidi = prefs.getBoolean("nativeMidiSwitch", false);
	else
		nativeMidi = true;
	
}
//-----------------------------------

public static boolean isMidi(String songTitle)
{
	return !(songTitle.toLowerCase(Locale.US).endsWith(".mp3")
	|| songTitle.toLowerCase(Locale.US).endsWith(".m4a")
	|| songTitle.toLowerCase(Locale.US).endsWith(".wav")
	|| songTitle.toLowerCase(Locale.US).endsWith(".ogg")
	|| songTitle.toLowerCase(Locale.US).endsWith(".flac")
	|| songTitle.toLowerCase(Locale.US).endsWith(".mp4")
	|| songTitle.toLowerCase(Locale.US).endsWith(".3gp")
	|| (Globals.nativeMidi 
			&&(songTitle.toLowerCase(Locale.US).endsWith(".mid")
			|| songTitle.toLowerCase(Locale.US).endsWith(".kar") 
			|| songTitle.toLowerCase(Locale.US).endsWith(".smf"))));
}
public static boolean initialize(final Activity a)
{
	if(firstRun)
	{
		final File rootStorage = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/TimidityAE/");
		if(!rootStorage.exists())
		{
			rootStorage.mkdir();
		}
		File playlistDir=new File(rootStorage.getAbsolutePath()+"/playlists/");
		if(!playlistDir.exists())
		{
			playlistDir.mkdir();
		}
		File tcfgDir=new File(rootStorage.getAbsolutePath()+"/timidity/");
		if(!tcfgDir.exists())
		{
			tcfgDir.mkdir();
		}
		File sfDir=new File(rootStorage.getAbsolutePath()+"/soundfonts/");
		if(!sfDir.exists())
		{
			sfDir.mkdir();
		}
		updateBuffers(updateRates());
		aRate = Integer.parseInt(prefs.getString("tplusRate", Integer.toString(AudioTrack.getNativeOutputSampleRate(AudioTrack.MODE_STREAM))));
		buff = Integer.parseInt(prefs.getString("tplusBuff", "192000")); // This is usually a safe number, but should probably do a test or something
		migrateFrom1X(rootStorage);
		final Editor eee = prefs.edit();
		firstRun=false;
		eee.putBoolean("tplusFirstRun", false);
		eee.putString("dataDir", Environment.getExternalStorageDirectory().getAbsolutePath()+"/TimidityAE/");
		if(new File(dataFolder+"/timidity/timidity.cfg").exists())
		{
			if(manConfig = !cfgIsAuto(dataFolder+"/timidity/timidity.cfg"))
			{
				eee.putBoolean("manConfig", true);
			}else{
				eee.putBoolean("manConfig", false);
				ArrayList<String> soundfonts = new ArrayList<String>();
				 FileInputStream fstream = null;
				try {
					fstream = new FileInputStream(dataFolder+"/timidity/timidity.cfg");
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			  	  // Get the object of DataInputStream
			  	  DataInputStream in = new DataInputStream(fstream);
			  	  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  	  //Read File Line By Line
			  	  try {
					br.readLine(); // skip first line
				} catch (IOException e) {
					e.printStackTrace();
				}
			  	  String line;
			  	  try {
					while((line=br.readLine())!=null)
					  {
						if(line.indexOf("soundfont \"")>=0&&line.lastIndexOf('"')>=0)
						{
							try{
							String st=line.substring(line.indexOf("soundfont \"")+11,line.lastIndexOf('"'));
							soundfonts.add(st);
							}catch (ArrayIndexOutOfBoundsException e1)
							{
								e1.printStackTrace();
							}
						  
						}
					  }
				} catch (IOException e) {
					e.printStackTrace();
				}
			  	 try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			  	try {
					eee.putString("tplusSoundfonts", ObjectSerializer.serialize(soundfonts));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			eee.commit();
			return true;
		}else{
			// Should probably check if 8rock11e exists no matter what
			eee.putBoolean("manConfig", false);
			
			AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
				
				ProgressDialog pd;
				@Override
				protected void onPreExecute() {
					pd = new ProgressDialog(a);
					pd.setTitle(a.getResources().getString(R.string.extract));
					pd.setMessage(a.getResources().getString(R.string.extract_sum));
					pd.setCancelable(false);
					pd.setIndeterminate(true);
					pd.show();
				}
					
				@Override
				protected Void doInBackground(Void... arg0) {
					
					if(extract8Rock(a)!=777)
					{
						Toast.makeText(a, "Could not extrct default soundfont", Toast.LENGTH_SHORT).show();
					}
					return null;
				}
				
				@Override
				protected void onPostExecute(Void result) {
					if(pd!=null)
						pd.dismiss();
						ArrayList<String> tmpConfig = new ArrayList<String>();
						tmpConfig.add(rootStorage.getAbsolutePath()+"/soundfonts/8Rock11e.sf2");
						try {
							eee.putString("tplusSoundfonts", ObjectSerializer.serialize(tmpConfig));
						} catch (IOException e) {
							e.printStackTrace();
						}
						eee.commit();
						writeCfg(a, rootStorage.getAbsolutePath()+"/timidity/timidity.cfg",tmpConfig);
						((TimidityActivity)a).initCallback();
				}
					
			};
			task.execute((Void[])null);
			return false;
		}
		
		
		
	}else{
		return true;
	}
}
public static void migrateFrom1X(File newData)
{
	File oldPlists = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Android/data/com.xperia64.timidityae/playlists/");
	if(oldPlists.exists())
	{
		if(oldPlists.isDirectory())
		{
			for(File f : oldPlists.listFiles())
			{
				if(f.getName().toLowerCase(Locale.US).endsWith(".tpl"))
				{
					f.renameTo(new File(newData.getAbsolutePath()+"/playlists/"+f.getName()));
				}
			}
		}
	}
	File oldSoundfonts = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Android/data/com.xperia64.timidityae/soundfonts/");
	if(oldSoundfonts.exists())
	{
		if(oldSoundfonts.isDirectory())
		{
			for(File f : oldSoundfonts.listFiles())
			{
				if(f.getName().toLowerCase(Locale.US).endsWith(".sf2")||f.getName().toLowerCase(Locale.US).endsWith(".sfark"))
				{
					f.renameTo(new File(newData.getAbsolutePath()+"/soundfonts/"+f.getName()));
				}
			}
		}
	}
}
public static void writeCfg(Context c, String path, ArrayList<String> soundfonts)
{
	if(path==null)
	{
		Toast.makeText(c, "Configuration path null (3)", Toast.LENGTH_LONG).show();
		return;
	}
	if(soundfonts==null)
	{
		Toast.makeText(c, "Soundfonts null (4)", Toast.LENGTH_LONG).show();
		return;
	}
	if(path.contains("//"))
	{
		path = path.replace("//", "/");
	}
	if(!manConfig)
	{
		String[] needLol = null;
		try{
	        new FileOutputStream(path,true).close();
	  }catch(FileNotFoundException e)
	  {
		needLol=getDocFilePaths(c, path); 
	  } catch (IOException e)
	{
		e.printStackTrace();
	}

		if(needLol!=null&&Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
		{
			if(theFold!=null)
			{
				String probablyTheDirectory = needLol[0];
			    String probablyTheRoot = needLol[1];
			    String needRename = null;
			    String value = null;
			    if(probablyTheDirectory.length()>1)
				{
					needRename = path.substring(path.indexOf(probablyTheRoot)+probablyTheRoot.length());
					value = probablyTheDirectory+path.substring(path.lastIndexOf('/'));
				}else{
					return;
				}
				if(new File(path).exists())
				{
					if(cfgIsAuto(path)||new File(path).length()<=0)
					{
						Globals.tryToDeleteFile(c, path);
					}else{
						Toast.makeText(c, "Renaming manually edited cfg... (7)", Toast.LENGTH_LONG).show();
						renameDocumentFile(c, path, needRename+".manualTimidityCfg."+Long.toString(System.currentTimeMillis()));
					}
				}
				
				FileWriter fw = null;
				try {
					fw = new FileWriter(value,false);
				} catch (IOException e) {
					e.printStackTrace();
				}
				  try {
					fw.write(autoSoundfontHeader+"\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				for(String s : soundfonts)
				{
					try {
						fw.write((s.startsWith("#")?"#":"")+"soundfont \""+s+"\"\n");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				  try {
						fw.close();
					} catch (IOException e) {
						e.printStackTrace();
					} 
				Globals.renameDocumentFile(c, value, needRename);
			}else{
				Toast.makeText(c, "Could not write configuration file. Does Timidity AE have write access to the data folder? (1)", Toast.LENGTH_LONG).show();
			}
			
			
		}else{
			
		File theConfig = new File(path);
		if(theConfig.exists()) // It should exist if we got here.
		{
			if(!theConfig.canWrite())
			{
				Toast.makeText(c, "Could not write configuration file. Does Timidity AE have write access to the data folder? (2)", Toast.LENGTH_LONG).show();
				return;
			}
			if(cfgIsAuto(path)||theConfig.length()<=0) // Negative file length? Who knows.
			{
					theConfig.delete(); // Auto config, safe to delete
			}else{
					Toast.makeText(c, "Renaming manually edited cfg... (6)", Toast.LENGTH_LONG).show();
					theConfig.renameTo(new File(path+".manualTimidityCfg."+Long.toString(System.currentTimeMillis()))); // manual config, rename for later
			}
		}
		FileWriter fw = null;
		try {
			fw = new FileWriter(path,false);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		  try {
			fw.write(autoSoundfontHeader+"\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for(String s : soundfonts)
		{
			if(s==null)
				continue;
			try {
				fw.write((s.startsWith("#")?"#":"")+"soundfont \""+s+"\"\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		  try {
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
	}
}
@SuppressLint("NewApi")
public static String[] getDocFilePaths(Context c, String parent)
{
	if(Build.VERSION.SDK_INT<Build.VERSION_CODES.LOLLIPOP)
		return null; // Error.
	parent = parent.replace("//", "/");
	 String probablyTheDirectory = "";
	  String probablyTheRoot = "";
	  File par = new File(parent);
	  String absp = par.getAbsolutePath();
	File[] x = c.getExternalFilesDirs(null);
	for(File f : x)
	{
		if(f!=null)
		{
		String ex = f.getAbsolutePath();
		String ss1;
		String ss2;
		int lastmatch = 1;
		while(lastmatch<absp.length()&&lastmatch<ex.length())
		{
			ss1 = ex.substring(0, lastmatch+1);
			ss2 = absp.substring(0, lastmatch+1);
			if(ss1.equals(ss2))
			{
				lastmatch++;
			}else{
				break;
			}
		}
		String theRoot = absp.substring(0,lastmatch);
		if(theRoot.equals("/storage/")||theRoot.equals("/mnt/"))
		{
			continue;
		}else{
			probablyTheDirectory = ex;
			probablyTheRoot = theRoot;
			break;
		}	
		}
	}
	String[] rets = new String[2];
	rets[0] = probablyTheDirectory;
	rets[1] = probablyTheRoot;
	return rets;
}


public static boolean renameDocumentFile(Context c, String from, String subTo)
{
	// From is the full path
	// subTo is the path without the device prefix. 
	// So /storage/sdcard1/folder/file.mid should be folder/file.mid
	if(theFold==null)
		return false;
	from = from.replace("//", "/");
	subTo = subTo.replace("//", "/");
	DocumentFile df = DocumentFile.fromTreeUri(c, theFold);
	String split[] = from.split("/");
	int i;
	for(i = 0; i<split.length; i++)
	{
		if(split[i].equals(df.getName()))
		{
			i++;
			break;
		}
	}
	DocumentFile xx = df;
	StringBuilder upper = new StringBuilder();
	while(i<split.length)
	{
		xx = xx.findFile(split[i++]);
		upper.append("../");
		if(xx==null)
		{
			Log.e("TimidityAE Globals","Rename file error.");
			break;
		}
	}
	if(xx!=null&&upper.length()>3)
	{
		return xx.renameTo(upper.substring(0, upper.length()-3)+subTo);
	}
	return false;
}
public static void tryToDeleteFile(Context c, String filename)
{
	filename = filename.replace("//", "/");
	if(new File(filename).exists())
	{
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP&& Globals.theFold!=null)
		{
			DocumentFile df = DocumentFile.fromTreeUri(c, theFold);
			String split[] = filename.split("/");
			int i;
			for(i = 0; i<split.length; i++)
			{
				if(split[i].equals(df.getName()))
				{
					i++;
					break;
				}
			}
			DocumentFile xx = df;
			while(i<split.length)
			{
				xx = xx.findFile(split[i++]);
				//upper.append("../");
				if(xx==null)
				{
					Log.e("TimidityAE Globals","Delete file error.");
					break;
				}
			}
	// Why on earth is DocumentFile's delete method recursive by default?
	// Seriously. I wiped my sd card twice because of this.
			if(xx!=null&&xx.isFile()&&!xx.isDirectory())
			{
				xx.delete();
			}
		}else{
			new File(filename).delete();
		}
	}
}
public static void tryToCreateFile(Context c, String filename)
{
	filename = filename.replace("//", "/");
	if(!(new File(filename).exists()))
	{
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP&&theFold!=null)
		{
			DocumentFile df = DocumentFile.fromTreeUri(c, theFold);
			String split[] = filename.split("/");
			int i;
			for(i = 0; i<split.length; i++)
			{
				if(split[i].equals(df.getName()))
				{
					i++;
					break;
				}
			}
			DocumentFile xx = df;
			while(i<split.length-1)
			{
				xx = xx.findFile(split[i++]);
				if(xx==null)
				{
					Log.e("TimidityAE Globals","Create file error.");
					break;
				}
			}
			if(xx!=null)
			{
				xx.createFile("timidityae/tpl", split[split.length-1]);
			}
		}else{
			try
			{
				new File(filename).createNewFile();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}

public static boolean cfgIsAuto(String path)
{
	String firstLine="";
	try{
  	  FileInputStream fstream = new FileInputStream(path);
  	  DataInputStream in = new DataInputStream(fstream);
  	  BufferedReader br = new BufferedReader(new InputStreamReader(in));
  	  firstLine=br.readLine();

  	  in.close();
  	    }catch (Exception e){}
	if(firstLine!=null)
		return firstLine.contains(autoSoundfontHeader);
	return false;
}
public static int[] updateRates()
{
	if(prefs!=null)
	{
		int[] values = Globals.validRates(
				prefs.getString("sdlChanValue","2").equals("2"), 
				/*prefs.getString("tplusBits", "16").equals("16")*/true);
		CharSequence[] hz = new CharSequence[values.length];
		CharSequence[] hzItems = new CharSequence[values.length];
		boolean validRate=false;
		for(int i = 0; i<values.length; i++)
		{
			hz[i]=Integer.toString(values[i])+"Hz";
			hzItems[i]=Integer.toString(values[i]);
			if(prefs.getString("tplusRate", Integer.toString(AudioTrack.getNativeOutputSampleRate(AudioTrack.MODE_STREAM))).equals(hzItems[i]))
			{
				validRate=true;
    			break;
			}
		}
  
		if(!validRate)
			prefs.edit().putString("tplusRate",Integer.toString(AudioTrack.getNativeOutputSampleRate(AudioTrack.MODE_STREAM))).commit();
    
		return values;
	}
	return null;
}
public static boolean updateBuffers(int[] rata)
{
	if(rata!=null)
	{
		SparseIntArray buffMap = Globals.validBuffers(rata, prefs.getString("sdlChanValue","2").equals("2"), true/*prefs.getString("tplusBits", "16").equals("16")*/);
		int realMin = buffMap.get(Integer.parseInt(prefs.getString("tplusRate", Integer.toString(AudioTrack.getNativeOutputSampleRate(AudioTrack.MODE_STREAM)))));
		if(buff<realMin)
		{
			prefs.edit().putString("tplusBuff", Integer.toString(buff=realMin)).commit();
			return false;
		}
	}
	return true;
}

public static int extract8Rock(Context c)
{
	InputStream in = null;
	try {
		in = c.getAssets().open("8Rock11e.sfArk");
	} catch (IOException e) {
		e.printStackTrace();
	}
	String[] needLol = null;
	try{
        new FileOutputStream(Globals.dataFolder+"/soundfonts/8Rock11e.sfArk",true).close();
	}catch(FileNotFoundException e)
	{
		needLol=getDocFilePaths(c, Globals.dataFolder); 
	}catch (IOException e)
	{
		e.printStackTrace();
	}

	if(needLol!=null)
	{
		File f = new File(Globals.dataFolder+"/soundfonts/8Rock11e.sfArk");
		if (f!=null)
			if(f.exists())
				 tryToDeleteFile(c,Globals.dataFolder+"/soundfonts/8Rock11e.sfArk");
	    OutputStream out = null;
	    String probablyTheDirectory = needLol[0];
	    String probablyTheRoot = needLol[1];
	    String needRename;
	    String value;
	    String value2;
	    if(probablyTheDirectory.length()>1)
		{
			needRename = dataFolder.substring(dataFolder.indexOf(probablyTheRoot)+probablyTheRoot.length())+"/soundfonts/8Rock11e.sf2";
			value = probablyTheDirectory+'/'+"8Rock11e.sfArk";
			value2 = probablyTheDirectory+'/'+"8Rock11e.sf2";
		}else{
			return -9;
		}
		try {
			out = new FileOutputStream( value );
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if(out==null)
			return -1;
	    byte buf[] = new byte[1024];
	    int len;
	    try {
			while( ( len = in.read( buf ) ) > 0 )
			    out.write( buf, 0, len );
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	    try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    JNIHandler.decompressSFArk(value, "8Rock11e.sf2");
	    renameDocumentFile(c, value2, needRename);
	    tryToDeleteFile(c,value);
	}else{
		File f = new File(Globals.dataFolder+"/soundfonts/8Rock11e.sfArk");
		if (f!=null)
			if(f.exists())
				f.delete();
	    OutputStream out = null;
		try {
			out = new FileOutputStream( Globals.dataFolder+"/soundfonts/8Rock11e.sfArk" );
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if(out==null)
			return -1;
	    byte buf[] = new byte[1024];
	    int len;
	    try {
			while( ( len = in.read( buf ) ) > 0 )
			    out.write( buf, 0, len );
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	    try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    JNIHandler.decompressSFArk(Globals.dataFolder+"/soundfonts/8Rock11e.sfArk", "8Rock11e.sf2");
	    //System.out.println("decompresed sfark");
	    new File(Globals.dataFolder+"/soundfonts/8Rock11e.sfArk").delete();
	}
	
    return 777;
    
}
@TargetApi(Build.VERSION_CODES.FROYO)
public static class DownloadTask extends AsyncTask<String, Integer, String> {

    private Context context;
    private PowerManager.WakeLock mWakeLock;
    private ProgressDialog prog;
    private String theUrl="";
    private String theFilename="";
    public DownloadTask(Context context) {
        this.context = context;
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // take CPU lock to prevent CPU from going off if the user 
        // presses the power button during download
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
             getClass().getName());
        mWakeLock.acquire();
        
  	  prog = new ProgressDialog(context);
  	  prog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
  		    @Override
  		    public void onClick(DialogInterface dialog, int which) {
  		    	
  		        dialog.dismiss();
  		    }
  		});
  	prog.setOnCancelListener(new DialogInterface.OnCancelListener() {
  	    @Override
  	    public void onCancel(DialogInterface dialog) {
  	        DownloadTask.this.cancel(true);
  	    }
  	});
  	  prog.setTitle("Downloading file...");
  	  prog.setMessage("Downloading...");       
  	  prog.setCancelable(false);
  	  prog.show();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        prog.setIndeterminate(false);
        prog.setMax(100);
        prog.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        mWakeLock.release();
        prog.dismiss();
        if (result != null)
            Toast.makeText(context,"Download error: "+result, Toast.LENGTH_LONG).show();
        else
        	((TimidityActivity)context).downloadFinished(theUrl, theFilename);
    }
	@TargetApi(Build.VERSION_CODES.FROYO)
	@Override
    protected String doInBackground(String... sUrl) {
        InputStream input = null;
        OutputStream output = null;
        URL url = null;
		try
		{
			url = new URL(sUrl[0]);
		} catch (MalformedURLException e1)
		{
			e1.printStackTrace();
		}
        theUrl=sUrl[0];
        theFilename=sUrl[1];
        if(theUrl.startsWith("http"))
        {
        HttpURLConnection connection = null;
        try {
            
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage();
            }

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();

            // download the file
            input = connection.getInputStream();
            output = new FileOutputStream(Globals.getExternalCacheDir(context).getAbsolutePath()+'/'+theFilename);

            byte[] data = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                // allow canceling with back button
                if (isCancelled()) {
                	output.close();
                    input.close();
                    return null;
                }
                total += count;
                // publishing the progress....
                if (fileLength > 0) // only if total length is known
                    publishProgress((int) (total * 100 / fileLength));
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            return e.toString();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }

            if (connection != null)
                connection.disconnect();
        }
        return null;
        }else{
        	 HttpsURLConnection connection = null;
             try {
                 
                 connection = (HttpsURLConnection) url.openConnection();
                 connection.connect();

                 // expect HTTP 200 OK, so we don't mistakenly save error report
                 // instead of the file
                 if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                     return "Server returned HTTPS " + connection.getResponseCode()
                             + " " + connection.getResponseMessage();
                 }

                 // this will be useful to display download percentage
                 // might be -1: server did not report the length
                 int fileLength = connection.getContentLength();

                 // download the file
                 input = connection.getInputStream();
                 output = new FileOutputStream(Globals.getExternalCacheDir(context).getAbsolutePath()+'/'+theFilename);

                 byte[] data = new byte[4096];
                 long total = 0;
                 int count;
                 while ((count = input.read(data)) != -1) {
                     // allow canceling with back button
                     if (isCancelled()) {
                    	 input.close();
                    	 output.close();
                         return null;
                     }
                     total += count;
                     // publishing the progress....
                     if (fileLength > 0) // only if total length is known
                         publishProgress((int) (total * 100 / fileLength));
                     output.write(data, 0, count);
                 }
             } catch (Exception e) {
                 return e.toString();
             } finally {
                 try {
                     if (output != null)
                         output.close();
                     if (input != null)
                         input.close();
                 } catch (IOException ignored) {
                 }

                 if (connection != null)
                     connection.disconnect();
             }
             return null;
        }
    }
   
}
// @formatter:off
/*
 * RESAMPLE_CSPLINE, 0
	RESAMPLE_LAGRANGE, 1
	RESAMPLE_GAUSS, 2
	RESAMPLE_NEWTON, 3
	RESAMPLE_LINEAR, 4
	RESAMPLE_NONE 5
 
/*
 * #define RC_ERROR	-1
#ifdef RC_NONE
#undef RC_NONE
#endif
#define RC_NONE		0
#define RC_QUIT		1
#define RC_NEXT		2
#define RC_PREVIOUS	3 // Restart this song at beginning, or the previous
			     song if we're less than a second into this one. 
#define RC_FORWARD	4
#define RC_BACK		5
#define RC_JUMP		6
#define RC_TOGGLE_PAUSE 7	Pause/continue 
#define RC_RESTART	8	/* Restart song at beginning 
#define RC_PAUSE	9	/* Really pause playing 
#define RC_CONTINUE	10	/* Continue if paused 
#define RC_REALLY_PREVIOUS 11	/* Really go to the previous song 
#define RC_CHANGE_VOLUME 12
#define RC_LOAD_FILE	13	/* Load a new midifile 
#define RC_TUNE_END	14	/* The tune is over, play it again sam? 
#define RC_KEYUP	15	/* Key up 
#define RC_KEYDOWN	16	/* Key down 
#define RC_SPEEDUP	17	/* Speed up 
#define RC_SPEEDDOWN	18	/* Speed down 
#define RC_VOICEINCR	19	/* Increase voices 
#define RC_VOICEDECR	20	/* Decrease voices 
#define RC_TOGGLE_DRUMCHAN 21	/* Toggle drum channel 
#define RC_RELOAD	22	/* Reload & Play 
#define RC_TOGGLE_SNDSPEC 23	/* Open/Close Sound Spectrogram Window 
#define RC_CHANGE_REV_EFFB 24
#define RC_CHANGE_REV_TIME 25
#define RC_SYNC_RESTART 26
#define RC_TOGGLE_CTL_SPEANA 27
#define RC_CHANGE_RATE	28
#define RC_OUTPUT_CHANGED      29
#define RC_STOP		30	/* Stop to play 
#define RC_TOGGLE_MUTE	31
#define RC_SOLO_PLAY	32
#define RC_MUTE_CLEAR	33*/
//@formatter:on


 
}
