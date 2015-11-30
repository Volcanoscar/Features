package com.android.launcher3;

import java.util.Calendar;

public class CalendarUtil {
	private int gregorianYear;
	private int gregorianMonth;
	private int gregorianDate;
	private boolean isGregorianLeap;
	private int dayOfYear;
	private int dayOfWeek;
	private int chineseYear;
	private int chineseMonth;
	private int chineseDate;

	private static char[] daysInGregorianMonth = { 31, 28, 31, 30, 31, 30, 31,
			31, 30, 31, 30, 31 };

	public static final String[] daysOfMonth = { "1", "2", "3", "4", "5", "6",
			"7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17",
			"18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28",
			"29", "30", "31" };

	private String monthOfAlmanac[] = { "正月", "二月", "三月", "四月", "五月", "六月",
			"七月", "八月", "九月", "十月", "冬月", "腊月" };

	public CalendarUtil() {
		setGregorian(1901, 1, 1);
	}

	public CalendarUtil(Calendar calendar) {
		setGregorian(calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH) + 1,
				calendar.get(Calendar.DAY_OF_MONTH));
	}

	/**
	 * 得到对应天的农历 要判断闰月 月初 月末 "正月","二月","三月","四月","五月","六月"。。。。
	 * 
	 * @param y
	 * @param m
	 * @param d
	 * @return
	 */
	public String getChineseMonth(int y, int m, int d) {
		setGregorian(y, m, d);
		computeChineseFields();

		int cd = getChineseMonth();
		if (cd < 1 || cd > 29)
			cd = 1;
		return monthOfAlmanac[cd - 1];
	}

	public void setGregorian(int y, int m, int d) {
		gregorianYear = y;
		gregorianMonth = m;
		gregorianDate = d;
		isGregorianLeap = isGregorianLeapYear(y);
		dayOfYear = dayOfYear(y, m, d);
		dayOfWeek = dayOfWeek(y, m, d);
		chineseYear = 0;
		chineseMonth = 0;
		chineseDate = 0;
	}

	/**
	 * 判断是否是闰年
	 * 
	 * @param year
	 * @return
	 */
	public static boolean isGregorianLeapYear(int year) {
		boolean isLeap = false;
		if (year % 4 == 0)
			isLeap = true;
		if (year % 100 == 0)
			isLeap = false;
		if (year % 400 == 0)
			isLeap = true;
		return isLeap;
	}

	/**
	 * 返回一个月有几天
	 * 
	 * @param y
	 * @param m
	 * @return
	 */
	public static int daysInGregorianMonth(int y, int m) {
		int d = daysInGregorianMonth[m - 1];
		if (m == 2 && isGregorianLeapYear(y))
			d++; // 公历闰年二月多一天
		return d;
	}

	/**
	 * 计算当前天在本年中是第几天
	 * 
	 * @param y
	 * @param m
	 * @param d
	 * @return
	 */
	public static int dayOfYear(int y, int m, int d) {
		int c = 0;
		for (int i = 1; i < m; i++) {
			c = c + daysInGregorianMonth(y, i);
		}
		c = c + d;
		return c;
	}

	/**
	 * 当前天是本周的第几天 ， 从星期天开始算
	 * 
	 * @param y
	 * @param m
	 * @param d
	 * @return
	 */
	public static int dayOfWeek(int y, int m, int d) {
		int w = 1; // 公历一年一月一日是星期一，所以起始值为星期日
		y = (y - 1) % 400 + 1; // 公历星期值分部 400 年循环一次
		int ly = (y - 1) / 4; // 闰年次数
		ly = ly - (y - 1) / 100;
		ly = ly + (y - 1) / 400;
		int ry = y - 1 - ly; // 常年次数
		w = w + ry; // 常年星期值增一
		w = w + 2 * ly; // 闰年星期值增二
		w = w + dayOfYear(y, m, d);
		w = (w - 1) % 7 + 1;
		return w;
	}

	/**
	 * 农历月份大小压缩表，两个字节表示一年。两个字节共十六个二进制位数， 前四个位数表示闰月月份，后十二个位数表示十二个农历月份的大小。
	 */
	private static char[] chineseMonths = { 0x00, 0x04, 0xad, 0x08, 0x5a, 0x01,
			0xd5, 0x54, 0xb4, 0x09, 0x64, 0x05, 0x59, 0x45, 0x95, 0x0a, 0xa6,
			0x04, 0x55, 0x24, 0xad, 0x08, 0x5a, 0x62, 0xda, 0x04, 0xb4, 0x05,
			0xb4, 0x55, 0x52, 0x0d, 0x94, 0x0a, 0x4a, 0x2a, 0x56, 0x02, 0x6d,
			0x71, 0x6d, 0x01, 0xda, 0x02, 0xd2, 0x52, 0xa9, 0x05, 0x49, 0x0d,
			0x2a, 0x45, 0x2b, 0x09, 0x56, 0x01, 0xb5, 0x20, 0x6d, 0x01, 0x59,
			0x69, 0xd4, 0x0a, 0xa8, 0x05, 0xa9, 0x56, 0xa5, 0x04, 0x2b, 0x09,
			0x9e, 0x38, 0xb6, 0x08, 0xec, 0x74, 0x6c, 0x05, 0xd4, 0x0a, 0xe4,
			0x6a, 0x52, 0x05, 0x95, 0x0a, 0x5a, 0x42, 0x5b, 0x04, 0xb6, 0x04,
			0xb4, 0x22, 0x6a, 0x05, 0x52, 0x75, 0xc9, 0x0a, 0x52, 0x05, 0x35,
			0x55, 0x4d, 0x0a, 0x5a, 0x02, 0x5d, 0x31, 0xb5, 0x02, 0x6a, 0x8a,
			0x68, 0x05, 0xa9, 0x0a, 0x8a, 0x6a, 0x2a, 0x05, 0x2d, 0x09, 0xaa,
			0x48, 0x5a, 0x01, 0xb5, 0x09, 0xb0, 0x39, 0x64, 0x05, 0x25, 0x75,
			0x95, 0x0a, 0x96, 0x04, 0x4d, 0x54, 0xad, 0x04, 0xda, 0x04, 0xd4,
			0x44, 0xb4, 0x05, 0x54, 0x85, 0x52, 0x0d, 0x92, 0x0a, 0x56, 0x6a,
			0x56, 0x02, 0x6d, 0x02, 0x6a, 0x41, 0xda, 0x02, 0xb2, 0xa1, 0xa9,
			0x05, 0x49, 0x0d, 0x0a, 0x6d, 0x2a, 0x09, 0x56, 0x01, 0xad, 0x50,
			0x6d, 0x01, 0xd9, 0x02, 0xd1, 0x3a, 0xa8, 0x05, 0x29, 0x85, 0xa5,
			0x0c, 0x2a, 0x09, 0x96, 0x54, 0xb6, 0x08, 0x6c, 0x09, 0x64, 0x45,
			0xd4, 0x0a, 0xa4, 0x05, 0x51, 0x25, 0x95, 0x0a, 0x2a, 0x72, 0x5b,
			0x04, 0xb6, 0x04, 0xac, 0x52, 0x6a, 0x05, 0xd2, 0x0a, 0xa2, 0x4a,
			0x4a, 0x05, 0x55, 0x94, 0x2d, 0x0a, 0x5a, 0x02, 0x75, 0x61, 0xb5,
			0x02, 0x6a, 0x03, 0x61, 0x45, 0xa9, 0x0a, 0x4a, 0x05, 0x25, 0x25,
			0x2d, 0x09, 0x9a, 0x68, 0xda, 0x08, 0xb4, 0x09, 0xa8, 0x59, 0x54,
			0x03, 0xa5, 0x0a, 0x91, 0x3a, 0x96, 0x04, 0xad, 0xb0, 0xad, 0x04,
			0xda, 0x04, 0xf4, 0x62, 0xb4, 0x05, 0x54, 0x0b, 0x44, 0x5d, 0x52,
			0x0a, 0x95, 0x04, 0x55, 0x22, 0x6d, 0x02, 0x5a, 0x71, 0xda, 0x02,
			0xaa, 0x05, 0xb2, 0x55, 0x49, 0x0b, 0x4a, 0x0a, 0x2d, 0x39, 0x36,
			0x01, 0x6d, 0x80, 0x6d, 0x01, 0xd9, 0x02, 0xe9, 0x6a, 0xa8, 0x05,
			0x29, 0x0b, 0x9a, 0x4c, 0xaa, 0x08, 0xb6, 0x08, 0xb4, 0x38, 0x6c,
			0x09, 0x54, 0x75, 0xd4, 0x0a, 0xa4, 0x05, 0x45, 0x55, 0x95, 0x0a,
			0x9a, 0x04, 0x55, 0x44, 0xb5, 0x04, 0x6a, 0x82, 0x6a, 0x05, 0xd2,
			0x0a, 0x92, 0x6a, 0x4a, 0x05, 0x55, 0x0a, 0x2a, 0x4a, 0x5a, 0x02,
			0xb5, 0x02, 0xb2, 0x31, 0x69, 0x03, 0x31, 0x73, 0xa9, 0x0a, 0x4a,
			0x05, 0x2d, 0x55, 0x2d, 0x09, 0x5a, 0x01, 0xd5, 0x48, 0xb4, 0x09,
			0x68, 0x89, 0x54, 0x0b, 0xa4, 0x0a, 0xa5, 0x6a, 0x95, 0x04, 0xad,
			0x08, 0x6a, 0x44, 0xda, 0x04, 0x74, 0x05, 0xb0, 0x25, 0x54, 0x03 };

	// 初始日，公历农历对应日期：
	// 公历 1901 年 1 月 1 日，对应农历 4598 年 11 月 11 日
	private static int baseYear = 1901;
	private static int baseMonth = 1;
	private static int baseDate = 1;
	private static int baseIndex = 0;
	private static int baseChineseYear = 4598 - 1;
	private static int baseChineseMonth = 11;
	private static int baseChineseDate = 11;

	public int computeChineseFields() {
		if (gregorianYear < 1901 || gregorianYear > 2100)
			return 1;
		int startYear = baseYear;
		int startMonth = baseMonth;
		int startDate = baseDate;
		chineseYear = baseChineseYear;
		chineseMonth = baseChineseMonth;
		chineseDate = baseChineseDate;
		// 第二个对应日，用以提高计算效率
		// 公历 2000 年 1 月 1 日，对应农历 4697 年 11 月 25 日
		if (gregorianYear >= 2000) {
			startYear = baseYear + 99;
			startMonth = 1;
			startDate = 1;
			chineseYear = baseChineseYear + 99;
			chineseMonth = 11;
			chineseDate = 25;
		}
		int daysDiff = 0;
		for (int i = startYear; i < gregorianYear; i++) {
			daysDiff += 365;
			if (isGregorianLeapYear(i))
				daysDiff += 1; // leap year
		}
		for (int i = startMonth; i < gregorianMonth; i++) {
			daysDiff += daysInGregorianMonth(gregorianYear, i);
		}
		daysDiff += gregorianDate - startDate;

		chineseDate += daysDiff;
		int lastDate = daysInChineseMonth(chineseYear, chineseMonth);
		int nextMonth = nextChineseMonth(chineseYear, chineseMonth);
		while (chineseDate > lastDate) {
			if (Math.abs(nextMonth) < Math.abs(chineseMonth))
				chineseYear++;
			chineseMonth = nextMonth;
			chineseDate -= lastDate;
			lastDate = daysInChineseMonth(chineseYear, chineseMonth);
			nextMonth = nextChineseMonth(chineseYear, chineseMonth);
		}
		return 0;
	}

	private static int[] bigLeapMonthYears = {
			// 大闰月的闰年年份
			6, 14, 19, 25, 33, 36, 38, 41, 44, 52, 55, 79, 117, 136, 147, 150,
			155, 158, 185, 193 };

	/**
	 * 农历日期1.2.3.4.5.。。。
	 * 
	 * @param y
	 * @param m
	 * @return
	 */
	public static int daysInChineseMonth(int y, int m) {
		// 注意：闰月 m < 0
		int index = y - baseChineseYear + baseIndex;
		int v = 0;
		int l = 0;
		int d = 30;
		if (1 <= m && m <= 8) {
			v = chineseMonths[2 * index];
			l = m - 1;
			if (((v >> l) & 0x01) == 1)
				d = 29;
		} else if (9 <= m && m <= 12) {
			v = chineseMonths[2 * index + 1];
			l = m - 9;
			if (((v >> l) & 0x01) == 1)
				d = 29;
		} else {
			v = chineseMonths[2 * index + 1];
			v = (v >> 4) & 0x0F;
			if (v != Math.abs(m)) {
				d = 0;
			} else {
				d = 29;
				for (int i = 0; i < bigLeapMonthYears.length; i++) {
					if (bigLeapMonthYears[i] == index) {
						d = 30;
						break;
					}
				}
			}
		}
		return d;
	}

	/**
	 * 下一个农历月
	 * 
	 * @param y
	 * @param m
	 * @return
	 */
	public static int nextChineseMonth(int y, int m) {
		int n = Math.abs(m) + 1;
		if (m > 0) {
			int index = y - baseChineseYear + baseIndex;
			int v = chineseMonths[2 * index + 1];
			v = (v >> 4) & 0x0F;
			if (v == m)
				n = -m;
		}
		if (n == 13)
			n = 1;
		return n;
	}

	// 农历部分假日
	final static String[] lunarHoliday = new String[] {
			"0101The_Spring_Festival", "0115The_Lantern_Festival",
			"0505The_Dragon_Boat_Festival", "0707The_Magpie_Festiva",
			"0715The_Ghost_Festival", "0815The_Mid_Autumn_Festival",
			"0909The_Double_Ninth_Festival", "1208The_Laba_Festival",
			"1224The_Lunar_Year", "0100The_New_Year_Eve" };

	// 公历部分节假日
	final static String[] solarHoliday = new String[] { "0101THe_New_year_Day",
			"0214Valentine_Day", "0308Women_Day", "0312Arbor_Day",
			"0401April_Fools_Day", "0405Qingming_Festival", "0501Labor_Day",
			"0504Youth_Day", "0601Children_Day", "0910Teacher_Day",
			"1001National_Day","1031Halloween", "1225Christmas" };

	public static String getChineseHoliday(int y, int m, int d) {
		String data;
		String text = "";
		CalendarUtil c = new CalendarUtil();
		c.setGregorian(y, m, d);
		c.computeChineseFields();
		int day = c.getChineseDate();
		int month = c.getChineseMonth();
		data = (month < 10 ? "0" + month : String.valueOf(month))
				+ (day < 10 ? "0" + day : String.valueOf(day));
		for (int i = 0; i < lunarHoliday.length; i++) {
			if (data.equals(lunarHoliday[i].substring(0, 4))) {
				text = lunarHoliday[i].substring(4);
			}
		}
		return text;
	}

	/**
	 * 获取阳历节假日
	 * 
	 * @param month
	 * @param day
	 * @return
	 */
	public static String getGregorianHoliday(int month, int day) {
		String data;
		String text = "";
		data = (month < 10 ? "0" + month : String.valueOf(month))
				+ (day < 10 ? "0" + day : String.valueOf(day));
		for (int i = 0; i < solarHoliday.length; i++) {
			if (data.equals(solarHoliday[i].substring(0, 4))) {
				text = solarHoliday[i].substring(4);
			}
		}
		return text;
	}

	public static String isMontherDayOrFatherDay(int year, int month, int day) {
		String text = "";

		if (month == 5) {
			int week = dayOfWeek(year, month, day);
			if (week == 1) {
				week = dayOfWeek(year, month, 1);
				if (week == 1 && day == 8) {
					return "Mother_Day";
				} else if ((16 - week) == day) {
					return "Mother_Day";
				}
			}
		} else if (month == 6) {
			int week = dayOfWeek(year, month, day);
			if (week == 1) {
				week = dayOfWeek(year, month, 1);
				if (week == 1 && day == 15) {
					return "Father_Day";
				} else if ((23 - week) == day) {
					return "Father_Day";
				}
			}
		} else {
		}
		return text;
	}

	public boolean isFestival() {
		return !"".equals(getFestival());
	}

	public String getFestival() {
		String text = getChineseHoliday(gregorianYear, gregorianMonth,
				gregorianDate)
				+ getGregorianHoliday(gregorianMonth, gregorianDate)
				+ isMontherDayOrFatherDay(gregorianYear, gregorianMonth,
						gregorianDate);
		return text;
	}

	public int getGregorianYear() {
		return gregorianYear;
	}

	public void setGregorianYear(int gregorianYear) {
		this.gregorianYear = gregorianYear;
	}

	public int getGregorianMonth() {
		return gregorianMonth;
	}

	public void setGregorianMonth(int gregorianMonth) {
		this.gregorianMonth = gregorianMonth;
	}

	public int getGregorianDate() {
		return gregorianDate;
	}

	public void setGregorianDate(int gregorianDate) {
		this.gregorianDate = gregorianDate;
	}

	public boolean isGregorianLeap() {
		return isGregorianLeap;
	}

	public void setGregorianLeap(boolean isGregorianLeap) {
		this.isGregorianLeap = isGregorianLeap;
	}

	public int getDayOfYear() {
		return dayOfYear;
	}

	public void setDayOfYear(int dayOfYear) {
		this.dayOfYear = dayOfYear;
	}

	public int getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(int dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public int getChineseYear() {
		return chineseYear;
	}

	public void setChineseYear(int chineseYear) {
		this.chineseYear = chineseYear;
	}

	public int getChineseMonth() {
		return chineseMonth;
	}

	public void setChineseMonth(int chineseMonth) {
		this.chineseMonth = chineseMonth;
	}

	public int getChineseDate() {
		return chineseDate;
	}

	public void setChineseDate(int chineseDate) {
		this.chineseDate = chineseDate;
	}

	public static char[] getDaysInGregorianMonth() {
		return daysInGregorianMonth;
	}

	public static void setDaysInGregorianMonth(char[] daysInGregorianMonth) {
		CalendarUtil.daysInGregorianMonth = daysInGregorianMonth;
	}

	public static char[] getChineseMonths() {
		return chineseMonths;
	}

	public static void setChineseMonths(char[] chineseMonths) {
		CalendarUtil.chineseMonths = chineseMonths;
	}

	public static int getBaseYear() {
		return baseYear;
	}

	public static void setBaseYear(int baseYear) {
		CalendarUtil.baseYear = baseYear;
	}

	public static int getBaseMonth() {
		return baseMonth;
	}

	public static void setBaseMonth(int baseMonth) {
		CalendarUtil.baseMonth = baseMonth;
	}

	public static int getBaseDate() {
		return baseDate;
	}

	public static void setBaseDate(int baseDate) {
		CalendarUtil.baseDate = baseDate;
	}

	public static int getBaseIndex() {
		return baseIndex;
	}

	public static void setBaseIndex(int baseIndex) {
		CalendarUtil.baseIndex = baseIndex;
	}

	public static int getBaseChineseYear() {
		return baseChineseYear;
	}

	public static void setBaseChineseYear(int baseChineseYear) {
		CalendarUtil.baseChineseYear = baseChineseYear;
	}

	public static int getBaseChineseMonth() {
		return baseChineseMonth;
	}

	public static void setBaseChineseMonth(int baseChineseMonth) {
		CalendarUtil.baseChineseMonth = baseChineseMonth;
	}

	public static int getBaseChineseDate() {
		return baseChineseDate;
	}

	public static void setBaseChineseDate(int baseChineseDate) {
		CalendarUtil.baseChineseDate = baseChineseDate;
	}

	public static int[] getBigLeapMonthYears() {
		return bigLeapMonthYears;
	}

	public static void setBigLeapMonthYears(int[] bigLeapMonthYears) {
		CalendarUtil.bigLeapMonthYears = bigLeapMonthYears;
	}

}
