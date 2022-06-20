package com.karen.store;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author WenHao
 * @ClassName MappedFile
 * @date 2020/2/15 10:13
 * @Description
 */
public class MappedFile extends ReferenceResource {

  protected static final Logger log = LoggerFactory.getLogger(MappedFile.class);

  public static final int OS_PAGE_SIZE = 1024 * 4;

  private static final AtomicLong TOTAL_MAPPED_VIRTUAL_MEMORY = new AtomicLong(0);

  private static final AtomicInteger TOTAL_MAPPED_FILES = new AtomicInteger(0);

  //写索引
  protected final AtomicInteger wrotePosition = new AtomicInteger(0);
  //
  private final AtomicInteger flushedPosition = new AtomicInteger(0);
  //提交索引
  protected final AtomicInteger committedPosition = new AtomicInteger(0);

  private MappedByteBuffer mappedByteBuffer;

  protected int fileSize;

  private String fileName;

  private long fileFromOffset;

  private File file;

  protected FileChannel fileChannel;

  protected ByteBuffer writeBuffer = null;

  private boolean firstCreateInQueue = false;

  private volatile long storeTimestamp = 0;

  //构造方法
  public MappedFile(final String fileName, final int fileSize) throws IOException {
    init(fileName, fileSize);
  }

  //初始化
  private void init(final String fileName, final int fileSize) throws IOException {
    this.fileName = fileName;
    this.fileSize = fileSize;
    this.file = new File(fileName);
    this.fileFromOffset = Long.parseLong(this.file.getName());
    boolean ok = false;

    ensureDirOK(this.file.getParent());

    try {
      this.fileChannel = new RandomAccessFile(this.file, "rw").getChannel();
      this.mappedByteBuffer = this.fileChannel.map(MapMode.READ_WRITE, 0, fileSize);
      TOTAL_MAPPED_VIRTUAL_MEMORY.addAndGet(fileSize);
      TOTAL_MAPPED_FILES.incrementAndGet();
      ok = true;
    } catch (FileNotFoundException e) {
      log.error("Failed to create file " + this.fileName, e);
      throw e;
    } catch (IOException e) {
      log.error("Failed to map file " + this.fileName, e);
      throw e;
    } finally {
      if (!ok && this.fileChannel != null) {
        this.fileChannel.close();
      }
    }
  }

  //确保目录正常
  public static void ensureDirOK(final String dirName) {
    if (dirName != null) {
      File f = new File(dirName);
      if (!f.exists()) {
        boolean result = f.mkdirs();
        log.info(dirName + " mkdir " + (result ? "OK" : "Failed"));
      }
    }
  }

  //写入文件
  public boolean appendMessage(final byte[] data) {
    int currentPos = this.wrotePosition.get();

    if ((currentPos + data.length) <= this.fileSize) {
      try {
        ByteBuffer buf = this.mappedByteBuffer.slice();
        buf.position(currentPos);
        buf.put(data);
      } catch (Throwable e) {
        log.error("Error occurred when append message to mappedFile.", e);
      }
      this.wrotePosition.addAndGet(data.length);
      return true;
    }

    return false;
  }

  /**
   * 从偏移量到偏移量 + 长度的数据内容将写入文件
   * @param offset 偏移量
   * @param length 长度
   */
  public boolean appendMessage(final byte[] data, final int offset, final int length) {
    int currentPos = this.wrotePosition.get();

    if ((currentPos + length) <= this.fileSize) {
      try {
        ByteBuffer buf = this.mappedByteBuffer.slice();
        buf.position(currentPos);
        buf.put(data, offset, length);
      } catch (Throwable e) {
        log.error("Error occurred when append message to mappedFile.", e);
      }
      this.wrotePosition.addAndGet(length);
      return true;
    }

    return false;
  }

  //获取指定位置特定大小的消息
  public SelectMappedBufferResult selectMappedBuffer(int pos, int size) {
    int readPosition = getReadPosition();
    if ((pos + size) <= readPosition) {
      if (this.hold()) {
        ByteBuffer byteBuffer = this.mappedByteBuffer.slice();
        byteBuffer.position(pos);
        ByteBuffer byteBufferNew = byteBuffer.slice();
        byteBufferNew.limit(size);
        return new SelectMappedBufferResult(this.fileFromOffset + pos, byteBufferNew, size, this);
      } else {
        log.warn("matched, but hold failed, request pos: " + pos + ", fileFromOffset: "
            + this.fileFromOffset);
      }
    } else {
      log.warn("selectMappedBuffer request pos invalid, request pos: " + pos + ", size: " + size
          + ", fileFromOffset: " + this.fileFromOffset);
    }

    return null;
  }

  //获取指定位置的消息
  public SelectMappedBufferResult selectMappedBuffer(int pos) {
    int readPosition = getReadPosition();
    if (pos < readPosition && pos >= 0) {
      if (this.hold()) {
        ByteBuffer byteBuffer = this.mappedByteBuffer.slice();
        byteBuffer.position(pos);
        int size = readPosition - pos;
        ByteBuffer byteBufferNew = byteBuffer.slice();
        byteBufferNew.limit(size);
        return new SelectMappedBufferResult(this.fileFromOffset + pos, byteBufferNew, size, this);
      }
    }

    return null;
  }

  //清理ByteBuffer
  public static void clean(final ByteBuffer buffer) {
    if (buffer == null || !buffer.isDirect() || buffer.capacity() == 0) {
      return;
    }
    invoke(invoke(viewed(buffer), "cleaner"), "clean");
  }

  private static Object invoke(final Object target, final String methodName, final Class<?>... args) {
    //获取特权，绕过权限检查执行方法
    return AccessController.doPrivileged(new PrivilegedAction<Object>() {
      @Override
      public Object run() {
        try {
          Method method = method(target, methodName, args);
          method.setAccessible(true);
          return method.invoke(target);
        } catch (Exception e) {
          throw new IllegalStateException(e);
        }
      }
    });
  }

  //取得对象内方法
  private static Method method(Object target, String methodName, Class<?>[] args)
      throws NoSuchMethodException {
    try {
      return target.getClass().getMethod(methodName, args);
    } catch (NoSuchMethodException e) {
      return target.getClass().getDeclaredMethod(methodName, args);
    }
  }

  private static ByteBuffer viewed(ByteBuffer buffer) {
    String methodName = "viewedBuffer";
    Method[] methods = buffer.getClass().getMethods();
    for (int i = 0; i < methods.length; i++) {
      if ("attachment".equals(methods[i].getName())) {
        methodName = "attachment";
        break;
      }
    }
    ByteBuffer viewedBuffer = (ByteBuffer) invoke(buffer, methodName);
    if (viewedBuffer == null) {
      return buffer;
    } else {
      return viewed(viewedBuffer);
    }
  }

  /**
   * @return The current flushed position
   */
  public int flush(final int flushLeastPages) {
    if (this.isAbleToFlush(flushLeastPages)) {
      if (this.hold()) {
        int value = getReadPosition();

        try {
          //We only append data to fileChannel or mappedByteBuffer, never both.
          if (writeBuffer != null || this.fileChannel.position() != 0) {
            this.fileChannel.force(false);
          } else {
            this.mappedByteBuffer.force();
          }
        } catch (Throwable e) {
          log.error("Error occurred when force data to disk.", e);
        }

        this.flushedPosition.set(value);
        this.release();
      } else {
        log.warn("in flush, hold failed, flush offset = " + this.flushedPosition.get());
        this.flushedPosition.set(getReadPosition());
      }
    }
    return this.getFlushedPosition();
  }

  public int commit(final int commitLeastPages) {
    if (writeBuffer == null) {
      //no need to commit data to file channel, so just regard wrotePosition as committedPosition.
      return this.wrotePosition.get();
    }
    if (this.isAbleToCommit(commitLeastPages)) {
      if (this.hold()) {
        commit0();
        this.release();
      } else {
        log.warn("in commit, hold failed, commit offset = " + this.committedPosition.get());
      }
    }

    // All dirty data has been committed to FileChannel.
    if (writeBuffer != null && this.fileSize == this.committedPosition.get()) {
      this.writeBuffer = null;
    }

    return this.committedPosition.get();
  }

  protected void commit0() {
    int writePos = this.wrotePosition.get();
    int lastCommittedPosition = this.committedPosition.get();

    if (writePos - lastCommittedPosition > 0) {
      try {
        ByteBuffer byteBuffer = writeBuffer.slice();
        byteBuffer.position(lastCommittedPosition);
        byteBuffer.limit(writePos);
        this.fileChannel.position(lastCommittedPosition);
        this.fileChannel.write(byteBuffer);
        this.committedPosition.set(writePos);
      } catch (Throwable e) {
        log.error("Error occurred when commit data to FileChannel.", e);
      }
    }
  }

  private boolean isAbleToFlush(final int flushLeastPages) {
    int flush = this.flushedPosition.get();
    int write = getReadPosition();

    if (this.isFull()) {
      return true;
    }

    if (flushLeastPages > 0) {
      return ((write / OS_PAGE_SIZE) - (flush / OS_PAGE_SIZE)) >= flushLeastPages;
    }

    return write > flush;
  }

  protected boolean isAbleToCommit(final int commitLeastPages) {
    int flush = this.committedPosition.get();
    int write = this.wrotePosition.get();

    if (this.isFull()) {
      return true;
    }

    if (commitLeastPages > 0) {
      return ((write / OS_PAGE_SIZE) - (flush / OS_PAGE_SIZE)) >= commitLeastPages;
    }

    return write > flush;
  }

  public boolean destroy(final long intervalForcibly) {
    this.shutdown(intervalForcibly);

    if (this.isCleanupOver()) {
      try {
        this.fileChannel.close();
        log.info("close file channel " + this.fileName + " OK");

        long beginTime = System.currentTimeMillis();
        boolean result = this.file.delete();
        log.info("delete file[REF:" + this.getRefCount() + "] " + this.fileName
            + (result ? " OK, " : " Failed, ") + "W:" + this.getWrotePosition() + " M:"
            + this.getFlushedPosition() + ", "
            + (System.currentTimeMillis() - beginTime));
      } catch (Exception e) {
        log.warn("close file channel " + this.fileName + " Failed. ", e);
      }

      return true;
    } else {
      log.warn("destroy mapped file[REF:" + this.getRefCount() + "] " + this.fileName
          + " Failed. cleanupOver: " + this.cleanupOver);
    }

    return false;
  }

  @Override
  public boolean cleanup(long currentRef) {
    if (this.isAvailable()) {
      log.error("this file[REF:" + currentRef + "] " + this.fileName
          + " have not shutdown, stop unmapping.");
      return false;
    }

    if (this.isCleanupOver()) {
      log.error("this file[REF:" + currentRef + "] " + this.fileName
          + " have cleanup, do not do it again.");
      return true;
    }

    clean(this.mappedByteBuffer);
    TOTAL_MAPPED_VIRTUAL_MEMORY.addAndGet(this.fileSize * (-1));
    TOTAL_MAPPED_FILES.decrementAndGet();
    log.info("unmap file[REF:" + currentRef + "] " + this.fileName + " OK");
    return true;
  }

  public boolean isFull() {
    return this.fileSize == this.wrotePosition.get();
  }

  public int getFlushedPosition() {
    return flushedPosition.get();
  }

  public void setFlushedPosition(int pos) {
    this.flushedPosition.set(pos);
  }

  public int getReadPosition() {
    return this.writeBuffer == null ? this.wrotePosition.get() : this.committedPosition.get();
  }

  public int getWrotePosition() {
    return wrotePosition.get();
  }

  public void setWrotePosition(int pos) {
    this.wrotePosition.set(pos);
  }

  public void setCommittedPosition(int pos) {
    this.committedPosition.set(pos);
  }

  public String getFileName() {
    return fileName;
  }

  public long getFileFromOffset() {
    return this.fileFromOffset;
  }

  public long getLastModifiedTimestamp() {
    return this.file.lastModified();
  }

  public int getFileSize() {
    return fileSize;
  }

  public long getStoreTimestamp() {
    return storeTimestamp;
  }

  public void setFirstCreateInQueue(boolean firstCreateInQueue) {
    this.firstCreateInQueue = firstCreateInQueue;
  }

  public File getFile() {
    return file;
  }

}
