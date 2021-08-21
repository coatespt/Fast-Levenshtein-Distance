/**
 * 
 */
package a140.util;

//import org.apache.log4j.Logger;

import java.util.Date;

/**
 * @author peter
 * 
 */
public class TimeAndRate {
	//Logger log = Logger.getLogger(TimeAndRate.class);
	private Date start = new Date();
	private Date end = new Date();
	private long elapsedMillis = 0;
	private long elapsedSeconds = 0;
	private double rateSeconds = 0;
	private long eventCount = 0;
	private boolean frozen = false;
	private int eventsPerTic = 10000;
	private int ticsPerLine = 100;
	private int eventsPerLine = eventsPerTic * ticsPerLine;
	private boolean printTics = false;

	/**
	 * Reset everything as if it were new.
	 */
	public void reset() {
		eventCount = 0;
		start = new Date();
		end = new Date();
		frozen = false;
		elapsedMillis = 0;
		elapsedSeconds = 0;
		rateSeconds = 0;
	}

	public int getEventsPerTic() {
		return eventsPerTic;
	}

	public void setEventsPerTic(int eventsPerTic) {
		this.eventsPerTic = eventsPerTic;
	}

	public int getTicsPerLine() {
		return ticsPerLine;
	}

	public void setTicsPerLine(int ticsPerLine) {
		this.ticsPerLine = ticsPerLine;
	}

	public boolean isPrintTics() {
		return printTics;
	}

	public void setPrintTics(boolean printTics) {
		this.printTics = printTics;
	}

	public TimeAndRate() {
	}

	/**
	 * Set all the values. If not frozen, it will set them as of this instant.
	 */
	public void compute() {
		Date endTime = end;
		if (!frozen) {
			endTime = new Date();
		}
		elapsedMillis = endTime.getTime() - start.getTime();
		elapsedSeconds = elapsedMillis / 1000;
		rateSeconds = elapsedMillis == 0 ? 0 : ((double) eventCount
				/ elapsedMillis * 1000);
	}

	public String toString() {
		compute();
		StringBuffer sb = new StringBuffer();
		sb.append("events:");
		sb.append(eventCount);
		sb.append(" ms:");
		sb.append(elapsedMillis);
		sb.append(" sec:");
		sb.append(elapsedSeconds);
		sb.append(" rate:");
		sb.append(rateSeconds);
		sb.append("/sec ");
		return sb.toString();
	}

	public String toString(String leading) {
		compute();
		StringBuffer sb = new StringBuffer(128);
		sb.append(leading);
		sb.append(" ");
		sb.append("events:");
		sb.append(eventCount);
		sb.append(" ms:");
		sb.append(elapsedMillis);
		sb.append(" sec:");
		sb.append(elapsedSeconds);
		sb.append(" rate:");
		sb.append(rateSeconds);
		sb.append("/sec ");
		return sb.toString();
	}

	/**
	 * Set the number of events all at once if you aren't bumping it as you.
	 * 
	 * @param count
	 */
	public void setEventCount(long count) {
		eventCount = count;
	}

	/**
	 * Start the timer. If it is not started, information calls will fail.
	 */
	public void start() {
		start = new Date();
	}

	/**
	 * Register that one of the events you are measuring has occurred.
	 */
	public void event() throws Exception {
		if (frozen) {
			throw new Exception(
					"Calling event() on a frozen TimeAndRate object.");
		}
		eventCount++;
		if (printTics) {
			if (eventCount % eventsPerTic == 0) {
				System.out.print(".");
			}
			if (eventCount % eventsPerLine == 0) {
				System.out.print("\n");
			}
		}
	}

	public TimeAndRate freeze() {
		frozen = true;
		end = new Date();
		compute();
		return this;
	}

	public long elapsedMS() {
		return elapsedMillis;
	}

	public long elapsedSecs() {
		return elapsedMS() / 1000;
	}

	public double rateSecs() {
		return rateSeconds;
	}

	public long getEvents() {
		return (eventCount);
	}

}
