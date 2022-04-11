package com.karen.common.util;


import java.util.UUID;
import java.util.zip.CRC32;

/**
 * @author WenHao
 * @ClassName crcUtils
 * @date 2022/1/21 19:40
 * @Description
 */
public class CrcUtils {

  public static int crc32(byte[] array) {
    if (array != null) {
      return crc32(array, 0, array.length);
    }

    return 0;
  }

  public static int crc32(byte[] array, int offset, int length) {
    CRC32 crc32 = new CRC32();
    crc32.update(array, offset, length);
    return (int) (crc32.getValue() & 0x7FFFFFFF);
  }

  public static void main(String[] args) throws Exception{
    for(int i = 0; i < 1000; i++){
      String str = UUID.randomUUID().toString()+"-"+UUID.randomUUID().toString();
      byte[] data = str.getBytes("UTF-8");
      int crc = crc32(data);
      byte[] crcByte = ByteUtils.intToByteArray(crc);
      int c = ByteUtils.byteArrayToInt(crcByte);
      System.out.println("crc int:" + crc + " crc byte len:" + crcByte.length + " res:" + (c == crc));
    }
  }

}
