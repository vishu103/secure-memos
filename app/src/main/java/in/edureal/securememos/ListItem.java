package in.edureal.securememos;

public class ListItem {

    private String title;
    private String dateTime;
    private int uid;

    public ListItem(String title, String dateTime, int uid) {
        this.title = title;
        this.dateTime = dateTime;
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public String getDateTime() {
        return dateTime;
    }

    public int getUid() {
        return uid;
    }
}
