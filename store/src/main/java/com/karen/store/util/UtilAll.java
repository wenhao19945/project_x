package com.karen.store.util;

import java.text.NumberFormat;

/**
 * @author WenHao
 * @ClassName UtilAll
 * @date 2020/2/15 14:46
 * @Description
 */
public class UtilAll {

  public static String offset2FileName(final long offset) {
    final NumberFormat nf = NumberFormat.getInstance();
    nf.setMinimumIntegerDigits(20);
    nf.setMaximumFractionDigits(0);
    nf.setGroupingUsed(false);
    return nf.format(offset);
  }

}
