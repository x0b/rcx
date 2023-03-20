package ca.pkay.rcloneexplorer.Items;


public class Trigger {

    public static String TABLE_NAME = "trigger_table";

    public static String COLUMN_NAME_ID= "trigger_id";
    public static String COLUMN_NAME_TITLE = "trigger_title";
    public static String COLUMN_NAME_TIME = "trigger_time";
    public static String COLUMN_NAME_WEEKDAY = "trigger_weekday";
    public static String COLUMN_NAME_ENABLED = "trigger_enabled";
    public static String COLUMN_NAME_TARGET = "trigger_target";
    public static String COLUMN_NAME_TYPE = "trigger_type";

    public static int TRIGGER_TYPE_SCHEDULE = 0;
    public static int TRIGGER_TYPE_INTERVAL = 1;

    public static long TRIGGER_ID_DOESNTEXIST = -1L;

    public static int TRIGGER_DAY_MON = 0;
    public static int TRIGGER_DAY_TUE = 1;
    public static int TRIGGER_DAY_WED = 2;
    public static int TRIGGER_DAY_THU = 3;
    public static int TRIGGER_DAY_FRI = 4;
    public static int TRIGGER_DAY_SAT = 5;
    public static int TRIGGER_DAY_SUN = 6;

    private Long id;

    private String title="";
    private boolean isEnabled=true;
    private byte weekdays = 0b01111111;      //treat as binary, so that each digit represents an boolean.
    private int time = 0;                   //in seconds since 00:00
    private Long whatToTrigger = 0L;
    private int type = TRIGGER_TYPE_SCHEDULE;

    public Trigger(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    /**
     * The weekday starts with monday.
     * Therefore is 0 monday, and 6 sunday.
     * @param weekday
     * @return
     */
    public boolean isEnabledAtDay(int weekday){
        return ((weekdays >> (weekday)) & 1)  == 1 ;
    }

    /**
     * The weekday starts with monday.
     * Therefore is 0 monday, and 6 sunday.
     * @param weekday
     * @param enabled
     */
    public void setEnabledAtDay(int weekday, boolean enabled){
        if(enabled){
            weekdays |= 1 << weekday;
        }else{
            weekdays &= ~(1 << weekday);
        }
    }

    public int getWeekdays() {
        return weekdays;
    }

    public void setWeekdays(byte weekdays) {
        this.weekdays = weekdays;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }


    // todo: rename this.
    public Long getWhatToTrigger() {
        return whatToTrigger;
    }

    public void setWhatToTrigger(Long whatToTrigger) {
        this.whatToTrigger = whatToTrigger;
    }


    @Override
    public String toString() {
        return "Trigger{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", isEnabled=" + isEnabled +
                ", weekdays=" + weekdays +
                ", time=" + time +
                ", whatToTrigger=" + whatToTrigger +
                ", type=" + type +
                '}';
    }

    private String binary(byte i){
        return String.format("%8s", Integer.toBinaryString(i & 0xFF)).replace(' ', '0');
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
