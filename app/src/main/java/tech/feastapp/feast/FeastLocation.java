package tech.feastapp.feast;

public class FeastLocation {
    private long timestamp;
    private int people;
    private double latitude;
    private double longitude;
    private String name;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FeastLocation(long timestamp, int people, double latitude, double longitude, String name) {

        this.timestamp = timestamp;
        this.people = people;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getPeople() {
        return people;
    }


    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public FeastLocation() {

    }

    public void setPeople(int people) {

        this.people = people;
    }

}
