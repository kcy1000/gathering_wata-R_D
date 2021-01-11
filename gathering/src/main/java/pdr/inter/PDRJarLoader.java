package pdr.inter;
import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;

import dalvik.system.DexClassLoader;

public class PDRJarLoader
{
	private String mJarDirectory = "/";
	private Context mContext;

	public static boolean isExistFile(String s)
	{
		File f = new File(s);
		return f.isFile();
	}

	public PDRJarLoader(Context context)
	{
		mContext = context;
		assert(mContext != null);
	}

	public pdr_interface Load(String jarFile, String jarClass)
			throws FileNotFoundException, MalformedURLException, ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		String storagePath = Environment.getExternalStorageDirectory().getPath();
		String jarFullPath = storagePath + mJarDirectory + jarFile;
		if(!isExistFile(jarFullPath))
			return null;

		DexClassLoader classLoader = new DexClassLoader(jarFullPath, "/data/data/" + mContext.getPackageName() + "/", null, getClass().getClassLoader());
		Class<?> myClass = classLoader.loadClass(jarClass);
		return (pdr_interface) myClass.newInstance();
	}
}