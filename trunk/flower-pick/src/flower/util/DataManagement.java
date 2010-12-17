package flower.util;

import java.io.FileWriter;
import java.io.IOException; 
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap; 

/**
 * 进行文件操作的类
 * @author 易建龙
 */
public class DataManagement {
	
	HashMap<String,FileWriter> fileWriterMap;
	DateFormat df = new SimpleDateFormat("HH:mm:ss");
	public DataManagement() { 
		fileWriterMap = new HashMap<String, FileWriter>();
	}
	
	public boolean creatFileWriter(String fileName) {

		try {
			FileWriter fw =  new FileWriter(fileName, true); 
			fileWriterMap.put(fileName,fw);
			return true; 
				
		} catch (IOException e) { 
			e.printStackTrace();
			return false;
		} 
	}
	
	public boolean exist(String fname) {
		if(fileWriterMap.keySet() == null)
			return false;
		for (String f : fileWriterMap.keySet()) {
			if(f.equals(fname))
				return true;
		}
		return false;
	}
	
	public void saveToFile(String fname,String content) {
		FileWriter fw = fileWriterMap.get(fname);
		try {
			fw.write(content+"\n");
			String ss = df.format(new java.util.Date());
			if(ss.split(":")[2].equals("00"))
				fw.write("##"+ss+"\n");
			fw.flush();
		} catch (IOException e) { 
			e.printStackTrace();
		}
	}
	
	public FileWriter getFileWriterByName(String fname) {
		return fileWriterMap.get(fname);
	}
	
	public void closeByName(String fname) {
		FileWriter fw = fileWriterMap.get(fname);
		try {
			fw.flush();
			fw.close();
		} catch (IOException e) { 
			e.printStackTrace();
		}
	}
	
	public void closeAll() {
		for(String f : fileWriterMap.keySet()) {
			
			try {
				fileWriterMap.get(f).flush();
				fileWriterMap.get(f).close();
				fileWriterMap.remove(f);
				
			} catch (IOException e) { 
				e.printStackTrace();
			}
		}
	}
	
	
}
