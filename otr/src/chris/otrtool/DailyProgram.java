package chris.otrtool;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DailyProgram {
	private Date date;
	static Pattern ymd = Pattern.compile("(\\d\\d)\\.(\\d\\d)\\.(\\d\\d)");

	public DailyProgram(int year, int month, int day) {
		new DailyProgram(new GregorianCalendar(year, month, day).getTime());
	}

	public DailyProgram(Date date) {
		this.date = date;
	}

	public static DailyProgram parse(String desc) {
		Matcher matcher = ymd.matcher(desc);
		if (!matcher.matches())
			throw new IllegalArgumentException();
		return new DailyProgram(2000+Integer.parseInt(matcher.group(0)), Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
	}
}
