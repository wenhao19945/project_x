package com.karen.common.util;

import java.nio.ByteBuffer;

/**
 * @author WenHao
 * @ClassName ByteUtils
 * @date 2022/1/19 20:53
 * @Description
 */
public class ByteUtils {

  private static final int EIGHT = 8;
  private static final int FOUR = 4;

  /**
   * 字符串转换为16进制字符串
   *
   * @param s
   * @return
   */
  public static String stringToHexString(String s) {
    String str = "";
    for (int i = 0; i < s.length(); i++) {
      int ch = s.charAt(i);
      String s4 = Integer.toHexString(ch);
      str = str + s4;
    }
    return str;
  }

  /**
   * 16进制字符串转换为字符串
   *
   * @param s
   * @return
   */
  public static String hexStringToString(String s) {
    if (s == null || "".equals(s)) {
      return null;
    }
    s = s.replace(" ", "");
    byte[] baKeyword = new byte[s.length() / 2];
    for (int i = 0; i < baKeyword.length; i++) {
      try {
        baKeyword[i] = (byte) (0xff & Integer.parseInt(
            s.substring(i * 2, i * 2 + 2), 16));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    try {
      s = new String(baKeyword, "gbk");
      new String();
    } catch (Exception e1) {
      e1.printStackTrace();
    }
    return s;
  }

  /**
   * 16进制表示的字符串转换为字节数组
   *
   * @param s 16进制表示的字符串
   * @return byte[] 字节数组
   */
  public static byte[] hexStringToByteArray(String s) {
    int len = s.length();
    byte[] b = new byte[len / 2];
    int two = 2;
    for (int i = 0; i < len; i += two) {
      // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个字节
      b[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
          .digit(s.charAt(i + 1), 16));
    }
    return b;
  }

  /**
   * byte数组转16进制字符串
   * @param bArray
   * @return
   */
  public static final String bytesToHexString(byte[] bArray) {
    StringBuffer sb = new StringBuffer(bArray.length);
    String sTemp;
    for (int i = 0; i < bArray.length; i++) {
      sTemp = Integer.toHexString(0xFF & bArray[i]);
      if (sTemp.length() < 2) {
        sb.append(0);
      }
      sb.append(sTemp.toUpperCase());
    }
    return sb.toString();
  }

  /**
   * byte[]转int，数组长度超过4，则index从0开始的多出的byte信息将会丢失.
   */
  public static int byteArrayToInt(byte[] bytes) {
    if (bytes == null) {
      return 0;
    }
    int result = 0;
    for (int i = 0; i < bytes.length; i++) {
      result += byteToInt(bytes[i]) << (EIGHT * (bytes.length - i - 1));
    }
    return result;
  }

  /**
   * byte to int.
   */
  public static int byteToInt(byte b) {
    return b & 0xFF;
  }

  /**
   * int转byte[4].
   */
  public static byte[] intToByteArray(int value) {
    byte[] result = new byte[FOUR];
    for (int i = 0; i < FOUR; i++) {
      result[FOUR - i - 1] = (byte) (value >> (EIGHT * i));
    }
    return result;
  }

  /**
   * long to byte[8].
   */
  public static byte[] longToByteArray(long l) {
    byte[] result = new byte[EIGHT];
    for (int i = 0; i < EIGHT; i++) {
      result[EIGHT - i - 1] = (byte) (l >> (EIGHT * i));
    }
    return result;
  }

  /**
   * byte[]转long，数组长度超过8，则index从0开始的多出的byte信息将会丢失.
   */
  public static long byteArrayToLong(byte[] bytes) {
    long result = 0;
    for (int i = 0; i < bytes.length; i++) {
      result += byteToLong(bytes[i]) << (EIGHT * (bytes.length - i - 1));
    }
    return result;
  }

  /**
   * short转byte[].
   */
  public static byte[] shortTo2Byte(short value) {
    byte[] result = new byte[2];
    result[0] = (byte) (0x00FF & (value>>8));
    result[1] = (byte) (0x00FF & value);
    return result;
  }

  /**
   * byte[]转short.
   */
  public static short byte2ToShort(byte[] bytes) {
    short res = (short)(((bytes[0] & 0x00FF) << 8) | (0x00FF & bytes[1]));
    return res;
  }

  /**
   * byte to long.
   */
  public static long byteToLong(byte b) {
    return b & 0xFF;
  }

  /**
   * @param signalInstallationCross 字符串
   * @param length 长度
   */
  public static byte[] str2ByteArray(String signalInstallationCross, int length) {
    byte[] bytes = signalInstallationCross.getBytes();
    ByteBuffer buffer = ByteBuffer.allocate(length);
    try {
      for (int i = 0;i<bytes.length;i++){
        if(buffer.remaining() > 0){
          buffer.put(bytes[i]);
        }
      }
      if(buffer.remaining() > 0){
        while (buffer.remaining() > 0){
          buffer.put((byte)0);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return buffer.array();
  }

}
