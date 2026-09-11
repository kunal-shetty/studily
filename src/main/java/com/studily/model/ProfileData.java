package com.studily.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Profile page view model.
 */
public class ProfileData {

    private String name;
    private String email;
    private LocalDateTime memberSince;
    private DashboardStats stats;
    private Set<String> badges;
    private List<Map<String, Object>> leaderboard;
    private int weeklyXp;
    private int weeklyRank;

    public static Map<String, String[]> BADGE_META = Map.of(
            "streak_7", new String[]{ "🔥", "7-Day Streak", "Studied 7 days in a row" },
            "flashcards_100", new String[]{ "📚", "Century Cards", "100 flashcards generated" },
            "accuracy_90", new String[]{ "🎯", "Sharpshooter", "90%+ average quiz accuracy" },
            "notes_10", new String[]{ "📁", "Librarian", "10 notes uploaded" },
            "first_quiz", new String[]{ "🧪", "First Quiz", "Completed your first quiz" }
    );

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDateTime getMemberSince() { return memberSince; }
    public void setMemberSince(LocalDateTime memberSince) { this.memberSince = memberSince; }

    public DashboardStats getStats() { return stats; }
    public void setStats(DashboardStats stats) { this.stats = stats; }

    public Set<String> getBadges() { return badges; }
    public void setBadges(Set<String> badges) { this.badges = badges; }

    public List<Map<String, Object>> getLeaderboard() { return leaderboard; }
    public void setLeaderboard(List<Map<String, Object>> leaderboard) { this.leaderboard = leaderboard; }

    public int getWeeklyXp() { return weeklyXp; }
    public void setWeeklyXp(int weeklyXp) { this.weeklyXp = weeklyXp; }

    public int getWeeklyRank() { return weeklyRank; }
    public void setWeeklyRank(int weeklyRank) { this.weeklyRank = weeklyRank; }
}
