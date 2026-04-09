package com.group1.career.model.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "assessment_results")
public class AssessmentResultDocument {

    @Id
    private String id;

    @Field("user_id")
    private Long userId;

    @Field("assessment_type")
    private String assessmentType;

    @Field("raw_answers")
    private Map<String, String> rawAnswers;

    @Field("calculated_traits")
    private Map<String, Integer> calculatedTraits;

    @Field("final_portrait")
    private String finalPortrait;

    @Field("created_at")
    private Date createdAt;
}
