package com.cravelog.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "daily_visits", uniqueConstraints = {
    @UniqueConstraint(name = "uk_daily_visit_user_date", columnNames = {"user_id", "visit_date"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @Column(nullable = false)
    private long count = 0;

    public DailyVisit(User user, LocalDate visitDate, long initialCount) {
        this.user = user;
        this.visitDate = visitDate;
        this.count = initialCount;
    }

    public void increment() {
        this.count++;
    }
}
