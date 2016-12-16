package models;

public class MyResultObject {
    private String title;
    private String url;
    private String indexed_date;
    private String description;

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setIndexedDate(String indexed_date) {
        this.indexed_date = indexed_date;
    }

    public String getIndexedDate() {
        return indexed_date;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
