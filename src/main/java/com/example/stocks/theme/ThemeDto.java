package com.example.stocks.theme;

/**
 * 네이버 금융 테마 시세 1행.
 */
public class ThemeDto {

    private String name;
    private String changeRate;
    private String recent3DaysRate;
    private int upCount;
    private int steadyCount;
    private int downCount;
    private String leader1;
    private String leader2;

    public ThemeDto() {}

    public ThemeDto(String name, String changeRate, String recent3DaysRate,
                    int upCount, int steadyCount, int downCount,
                    String leader1, String leader2) {
        this.name = name;
        this.changeRate = changeRate;
        this.recent3DaysRate = recent3DaysRate;
        this.upCount = upCount;
        this.steadyCount = steadyCount;
        this.downCount = downCount;
        this.leader1 = leader1;
        this.leader2 = leader2;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getChangeRate() { return changeRate; }
    public void setChangeRate(String changeRate) { this.changeRate = changeRate; }

    public String getRecent3DaysRate() { return recent3DaysRate; }
    public void setRecent3DaysRate(String recent3DaysRate) { this.recent3DaysRate = recent3DaysRate; }

    public int getUpCount() { return upCount; }
    public void setUpCount(int upCount) { this.upCount = upCount; }

    public int getSteadyCount() { return steadyCount; }
    public void setSteadyCount(int steadyCount) { this.steadyCount = steadyCount; }

    public int getDownCount() { return downCount; }
    public void setDownCount(int downCount) { this.downCount = downCount; }

    public String getLeader1() { return leader1; }
    public void setLeader1(String leader1) { this.leader1 = leader1; }

    public String getLeader2() { return leader2; }
    public void setLeader2(String leader2) { this.leader2 = leader2; }

    public boolean isPositive() {
        return changeRate != null && changeRate.startsWith("+");
    }

    public boolean isNegative() {
        return changeRate != null && changeRate.startsWith("-");
    }
}
