package com.karen.server.server;

import com.alibaba.fastjson.JSONObject;
import com.karen.common.model.BaseMessage;
import com.karen.common.util.ByteUtils;
import com.karen.store.MappedFile;
import com.karen.store.SelectMappedBufferResult;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author WenHao
 * @ClassName MsgService
 * @date 2022/2/16 15:34
 * @Description
 */
@Component
public class MsgService {

  Logger log = LoggerFactory.getLogger(this.getClass());

  private int mappedFileListIndex = 0;

  protected final CopyOnWriteArrayList<MappedFile> mappedFiles = new CopyOnWriteArrayList<MappedFile>();

  private String root = "D:/unit_test_store/MappedFileTest/";

  private int oneFileSize = 1024 * 10;

  protected int fileName = 0;

  private static int index = 0;

  public MsgService() {

  }

  void load() {
    try{
      File dir = new File(this.root);
      File[] ls = dir.listFiles();
      if (ls != null && ls.length > 0) {
        List<File> files = Arrays.asList(ls);
        files.sort(Comparator.comparing(File::getName));
        for (File file : files) {
          int len = (int)file.length();
          MappedFile mf = new MappedFile(file.getPath(), len);
          mf.setWrotePosition(len);
          mf.setFlushedPosition(len);
          mf.setCommittedPosition(len);
          this.mappedFiles.add(mf);
          log.info("load file:"+file.getName());
        }
      }else{
        MappedFile mf = new MappedFile(this.root + "/00000", this.oneFileSize);
        this.mappedFiles.add(mf);
        log.info("create file:"+mf.getFile().getName());
      }
    }catch (Exception e){
      log.error("mappedFile load err");
      e.printStackTrace();
    }

  }

  public boolean write(String msg) throws Exception{
    if(this.mappedFiles.size() < 1){
      load();
    }
    byte[] data = msg.getBytes("UTF-8");
    byte[] len = ByteUtils.intToByteArray(data.length);
    int msgLen = data.length + 4;
    ByteBuffer buffer = ByteBuffer.allocate(msgLen);
    buffer.put(len);
    buffer.put(data);
    if(this.mappedFiles.get(this.mappedFiles.size() - 1).getWrotePosition() + msgLen > this.mappedFiles.get(this.mappedFiles.size() - 1).getFileSize()){
      long oldName = Long.parseLong(this.mappedFiles.get(this.mappedFiles.size() - 1).getFile().getName());
      this.fileName =  (int)oldName + this.oneFileSize;
      MappedFile newMappedFile = new MappedFile(this.root+"/"+this.fileName, this.oneFileSize);
      this.mappedFiles.add(newMappedFile);
    }
    boolean res = this.mappedFiles.get(this.mappedFiles.size() - 1).appendMessage(buffer.array(), 0, msgLen);
    buffer.clear();
    return res;
  }

  public BaseMessage read() {
    if(this.mappedFiles.size() < 1){
      load();
    }
    int len = getIndex(index);
    if(len == 0){
      if(this.mappedFileListIndex == (this.mappedFiles.size()-1)){
        log.info("end file");
        return null;
      }
      this.mappedFileListIndex+=1;
      index = 0;
      len = getIndex(index);
    }
    index+=4;
    System.out.println("getMsg:"+index+"->"+(index+len));
    String str = getMsg(index, len);
    index += len;
    try{
      BaseMessage msg = JSONObject.parseObject(str, BaseMessage.class);
      return msg;
    }catch (Exception e){
      e.printStackTrace();
      return null;
    }
  }

  private String getMsg(int pos, int size){
    SelectMappedBufferResult selectMappedBufferResult = this.mappedFiles.get(this.mappedFileListIndex).selectMappedBuffer(pos, size);
    byte[] bytes = new byte[selectMappedBufferResult.getSize()];
    selectMappedBufferResult.getByteBuffer().get(bytes);
    String readString = new String(bytes);
    return readString;
  }

  private int getIndex(int pos){
    SelectMappedBufferResult selectMappedBufferResult = this.mappedFiles.get(this.mappedFileListIndex).selectMappedBuffer(pos,4);
    byte[] dataLen = new byte[selectMappedBufferResult.getSize()];
    selectMappedBufferResult.getByteBuffer().get(dataLen);
    int len = ByteUtils.byteArrayToInt(dataLen);
    return len;
  }

}
