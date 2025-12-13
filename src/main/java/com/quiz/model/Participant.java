package com.quiz.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "participants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "total_score")
    private Integer totalScore = 0;

    @Column(name = "total_response_time")
    private Long totalResponseTime = 0L; // milliseconds - for tiebreaker

    @Column(name = "socket_id")
    private String socketId;

    @Column(name = "is_connected")
    private Boolean isConnected = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    @JsonIgnore
    private GameSession session;

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<ParticipantResponse> responses = new ArrayList<>();

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
