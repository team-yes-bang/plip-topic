package com.plip.topic.adapter.out.persistence.repository;

import com.plip.topic.adapter.out.persistence.entity.TopicCalendarDayEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TopicCalendarDayJpaRepository extends JpaRepository<TopicCalendarDayEntity, Long> {

	Optional<TopicCalendarDayEntity> findByAgitUuidAndDay(UUID agitUuid, LocalDate day);

	@Query("""
			SELECT d.day FROM TopicCalendarDayEntity d
			WHERE d.agitUuid = :agitUuid
			  AND d.day >= :from
			  AND d.day < :to
			  AND d.videoCount > 0
			ORDER BY d.day
			""")
	List<LocalDate> findActiveDays(
			@Param("agitUuid") UUID agitUuid,
			@Param("from") LocalDate from,
			@Param("to") LocalDate to
	);
}
