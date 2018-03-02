package com.arkadiusz.dayscounter;

import static junit.framework.Assert.assertEquals;

import com.arkadiusz.dayscounter.utils.DateUtils;
import org.junit.Test;

//tests valid on 02.03.2018

public class DateUtilsUnitTests {


  @Test
  public void onlyYearsTest() {
    assertEquals(DateUtils.INSTANCE.calculateDate(2018, 2, 25, true, false, false, false), "0,0,0,0");
    assertEquals(DateUtils.INSTANCE.calculateDate(2017, 3, 2, true, true, true, true), "1,0,0,0");
    assertEquals(DateUtils.INSTANCE.calculateDate(2016, 3, 2, true, true, true, true), "2,0,0,0");
    assertEquals(DateUtils.INSTANCE.calculateDate(2015, 3, 2, true, true, true, true), "3,0,0,0");
    assertEquals(DateUtils.INSTANCE.calculateDate(2014, 3, 2, true, true, true, true), "4,0,0,0");
    assertEquals(DateUtils.INSTANCE.calculateDate(2013, 3, 2, true, true, true, true), "5,0,0,0");
    assertEquals(DateUtils.INSTANCE.calculateDate(2012, 3, 2, true, true, true, true), "6,0,0,0");
    assertEquals(DateUtils.INSTANCE.calculateDate(2011, 3, 2, true, true, true, true), "7,0,0,0");
    assertEquals(DateUtils.INSTANCE.calculateDate(2019, 3, 2, true, true, true, true), "1,0,0,0");
  }

  @Test
  public void onlyMonthsTest() {
    assertEquals(DateUtils.INSTANCE.calculateDate(2018, 2, 21, true, true, false, false), "0,0,0,0");
    assertEquals(DateUtils.INSTANCE.calculateDate(2018, 2, 2, true, true, true, true), "0,1,0,0");
    assertEquals(DateUtils.INSTANCE.calculateDate(2018, 1, 2, true, true, true, true), "0,2,0,0");
    assertEquals(DateUtils.INSTANCE.calculateDate(2017, 12, 2, true, true, true, true), "0,3,0,0");
    assertEquals(DateUtils.INSTANCE.calculateDate(2017, 11, 2, true, true, true, true), "0,4,0,0");
    assertEquals(DateUtils.INSTANCE.calculateDate(2017, 10, 2, true, true, true, true), "0,5,0,0");
    assertEquals(DateUtils.INSTANCE.calculateDate(2017, 9, 2, true, true, true, true), "0,6,0,0");
    assertEquals(DateUtils.INSTANCE.calculateDate(2017, 8, 2, true, true, true, true), "0,7,0,0");
    assertEquals(DateUtils.INSTANCE.calculateDate(2017, 4, 2, true, true, true, true), "0,11,0,0");
    assertEquals(DateUtils.INSTANCE.calculateDate(2019, 2, 2, true, true, true, true), "0,11,0,0");
    assertEquals(DateUtils.INSTANCE.calculateDate(2018, 4, 21, true, true, false, false), "0,1,0,0");
  }

  @Test
  public void onlyDaysTest() {
    assertEquals(DateUtils.INSTANCE.calculateDate(2018, 3, 2, true, true, true, true), "0,0,0,0");
    assertEquals(DateUtils.INSTANCE.calculateDate(2018, 2, 27, true, true, false, true), "0,0,0,3");
    assertEquals(DateUtils.INSTANCE.calculateDate(2018, 2, 4, true, true, false, true), "0,0,0,26");
    assertEquals(DateUtils.INSTANCE.calculateDate(2018, 2, 3, true, true, false, true), "0,0,0,27");
    assertEquals(DateUtils.INSTANCE.calculateDate(2018, 4, 3, true, false, false, true), "0,0,0,32");
  }

  @Test
  public void onlyWeeksTest() {
    assertEquals(DateUtils.INSTANCE.calculateDate(2018, 1, 5, true, false, true, false), "0,0,8,0");
    assertEquals(DateUtils.INSTANCE.calculateDate(2018, 2, 23, true, false, true, false), "0,0,1,0");
    assertEquals(DateUtils.INSTANCE.calculateDate(2018, 3, 16, true, false, true, false), "0,0,2,0");
  }

  @Test
  public void YearsAndMonths() {
    assertEquals(DateUtils.INSTANCE.calculateDate(2015, 10, 2, true, true, true, true), "2,5,0,0");
    assertEquals(DateUtils.INSTANCE.calculateDate(2009, 4, 2, true, true, true, true), "8,11,0,0");
    assertEquals(DateUtils.INSTANCE.calculateDate(1997, 2, 2, true, true, true, true), "21,1,0,0");
  }

  @Test
  public void MixTest() {
    //past
    assertEquals(DateUtils.INSTANCE.calculateDate(2018, 8, 21, true, true, true, true), "0,5,2,5");
    assertEquals(DateUtils.INSTANCE.calculateDate(2018, 8, 21, true, true, false, true), "0,5,0,19");
    assertEquals(DateUtils.INSTANCE.calculateDate(2014, 11, 16, true, true, false, true), "3,3,0,14");
    assertEquals(DateUtils.INSTANCE.calculateDate(2014, 11, 16, false, true, false, true), "0,39,0,14");
    assertEquals(DateUtils.INSTANCE.calculateDate(2014, 11, 16, false, false, true, true), "0,0,171,5");

    //future
    assertEquals(DateUtils.INSTANCE.calculateDate(2021, 7, 19, true, true, false, true), "3,4,0,17");
    assertEquals(DateUtils.INSTANCE.calculateDate(2021, 7, 19, false, true, false, true), "0,40,0,17");
    assertEquals(DateUtils.INSTANCE.calculateDate(2021, 7, 19, false, false, true, true), "0,0,176,3");
    assertEquals(DateUtils.INSTANCE.calculateDate(2021, 7, 19, false, false, true, false), "0,0,176,0");
  }


}