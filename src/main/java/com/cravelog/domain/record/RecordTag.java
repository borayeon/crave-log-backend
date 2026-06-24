package com.cravelog.domain.record;

import com.cravelog.domain.tag.Tag;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "record_tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecordTag {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    private Record record;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    public RecordTag(Record record, Tag tag) {
        this.record = record;
        this.tag = tag;
    }
}