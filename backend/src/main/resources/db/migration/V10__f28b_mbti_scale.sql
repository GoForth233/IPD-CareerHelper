-- V10 F28b: Add MBTI-style personality scale (scale_id = 1)
-- 4 dimensions: E/I (外向/内向)  N/S (直觉/实感)  T/F (思考/情感)  J/P (判断/知觉)
-- 20 binary-choice questions, 5 per dimension.
-- Question type: CHOICE (A = first pole, B = second pole)
-- Uses INSERT IGNORE + uk_scale_sort_order added in V9 to stay idempotent.

INSERT IGNORE INTO `assessment_scales`
  (scale_id, title, description, question_count, status, version, is_active)
VALUES
(1, '性格倾向测评(MBTI)',
   '探索你在四个维度上的性格倾向：外向/内向、直觉/实感、思考/情感、判断/知觉，帮助你了解自己的思维与决策风格',
   20, 1, 1, 1);

-- ─────────────────────────────────────────────────────────────────────────────
-- Questions  (5 × 4 dimensions = 20)
-- ─────────────────────────────────────────────────────────────────────────────
INSERT IGNORE INTO `assessment_questions`
  (scale_id, question_text, question_type, dimension_code, sort_order, is_active)
VALUES
-- E vs I (外向 vs 内向)
(1, '下班/下课后，你更倾向于？',                           'CHOICE', 'E_I',  1, 1),
(1, '在一个陌生的聚会上，你通常会？',                       'CHOICE', 'E_I',  2, 1),
(1, '你更擅长的沟通方式是？',                               'CHOICE', 'E_I',  3, 1),
(1, '独处一整天之后，你感觉？',                             'CHOICE', 'E_I',  4, 1),
(1, '面对新认识的人，你通常是？',                           'CHOICE', 'E_I',  5, 1),
-- N vs S (直觉 vs 实感)
(1, '在分析问题时，你更依赖？',                             'CHOICE', 'N_S',  6, 1),
(1, '你更感兴趣的是？',                                     'CHOICE', 'N_S',  7, 1),
(1, '面对一份新工作，你首先关注的是？',                     'CHOICE', 'N_S',  8, 1),
(1, '在学习新知识时，你更喜欢？',                           'CHOICE', 'N_S',  9, 1),
(1, '你更欣赏哪种类型的人？',                               'CHOICE', 'N_S', 10, 1),
-- T vs F (思考 vs 情感)
(1, '做决定时，你更看重的是？',                             'CHOICE', 'T_F', 11, 1),
(1, '朋友向你倾诉烦恼，你通常会？',                         'CHOICE', 'T_F', 12, 1),
(1, '面对批评，你的第一反应是？',                           'CHOICE', 'T_F', 13, 1),
(1, '在团队中，你更倾向于？',                               'CHOICE', 'T_F', 14, 1),
(1, '你认为好的决策应该？',                                 'CHOICE', 'T_F', 15, 1),
-- J vs P (判断 vs 知觉)
(1, '对于计划，你的态度是？',                               'CHOICE', 'J_P', 16, 1),
(1, '面对截止日期，你通常？',                               'CHOICE', 'J_P', 17, 1),
(1, '你的工作/学习空间通常是？',                            'CHOICE', 'J_P', 18, 1),
(1, '旅行时，你更喜欢？',                                   'CHOICE', 'J_P', 19, 1),
(1, '对于变化和意外，你的感受是？',                         'CHOICE', 'J_P', 20, 1);

-- ─────────────────────────────────────────────────────────────────────────────
-- Options  (A = first dimension pole, B = second dimension pole)
-- ─────────────────────────────────────────────────────────────────────────────

-- Q1 E vs I
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '和朋友出去聚会，充充电', 1.00, 'E' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=1;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '一个人待着，安静放松', 1.00, 'I' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=1;

-- Q2 E vs I
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '主动认识新朋友，享受社交', 1.00, 'E' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=2;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '找个角落，和熟人低调聊聊', 1.00, 'I' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=2;

-- Q3 E vs I
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '边说边想，越讲越清晰', 1.00, 'E' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=3;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '想清楚再开口，不喜欢被打断思路', 1.00, 'I' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=3;

-- Q4 E vs I
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '有点无聊，想找人说说话', 1.00, 'E' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=4;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '终于可以喘口气，很满足', 1.00, 'I' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=4;

-- Q5 E vs I
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '主动打招呼，很快进入话题', 1.00, 'E' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=5;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '等对方先开口，慢慢熟络', 1.00, 'I' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=5;

-- Q6 N vs S
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '直觉和整体感受', 1.00, 'N' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=6;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '具体事实和数据', 1.00, 'S' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=6;

-- Q7 N vs S
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '探索未来的可能性和潜力', 1.00, 'N' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=7;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '当下实际发生的事情', 1.00, 'S' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=7;

-- Q8 N vs S
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '这份工作有什么发展空间和意义', 1.00, 'N' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=8;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '具体的工作内容和日常职责', 1.00, 'S' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=8;

-- Q9 N vs S
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '先理解背后的规律和框架', 1.00, 'N' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=9;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '从具体例子出发，一步步来', 1.00, 'S' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=9;

-- Q10 N vs S
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '有创意、善于发现新思路的人', 1.00, 'N' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=10;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '脚踏实地、做事靠谱的人', 1.00, 'S' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=10;

-- Q11 T vs F
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '逻辑是否合理，结果是否公平', 1.00, 'T' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=11;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '对相关的人是否有影响，是否照顾到感受', 1.00, 'F' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=11;

-- Q12 T vs F
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '帮他分析问题，找出解决办法', 1.00, 'T' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=12;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '先倾听，让他感觉被理解', 1.00, 'F' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=12;

-- Q13 T vs F
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '分析批评是否有道理，有就接受', 1.00, 'T' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=13;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '先在意对方的态度和语气', 1.00, 'F' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=13;

-- Q14 T vs F
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '直接指出问题，追求高效', 1.00, 'T' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=14;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '照顾团队情绪，维护和谐氛围', 1.00, 'F' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=14;

-- Q15 T vs F
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '基于客观事实和逻辑推导', 1.00, 'T' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=15;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '考虑对人的影响，兼顾价值观', 1.00, 'F' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=15;

-- Q16 J vs P
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '提前计划好，心里踏实', 1.00, 'J' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=16;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '灵活应变，随时调整', 1.00, 'P' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=16;

-- Q17 J vs P
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '提前完成，不喜欢最后一刻赶', 1.00, 'J' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=17;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '临近截止才进入状态，压力下发挥更好', 1.00, 'P' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=17;

-- Q18 J vs P
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '整洁有序，东西各归其位', 1.00, 'J' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=18;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '看起来乱，但自己知道东西在哪', 1.00, 'P' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=18;

-- Q19 J vs P
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '提前订好酒店和行程，按计划走', 1.00, 'J' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=19;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '随性出发，走到哪算哪', 1.00, 'P' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=19;

-- Q20 J vs P
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '有点不适应，喜欢按计划进行', 1.00, 'J' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=20;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '觉得挺好的，变化让生活有趣', 1.00, 'P' FROM assessment_questions q WHERE q.scale_id=1 AND q.sort_order=20;
