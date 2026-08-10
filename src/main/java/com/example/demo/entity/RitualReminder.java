package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "ritual_reminders")
@Getter
@Setter
@NoArgsConstructor
public class RitualReminder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ritual_id", nullable = false)
    private Integer ritualId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "time_of_day", nullable = false)
    private String timeOfDay; // HH:mm format

    @ElementCollection
    @CollectionTable(name = "reminder_days", joinColumns = @JoinColumn(name = "reminder_id"))
    @Column(name = "day_of_week")
    private List<String> daysOfWeek;
}
