package chris.otrtool;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Show {
	private Date date;
	static Pattern ymd = Pattern
			.compile("(\\d\\d)\\.(\\d\\d)\\.(\\d\\d)_(\\d\\d)-(\\d\\d)");

	public Show(int year, int month, int day, int hour, int minute) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.set(year, month, day, hour, minute);
		new DailyProgram(cal.getTime());
	}

	public Show(Date date) {
		this.date = date;
		date.g
	}

	public static Show parse(String desc) {
		Matcher matcher = ymd.matcher(desc);
		if (!matcher.matches())
			throw new IllegalArgumentException();
		return new Show(2000 + Integer.parseInt(matcher.group(0)),
				Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher
						.group(2)), Integer.parseInt(matcher.group(3)),
				Integer.parseInt(matcher.group(4)));
	}
}
